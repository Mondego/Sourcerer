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
import edu.uci.ics.sourcerer.utils.db.sql.BindVariable;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.sql.SetStatement;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class SetStatementImpl extends StatementImpl implements SetStatement {
  private final DatabaseTable table;
  private final ArrayList<Column<?>> assignments;
  private final ArrayList<BindVariable> assignmentValues;
  private final ArrayList<ConstantCondition<?>> conditions;
  private final ArrayList<BindVariable> conditionValues;
  
  SetStatementImpl(QueryExecutorImpl executor, DatabaseTable table) {
    super(executor);
    this.table = table;
    this.assignments = Helper.newArrayList();
    this.assignmentValues = Helper.newArrayList();
    this.conditions = Helper.newArrayList();
    this.conditionValues = Helper.newArrayList();
  }
  
  public <T> void addAssignment(Column<T> column, T value) {
    if (column.getTable() == table) {
      throw new IllegalArgumentException("Column " + column.getName() + " is not from table " + table.getName());
    } else {
      int ind = assignments.indexOf(column);
      if (ind == -1) {
        assignments.add(column);
        assignmentValues.add(column.bind(value));
        reset();
      } else {
        assignmentValues.set(ind, column.bind(value));
      }
    }
  }
  
  public <T> void addWhere(ConstantCondition<T> condition, T value) {
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
      for (Column<?> assignment : assignments) {
        if (comma) {
          sql.append(", ");
        } else {
          comma = true;
        }
        sql.append(assignment.getName() + "=?");
      }
      if (!conditions.isEmpty()) {
        sql.append(" WHERE ");
        comma = false;
        for (ConstantCondition<?> condition : conditions) {
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
      for (BindVariable bind : assignmentValues) {
        bind.bind(statement, i++);
      }
      for (BindVariable bind : conditionValues) {
        bind.bind(statement, i++);
      }
      statement.executeUpdate();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error executing statement", e);
    }
  }
}
