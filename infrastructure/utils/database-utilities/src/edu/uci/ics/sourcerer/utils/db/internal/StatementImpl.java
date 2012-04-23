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
import java.sql.SQLException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.utils.db.sql.Statement;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
abstract class StatementImpl implements Statement {
  private QueryExecutorImpl executor;
  protected PreparedStatement statement;
  
  StatementImpl(QueryExecutorImpl executor) {
    this.executor = executor;
  }
  
  protected void prepareStatement(String sql) {
    statement = executor.prepareStatement(sql);
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
  
  @Override
  public abstract void execute();
}
