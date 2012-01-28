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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.ArrayUtils;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.utils.db.sql.ComparisonCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Condition;
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
  private Map<Selectable<?>, Integer> selects;
  private Condition whereCondition;
  private ArrayList<Pair<Selectable<?>, Boolean>> orderBy;
  
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
  public void addSelect(Selectable<?> select) {
    verifyFresh();
    if (ArrayUtils.containsReference(tables, select.getTable())) {
      selects.put(select, Integer.valueOf(selects.size() + 1));
    } else {
      throw new IllegalStateException("Cannot select from " + select + " as its table is not in the from clause");
    }
  }
  
  @Override
  public void addSelects(Selectable<?> ... selects) {
    for (Selectable<?> select : selects) {
      addSelect(select);
    }
  }
  
  @Override
  public void clearSelect() {
    close();
    selects.clear();
  }
  
  @Override
  public void andWhere(Condition condition) {
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
    orderBy.add(new Pair<Selectable<?>, Boolean>(select, ascending));
  }
  
  private void verifyFresh() {
    if (statement != null) {
      throw new IllegalStateException("May not alter query once executed.");
    }
  }

  private ResultSet execute() {
    if (statement == null) {
      if (selects.isEmpty()) {
        throw new IllegalStateException("Must have at least one select.");
      } else if (tables == null) {
        throw new IllegalStateException("Must have at least one table.");
      }
      StringBuilder sql = new StringBuilder("SELECT ");
      boolean comma = false;
      for (Selectable<?> select : selects.keySet()) {
        if (comma) {
          sql.append(", ");
        } else {
          comma = true;
        }
        select.toSql(sql);
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
      sql.append(";");
      statement = executor.prepareStatement(sql.toString());
    }
    
    try {
      if (whereCondition != null) {
        whereCondition.bind(statement, 1);
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
    return new QueryResult(execute());
  }
  
  @Override
  public TypedQueryResult selectStreamed() {
    return new QueryResult(execute());
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
    public <T> T getResult(Selectable<T> selectable) {
      Integer index = selects.get(selectable);
      if (index == null) {
        throw new IllegalArgumentException("Column not in select: " + selectable);
      } else {
        try {
          return selectable.from(result.getString(index));
        } catch (SQLException e) {
          logger.log(Level.SEVERE, "Error getting result.", e);
          return null;
        }
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
