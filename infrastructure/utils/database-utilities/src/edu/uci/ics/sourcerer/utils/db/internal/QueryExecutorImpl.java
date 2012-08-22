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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;
import edu.uci.ics.sourcerer.utils.db.TableLocker;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.ComparisonCondition;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.sql.DeleteStatement;
import edu.uci.ics.sourcerer.utils.db.sql.QueryResult;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.SetStatement;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class QueryExecutorImpl implements QueryExecutor {
  private Connection connection;
  private java.sql.Statement statement;
  private TableLocker locker;

  private QueryExecutorImpl(Connection connection) {
    this.connection = connection;
  }
  
  public static QueryExecutorImpl make(Connection connection) {
    QueryExecutorImpl exec = new QueryExecutorImpl(connection);
    if (exec.reset()) {
      return exec;
    } else {
      return null;
    }
  }
  
  boolean reset() {
    close();
    try {
      statement = connection.createStatement();
      return true;
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Unable to create statement", e);
      statement = null;
      return false;
    }
  }
  
  private void verifyOpen() {
    if (statement == null) {
      throw new IllegalStateException("Statement is not open.");
    }
  }
  
  @Override
  public void close() {
    try {
      if (statement != null) {
        statement.close();
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Unable to close statement", e);
    }
    statement = null;
  }
  
  @Override
  public TableLocker getTableLocker() {
    if (locker == null) {
      locker = new TableLockerImpl(this);
    }
    return locker;
  }
  
  @Override
  public BatchInserter makeInFileInserter(File tempDir, DatabaseTable table) {
    return InFileInserter.makeInFileInserter(tempDir, this, table);
  }
  
  PreparedStatement prepareStatement(String sql) {
    try {
      return connection.prepareStatement(sql);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error preparing statement", e);
      return null;
    }
  }
  
  @Override
  public void executeUpdate(String sql) {
    verifyOpen();
    try {
      statement.executeUpdate(sql);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in update: " + sql, e);
    }
  }
  
  @Override
  public String executeUpdateWithKey(String sql) {
    verifyOpen();
    try {
      statement.executeUpdate(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
      ResultSet result = statement.getGeneratedKeys();
      if (result.next()) {
        return result.getString(1);
      } else {
        logger.log(Level.SEVERE, "Unable to read key in update: " + sql);
        return null;
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in update", e);
      logger.log(Level.SEVERE, sql);
      return null;
    }
  }
  
  @Override
  public QueryResult executeUpdateWithKeys(String sql) {
    verifyOpen();
    try {
      statement.executeUpdate(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
      return new QueryResultImpl(statement.getGeneratedKeys());
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in update", e);
      logger.log(Level.SEVERE, sql);
      return null;
    }
  }
  
  @Override
  public String executeSingle(String sql) {
    verifyOpen();
    try {
      statement.execute(sql);
      ResultSet result = statement.getResultSet();
      if (result.next()) {
        String retval = result.getString(1);
        if (result.next()) {
          logger.log(Level.SEVERE, "There should not be two results to " + sql);
        }
        return retval;
      } else {
        return null;
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in execute: " + sql, e);
      return null;
    } 
  }
  
  @Override
  public int exucuteSingleInt(String sql) {
    verifyOpen();
    try {
      statement.execute(sql);
      ResultSet result = statement.getResultSet();
      if (result.next()) {
        int retval = result.getInt(1);
        if (result.next()) {
          logger.log(Level.SEVERE, "There should not be two results to " + sql);
        }
        return retval;
      } else {
        logger.log(Level.SEVERE, "Unable to get value: " + sql);
        return 0;
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in execute: " + sql, e);
      return 0;
    } 
  }
  
  @Override
  public QueryResult execute(String sql) {
    try {
      statement.execute(sql);
      return new QueryResultImpl(statement.getResultSet());
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in execute", e);
      throw new RuntimeException(e);
    } 
  }
  
  @Override
  public void createTable(DatabaseTable table) {
    StringBuilder sql = new StringBuilder("CREATE TABLE ");
    sql.append(table.getName()).append(" (");
    for (Column<?> column : table.getColumns()) {
      sql.append(column.getName()).append(" ").append(column.getType()).append(',');
    }
    for (Column<?> column : table.getColumns()) {
      if (column.isIndexed()) {
        sql.append(column.getIndex()).append(',');
      }
    }
    sql.setCharAt(sql.length() - 1, ')');
    executeUpdate(sql.toString());
  }
  
  @Override
  public void createTables(DatabaseTable ... tables) {
    for (DatabaseTable table : tables) {
      createTable(table);
    }
  }
  
  @Override
  public void dropTables(DatabaseTable ... tables) {
    StringBuilder sql = new StringBuilder("DROP TABLE IF EXISTS ");
    for (DatabaseTable table : tables) {
      sql.append(table.getName()).append(',');
    }
    sql.setCharAt(sql.length() - 1, ';');
    executeUpdate(sql.toString());
  }
  
  @Override
  public void insert(Insert insert) {
    StringBuilder sql = new StringBuilder("INSERT INTO ");
    sql.append(insert.getTable().toSql()).append(" VALUES").append(insert.toString());
    executeUpdate(sql.toString());
  }
  
  @Override
  public Integer insertWithKey(Insert insert) {
    StringBuilder sql = new StringBuilder("INSERT INTO ");
    sql.append(insert.getTable().toSql()).append(" VALUES").append(insert.toString());
    return Integer.valueOf(executeUpdateWithKey(sql.toString()));
  }
  
  @Override
  public SetStatement createSetStatement(DatabaseTable table) {
    return new SetStatementImpl(this, table);
  }
  
  @Override
  public DeleteStatement createDeleteStatement(DatabaseTable table) {
    return new DeleteStatementImpl(this, table);
  }

  @Override
  public SelectQuery createSelectQuery(DatabaseTable table) {
    SelectQueryImpl select = new SelectQueryImpl(this);
    select.fromTable(table);
    return select;
  }
  
  @Override
  public SelectQuery createSelectQuery(ComparisonCondition ... joinConditions) {
    SelectQueryImpl select = new SelectQueryImpl(this);
    select.fromJoin(joinConditions);
    return select;
  }
}
