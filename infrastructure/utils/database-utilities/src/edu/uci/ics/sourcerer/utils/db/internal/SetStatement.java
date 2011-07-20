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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.sql.IBindVariable;
import edu.uci.ics.sourcerer.utils.db.sql.IColumn;
import edu.uci.ics.sourcerer.utils.db.sql.IConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.ISetStatement;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class SetStatement extends Statement implements ISetStatement {
  private final DatabaseTable table;
  private final ArrayList<String> assignments;
  private final ArrayList<IBindVariable> assignmentValues;
  private final ArrayList<IConstantCondition<?>> conditions;
  private final ArrayList<IBindVariable> conditionValues;
  
  SetStatement(QueryExecutor executor, DatabaseTable table) {
    super(executor);
    this.table = table;
    this.assignments = Helper.newArrayList();
    this.assignmentValues = Helper.newArrayList();
    this.conditions = Helper.newArrayList();
    this.conditionValues = Helper.newArrayList();
  }
  
  public <T> void addAssignment(IColumn<T> column, T value) {
    if (column.getTable() == table) {
      throw new IllegalArgumentException("Column " + column.getName() + " is not from table " + table.getName());
    } else {
      int ind = assignments.indexOf(column);
      if (ind == -1) {
        assignments.add(column.getName() + "=?");
        assignmentValues.add(column.bind(value));
        reset();
      } else {
        assignmentValues.set(ind, column.bind(value));
      }
    }
  }
  
  public <T> void addWhere(IConstantCondition<T> condition, T value) {
    if (condition.getTable() != table) {
      throw new IllegalArgumentException("Condition " + condition.toSql() + " is not for table " + table.getName());
    }
    int ind = conditions.indexOf(condition);
    if (ind == -1) {
      conditions.add(condition);
      conditionValues.add(condition.bind(value));
      reset();
    } else {
      conditionValues.set(ind, condition.bind(value));
    }
  }
  
  public void execute() {
    if (statement == null) {
      StringBuilder sql = new StringBuilder("UPDATE ");
      sql.append(table.toSql()).append(" SET ");
      boolean comma = false;
      for (String assignment : assignments) {
        if (comma) {
          sql.append(", ");
        } else {
          comma = true;
        }
        sql.append(assignment);
      }
      if (!conditions.isEmpty()) {
        sql.append(" WHERE ");
        comma = false;
        for (IConstantCondition<?> condition : conditions) {
          if (comma) {
            sql.append(" AND ");
          } else {
            comma = true;
          }
          sql.append(condition.toSql());
        }
      }
      sql.append(";");
      prepareStatement(sql.toString());
    }
    int i = 1;
    try {
      for (IBindVariable bind : assignmentValues) {
        bind.bind(statement, i++);
      }
      for (IBindVariable bind : conditionValues) {
        bind.bind(statement, i++);
      }
      statement.executeUpdate();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error executing statement", e);
    }
  }
}
