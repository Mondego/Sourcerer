/* 
 * Sourcerer: an infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.uci.ics.sourcerer.utils.db.internal;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.ArrayUtils;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.utils.db.sql.ComparisonCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Condition;
import edu.uci.ics.sourcerer.utils.db.sql.ResultConstructor;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;
import edu.uci.ics.sourcerer.utils.db.sql.Table;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class SelectQueryImpl implements SelectQuery {
  private QueryExecutorImpl executor;
  protected PreparedStatement statement;
  
  private Table[] tables;
  private ComparisonCondition[] joinConditions;
  private boolean count;
  private boolean distinct;
  private Map<Selectable<?>, Integer> selects;
  private Condition whereCondition;
  private ArrayList<Pair<Selectable<?>, Boolean>> orderBy;
  private int limit = 0;
  
  SelectQueryImpl(QueryExecutorImpl executor) {
    this.executor = executor;
    
    selects = new LinkedHashMap<>();
    orderBy = new ArrayList<>();
  }
  
  void fromTable(Table table) {
    if (tables == null) {
      tables = new Table[1];
      tables[0] = table;
    } else {
      throw new IllegalStateException("Cannot set from twice");
    }
  }
  
  void fromJoin(ComparisonCondition ... conditions) {
    if (tables == null) {
      Set<Table> usedTables = new HashSet<>();
      for (int i = 0; i < conditions.length; i++) {
        if (i == 0) {
          Table table = conditions[i].getLeftTable();
          if (usedTables.contains(table)) {
            throw new IllegalArgumentException("Duplicate tables must be qualified: " + table.toSql());
          } else {
            usedTables.add(table);
          }
        } else {
          Table past = conditions[i - 1].getRightTable();
          Table present = conditions[i].getLeftTable();
          if (past != present) {
            throw new IllegalArgumentException("The tables do not match for " + conditions[i - 1] + " and " + conditions[i]);
          }
        }
        Table table = conditions[i].getRightTable();
        if (usedTables.contains(table)) {
          throw new IllegalArgumentException("Duplicate tables must be qualified: " + table.toSql());
        } else {
          usedTables.add(table);
        }
      }
      
      tables = new Table[conditions.length + 1];
      for (int i = 0; i < conditions.length; i++) {
        if (i == 0) {
          tables[0] = conditions[i].getLeftTable();
        }
        tables[i + 1] = conditions[i].getRightTable();
      }
      
      joinConditions = conditions;
    } else {
      throw new IllegalStateException("Cannot set from twice");
    }
  }
  
  @Override
  public void setCount(boolean count) {
    this.count = count;
  }
  
  @Override
  public void setDistinct(boolean distinct) {
    this.distinct = distinct;
  }
  
  private void addSelect(Selectable<?> select) {
    verifyFresh();
    if (ArrayUtils.containsReference(tables, select.getTable())) {
      selects.put(select, Integer.valueOf(selects.size() + 1));
    } else {
      throw new IllegalStateException("Cannot select from " + select + " as its table is not in the from clause");
    }
  }
  
  @Override
  public void addSelect(Selectable<?> ... selects) {
    for (Selectable<?> select : selects) {
      addSelect(select);
    }
  }
  
  @Override
  public void clearSelect() {
    close();
    selects.clear();
  }
  
  private void andWhere(Condition condition) {
    verifyFresh();
    condition.verifyTables(tables);
    if (whereCondition == null) {
      whereCondition = condition;
    } else {
      whereCondition = whereCondition.and(condition);
    }
  }
  
  @Override
  public void andWhere(Condition ... conditions) {
    for (Condition condition : conditions) {
      andWhere(condition);
    }
  }
  
  @Override
  public void clearWhere() {
    close();
    whereCondition = null;
  }
  
  @Override
  public void orderBy(Selectable<?> select, boolean ascending) {
    verifyFresh();
    orderBy.add(new Pair<Selectable<?>, Boolean>(select, ascending));
  }
  
  @Override
  public void setLimit(int limit) {
    verifyFresh();
    this.limit = limit;
  }
  
  private void verifyFresh() {
    if (statement != null) {
      throw new IllegalStateException("May not alter query once executed.");
    }
  }

  private ResultSet execute(boolean streamed) {
    if (statement == null) {
      if (selects.isEmpty() && !count) {
        throw new IllegalStateException("Must have at least one select.");
      } else if (tables == null) {
        throw new IllegalStateException("Must have at least one table.");
      }
      StringBuilder sql = new StringBuilder("SELECT ");
      if (count) {
        sql.append("COUNT(");
      }
      if (distinct) {
        sql.append("DISTINCT ");
      }
      if (selects.isEmpty()) {
        sql.append("*");
      } else {
        boolean comma = false;
        for (Selectable<?> select : selects.keySet()) {
          if (comma) {
            sql.append(", ");
          } else {
            comma = true;
          }
          select.toSql(sql);
        }
      }
      if (count) {
        sql.append(")");
      }
      
      sql.append(" FROM ");
      if (tables.length == 1) {
        sql.append(tables[0].toSql());
      } else {
        boolean rest = false;
        for (Table table : tables) {
          if (rest) {
            sql.append(" INNER JOIN ");
          } else {
            rest = true;
          }
          sql.append(table.toSql());
        }
        
        sql.append(" ON ");
        rest = false;
        for (ComparisonCondition cond : joinConditions) {
          if (rest) {
            sql.append(" AND ");
          } else {
            rest = true;
          }
          cond.toSql(sql);
        }
      }
      
      if (whereCondition != null) {
        sql.append(" WHERE ");
        whereCondition.toSql(sql);
      }
      
      if (!orderBy.isEmpty()) {
        sql.append(" ORDER BY ");
        boolean rest = false;
        for (Pair<Selectable<?>, Boolean> pair : orderBy) {
          if (rest) {
            sql.append(',');
          } else {
            rest = true;
          }
          pair.getFirst().toSql(sql);
          sql.append(pair.getSecond() ? " ASC" : " DESC");
        }
      }
      
      if (limit > 0) {
        sql.append(" LIMIT ").append(limit);
      }
      sql.append(";");
      statement = executor.prepareStatement(sql.toString());
    }
    
    try {
      if (whereCondition != null) {
        whereCondition.bind(statement, 1);
      }
      if (streamed) {
        statement.setFetchSize(Integer.MIN_VALUE);
      } else {
        statement.setFetchSize(10);
      }
      statement.execute();
      return statement.getResultSet();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error executing statement", e);
      return null;
    }
  }

  @Override
  public TypedQueryResult select() {
    return new QueryResult(execute(false));
  }
  
  @Override
  public TypedQueryResult selectStreamed() {
    return new QueryResult(execute(true));
  }
  
  private class QueryResult implements TypedQueryResult {
    private ResultSet result = null;
    
    QueryResult(ResultSet result) {
      this.result = result;
    }

    @Override
    public boolean next() {
      if (result == null) {
        return false;
      } else {
        try {
          boolean val = result.next();
          if (!val) {
            result.close();
            result = null;
          }
          return val;
        } catch (SQLException e) {
          logger.log(Level.SEVERE, "Error getting next result.", e);
          result = null;
          return false;
        }
      }
    }

    @Override
    public int getCount() {
      if (SelectQueryImpl.this.count) {
        if (result != null) {
          try {
            return result.getInt(1);
          } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting result.", e);
          }
        }
        return 0;
      } else {
        throw new IllegalStateException("Count not enabled.");
      }
    }
    
    @Override
    public int toCount() {
      if (next()) {
        return getCount();
      } else {
        return 0;
      }
    }
    
    @Override
    public <T> T getResult(Selectable<T> selectable) {
      if (SelectQueryImpl.this.count) {
        throw new IllegalStateException("Count enabled.");
      } else {
        if (result != null) {
          Integer index = selects.get(selectable);
          if (index == null) {
            throw new IllegalArgumentException("Column not in select: " + selectable);
          } else {
            try {
              byte[] bytes = result.getBytes(index);
              if (bytes == null) {
                return selectable.from(null);
              } else {
                return selectable.from(new String(bytes, "UTF-8"));
              }
            } catch (SQLException | UnsupportedEncodingException e) {
              logger.log(Level.SEVERE, "Error getting result.", e);
            }
          }
        }
        return null;
      }
    }
    
    @Override
    public <T> Collection<T> toCollection(Selectable<T> selectable) {
      Collection<T> collection = new ArrayList<>();
      while (next()) {
        collection.add(getResult(selectable));
      }
      return collection;
    }
    

    @Override
    public <T> Collection<T> toCollection(ResultConstructor<T> constructor) {
      Collection<T> collection = new ArrayList<>();
      while (next()) {
        collection.add(constructor.constructResult(this));
      }
      return collection;
    }
    
    @Override
    public <T> Iterable<T> toIterable(final Selectable<T> selectable) {
      return new Iterable<T>() {
        @Override
        public Iterator<T> iterator() {
          return new Iterator<T>() {
            T next = null;
            @Override
            public boolean hasNext() {
              if (next == null) {
                if (QueryResult.this.next()) {
                  next = getResult(selectable);
                  return true;
                } else {
                  return false;
                }
              } else {
                return true;
              }
            }

            @Override
            public T next() {
              if (hasNext()) {
                T result = next;
                next = null;
                return result;
              } else {
                throw new NoSuchElementException();
              }
            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }
          };
        }
      };
    }
    
    @Override
    public <T> Iterable<T> toIterable(final ResultConstructor<T> constructor) {
      return new Iterable<T>() {
        @Override
        public Iterator<T> iterator() {
          return new Iterator<T>() {
            T next = null;
            @Override
            public boolean hasNext() {
              if (next == null) {
                if (QueryResult.this.next()) {
                  next = constructor.constructResult(QueryResult.this);
                  return true;
                } else {
                  return false;
                }
              } else {
                return true;
              }
            }

            @Override
            public T next() {
              if (hasNext()) {
                T result = next;
                next = null;
                return result;
              } else {
                throw new NoSuchElementException();
              }
            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }
          };
        }
      };
    }

    @Override
    public <T> T toSingleton(Selectable<T> selectable, boolean permitMissing) {
      if (next()) {
        T val = getResult(selectable);
        if (next()) {
          logger.log(Level.WARNING, "More than one result returned for a singleton.");
          try {
            result.close();
          } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error closing result.", e);
          }
          result = null;
        }
        return val;
      } else {
        if (permitMissing) {
          return null;
        } else {
          throw new NoSuchElementException();
        }
      }
    }
    
    @Override
    public <T> T toSingleton(ResultConstructor<T> constructor, boolean permitMissing) {
      if (next()) {
        T val = constructor.constructResult(this);
        if (next()) {
          logger.log(Level.WARNING, "More than one result returned for a singleton.");
          try {
            result.close();
          } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error closing result.", e);
          }
          result = null;
        }
        return val;
      } else {
        if (permitMissing) {
          return null;
        } else {
          throw new NoSuchElementException();
        }
      }
    }
    
    @Override
    public void close() {
      if (result != null) {
        try {
          result.close();
        } catch (SQLException e) {
          logger.log(Level.SEVERE, "Error closing result set", e);
        }
      }
    }
  }
  
  @Override
  public void close() {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error closing statement.", e);
      }
      statement = null;
    }
  }
}
