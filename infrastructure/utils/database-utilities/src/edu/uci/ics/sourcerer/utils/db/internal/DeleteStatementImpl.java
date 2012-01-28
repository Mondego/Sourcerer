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
import java.util.logging.Level;

import edu.uci.ics.sourcerer.utils.db.sql.Condition;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.sql.DeleteStatement;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class DeleteStatementImpl extends StatementImpl implements DeleteStatement {
  private final DatabaseTable table;
  private Condition whereCondition;
  
  DeleteStatementImpl(QueryExecutorImpl executor, DatabaseTable table) {
    super(executor);
    this.table = table;
  }
  
  @Override
  public void andWhere(Condition condition) {
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
      StringBuilder sql = new StringBuilder("DELETE FROM ");
      sql.append(table.toSql());
      if (whereCondition != null) {
        sql.append(" WHERE ");
        whereCondition.toSql(sql);
      }
      sql.append(";");
      prepareStatement(sql.toString());
    }
    try {
      if (whereCondition != null) {
        whereCondition.bind(statement, 1);
      }
      
      statement.executeUpdate();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error executing statement", e);
    }
  }
}
