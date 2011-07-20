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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.ArrayUtils;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.utils.db.sql.IBindVariable;
import edu.uci.ics.sourcerer.utils.db.sql.IComparisonCondition;
import edu.uci.ics.sourcerer.utils.db.sql.IConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.ITypedQueryResult;
import edu.uci.ics.sourcerer.utils.db.sql.ISelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.ISelectable;
import edu.uci.ics.sourcerer.utils.db.sql.ITable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class SelectQuery implements ISelectQuery {
  private QueryExecutor executor;
  protected PreparedStatement statement;
  
  private ITable[] tables;
  private IComparisonCondition[] joinConditions;
  private Map<ISelectable<?>, Integer> selects;
  private ArrayList<IConstantCondition<?>> constantConditions;
  private ArrayList<IBindVariable> constantConditionValues;
  
  SelectQuery(QueryExecutor executor) {
    this.executor = executor;
    
    selects = Helper.newLinkedHashMap();
    constantConditions = Helper.newArrayList();
    constantConditionValues = Helper.newArrayList();
  }
  
  void fromTable(ITable table) {
    if (tables == null) {
      tables = new ITable[1];
      tables[0] = table;
    } else {
      throw new IllegalStateException("Cannot set from twice");
    }
  }
  
  ITable[] fromJoin(IComparisonCondition ... conditions) {
    if (tables == null) {
      Map<ITable, Boolean> dupTables = Helper.newHashMap();
      for (int i = 0; i < conditions.length; i++) {
        if (i == 0) {
          ITable table = conditions[i].getLeftTable();
          if (dupTables.containsKey(table)) {
            dupTables.put(table, Boolean.TRUE);
          } else {
            dupTables.put(table, Boolean.FALSE);
          }
        } else {
          ITable past = conditions[i - 1].getRightTable();
          ITable present = conditions[i].getLeftTable();
          if (past != present) {
            throw new IllegalArgumentException("The tables do not match for " + conditions[i - 1] + " and " + conditions[i]);
          }
        }
        ITable table = conditions[i].getRightTable();
        if (dupTables.containsKey(table)) {
          dupTables.put(table, Boolean.TRUE);
        } else {
          dupTables.put(table, Boolean.FALSE);
        }
      }
      
      tables = new ITable[conditions.length + 1];
      for (int i = 0; i < conditions.length; i++) {
        if (i == 0) {
          ITable table = conditions[i].getLeftTable();
          if (dupTables.containsKey(table)) {
            tables[0] = new QualifiedTable(table, "t");
          } else {
            tables[0] = table;
          }
        }
        ITable table = conditions[i].getRightTable();
        if (dupTables.containsKey(table)) {
          tables[i + 1] = new QualifiedTable(table, "t" + i);
        } else {
          tables[i + 1] = table;
        }
      }
      
      joinConditions = conditions;
      return tables;
    } else {
      throw new IllegalStateException("Cannot set from twice");
    }
  }
  
  @Override
  public void addSelect(ISelectable<?> select) {
    verifyFresh();
    if (ArrayUtils.containsReference(tables, select.getTable())) {
      selects.put(select, Integer.valueOf(selects.size() + 1));
    } else {
      throw new IllegalStateException("Cannot select from " + select + " as its table is not in the from clause");
    }
  }
  
  @Override
  public <T> void addWhere(IConstantCondition<T> condition, T value) {
    if (!ArrayUtils.contains(tables, condition.getTable())) {
      throw new IllegalStateException("Cannot add " + condition + " as its table is not in the from clause");
    }
    
    int ind = constantConditions.indexOf(condition);
    if (ind == -1) {
      verifyFresh();
      constantConditions.add(condition);
      constantConditionValues.add(condition.bind(value));
    } else {
      constantConditionValues.set(ind, condition.bind(value));
    }
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
      for (ISelectable<?> select : selects.keySet()) {
        if (comma) {
          sql.append(", ");
        } else {
          comma = true;
        }
        sql.append(select.toSql());
      }
      
      sql.append(" FROM ");
      if (tables.length == 1) {
        sql.append(tables[0].toSql());
      } else {
        boolean rest = false;
        for (ITable table : tables) {
          if (rest) {
            sql.append(" INNER JOIN ");
          } else {
            rest = true;
          }
          sql.append(table.toSql());
        }
        
        sql.append(" ON ");
        rest = false;
        for (IComparisonCondition cond : joinConditions) {
          if (rest) {
            sql.append(" AND ");
          } else {
            rest = true;
          }
          sql.append(cond.toSql());
        }
      }
      
      if (!constantConditions.isEmpty()) {
        sql.append(" WHERE ");
        boolean rest = false;
        for (IConstantCondition<?> cond : constantConditions) {
          if (rest) {
            sql.append(" AND ");
          } else {
            rest = true;
          }
          sql.append(cond.toSql());
        }
      }
      sql.append(";");
      statement = executor.prepareStatement(sql.toString());
    }
    
    int i = 1;
    try {
      for (IBindVariable bind : constantConditionValues) {
        bind.bind(statement, i++);
      }
      
      statement.execute();
      return statement.getResultSet();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error executing statement", e);
      return null;
    }
  }

  @Override
  public ITypedQueryResult select() {
    return new QueryResult(execute());
  }
  
  @Override
  public ITypedQueryResult selectStreamed() {
    return new QueryResult(execute());
  }
  
  private class QueryResult implements ITypedQueryResult {
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
          return result.next();
        } catch (SQLException e) {
          logger.log(Level.SEVERE, "Error getting next result.", e);
          return false;
        }
      }
    }

    @Override
    public <T> T getResult(ISelectable<T> selectable) {
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
  }
}
