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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.utils.db.sql.Assignment;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.Condition;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.sql.SetStatement;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class SetStatementImpl extends StatementImpl implements SetStatement {
  private final DatabaseTable table;
  private final ArrayList<AssignmentImpl<?>> assignments;
  private Condition whereCondition;
  
  SetStatementImpl(QueryExecutorImpl executor, DatabaseTable table) {
    super(executor);
    this.table = table;
    this.assignments = new ArrayList<>();
  }
  
  @Override
  public <T> Assignment<T> addAssignment(Column<T> column) {
    if (column.getTable() != table) {
      throw new IllegalArgumentException("Column " + column.getName() + " is not from table " + table.getName());
    } else {
      close();
      AssignmentImpl<T> ass = new AssignmentImpl<T>(column);
      assignments.add(ass);
      return ass;
    }
  }
  
  @Override
  public <T> Assignment<T> addAssignment(Column<T> column, T value) {
    Assignment<T> ass = addAssignment(column);
    ass.setValue(value);
    return ass;
  }
  
  @Override
  public void andWhere(Condition condition) {
    close();
    condition.verifyTables(table);
    if (whereCondition == null) {
      whereCondition = condition;
    } else {
      whereCondition = whereCondition.and(condition);
    }
  }
  
  @Override
  public void execute() {
    if (statement == null) {
      StringBuilder sql = new StringBuilder("UPDATE ");
      sql.append(table.toSql()).append(" SET ");
      boolean comma = false;
      for (AssignmentImpl<?> assignment : assignments) {
        if (comma) {
          sql.append(", ");
        } else {
          comma = true;
        }
        assignment.toSql(sql);
      }
      if (whereCondition != null) {
        sql.append(" WHERE ");
        comma = false;
        whereCondition.toSql(sql);
      }
      sql.append(";");
      prepareStatement(sql.toString());
    }
    try {
      int i = 1;
      for (AssignmentImpl<?> assignment : assignments) {
        i = assignment.bind(statement, i);
      }
      if (whereCondition != null) {
        whereCondition.bind(statement, i);
      }
      
      statement.executeUpdate();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error executing statement", e);
    }
  }
}
