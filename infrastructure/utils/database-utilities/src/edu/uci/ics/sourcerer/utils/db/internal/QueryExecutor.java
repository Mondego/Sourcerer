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

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.db.columns.Column;
import edu.uci.ics.sourcerer.utils.db.DatabaseException;
import edu.uci.ics.sourcerer.utils.db.IColumn;
import edu.uci.ics.sourcerer.utils.db.IQueryExecutor;
import edu.uci.ics.sourcerer.utils.db.IQueryResult;
import edu.uci.ics.sourcerer.utils.db.IRowInserter;
import edu.uci.ics.sourcerer.utils.db.ITableLocker;
import edu.uci.ics.sourcerer.utils.db.sql.ISelectFromClause;
import edu.uci.ics.sourcerer.utils.db.sql.ISetClause;
import edu.uci.ics.sourcerer.utils.db.sql.ITable;
import edu.uci.ics.sourcerer.utils.db.sql.IWhereClause;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class QueryExecutor implements IQueryExecutor {
  private Connection connection;
  private Statement statement;
  private TableLocker locker;

  private QueryExecutor(Connection connection) {
    this.connection = connection;
  }
  
  static /**
   * 
   */
  public QueryExecutor make(Connection connection) {
    QueryExecutor exec = new QueryExecutor(connection);
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
  public ITableLocker getTableLocker() {
    if (locker == null) {
      locker = new TableLocker(this);
    }
    return locker;
  }
  
  @Override
  public IRowInserter getInFileInserter(File tempDir, ITable table) {
    return InFileInserter.getInFileInserter(tempDir, this, table);
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
  public IQueryResult executeUpdateWithKeys(String sql) {
    verifyOpen();
    try {
      statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
      return new QueryResult(statement.getGeneratedKeys());
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in update", e);
      logger.log(Level.SEVERE, sql);
      return null;
    }
  }
  
  @Override
  public String executeUpdateWithKey(String sql) {
    verifyOpen();
    try {
      statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
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
  public void executeUpdate(ITable table, ISetClause set, IWhereClause where) {
    executeUpdate("UPDATE " + table + " SET " + set + " WHERE " + where + ";");
  }
    
  @Override
  public void deleteRows(ITable table, IWhereClause where) {
    executeUpdate("DELETE FROM " + table + " WHERE " + where + ";");
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
  
  
  public int executeSingleInt(String sql) {
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
  public int getRowCount(ITable table, IWhereClause where) {
    return executeSingleInt("SELECT COUNT(*) FROM " + table + " WHERE " + where + ";");
  }
  
  @Override
  public <T> T selectSingle(ITable table, IColumn<T> column, IWhereClause where) {
    return column.from(executeSingle("SELECT " + column + " FROM " + table + " WHERE " + where + ";"));
  }
  
  @Override
  public <T> Collection<T> select(ITable table, ISelectFromClause from, IWhereClause where) {
    return execute("SELECT " + columns + " FROM " + table + " WHERE " + where + ";", translator);
  }
  
  public <T> Collection<T> select(String where, ResultTranslator<T> translator) {
    return execute("SELECT " + translator.getSelect() + " FROM " + translator.getTable() + " WHERE " + where + ";", translator);
  }
  
  public <T> Iterable<T> selectStreamed(String where, ResultTranslator<T> translator) {
    return executeStreamed("SELECT " + translator.getSelect() + " FROM " + translator.getTable() + (where == null ? "" : (" WHERE " + where)) + ";", translator);
  }
  
  public <T> Iterable<T> selectStreamed(String table, String columns, String where, BasicResultTranslator<T> translator) {
    return executeStreamed("SELECT " + columns + " FROM " + table + (where == null ? "" : (" WHERE " + where)) + ";", translator);
  }
    
  public void insertSingle(String table, String value) {
    executeUpdate("INSERT INTO " + table + " VALUES " + value + ";");
  }
  
  public Integer insertSingleWithKey(String table, String value) {
    String val = executeUpdateWithKey("INSERT INTO " + table + " VALUES " + value + ";");
    if (val == null) {
      return null;
    } else {
      try {
        return Integer.valueOf(val);
      } catch (NumberFormatException e) {
        logger.log(Level.SEVERE, "Unable to understand key value " + val + " for " + value);
        return null;
      }
    }
  }
  
  public void dropTables(DatabaseTable... tables) {
    StringBuilder sql = new StringBuilder("DROP TABLE IF EXISTS ");
    for (DatabaseTable table : tables) {
      sql.append(table.getName()).append(',');
    }
    sql.setCharAt(sql.length() - 1, ';');
    executeUpdate(sql.toString());
  }
  
  public void createTable(String table, Column<?>... columns) {
    StringBuilder sql = new StringBuilder("CREATE TABLE ");
    sql.append(table).append(" (");
    for (Column<?> column : columns) {
      sql.append(column.getName()).append(" ").append(column.getType()).append(',');
    }
    for (Column<?> column : columns) {
      if (column.isIndexed()) {
        sql.append(column.getIndex()).append(',');
      }
    }
    sql.setCharAt(sql.length() - 1, ')');
    executeUpdate(sql.toString());
  }
  
  public <T> IterableResult<T> executeStreamed(String sql, BasicResultTranslator<T> translator) {
    try {
      Statement streamingStatement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      streamingStatement.setFetchSize(Integer.MIN_VALUE);
      streamingStatement.execute(sql);
      return IterableResult.getResultIterable(streamingStatement.getResultSet(), translator);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in execute of " + sql, e);
      throw new RuntimeException(e);
    } 
  }
  
  public QueryResult execute(String sql) {
    try {
      statement.execute(sql);
      return new QueryResult(statement.getResultSet());
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in execute", e);
      throw new RuntimeException(e);
    } 
  }
  
  public <T> Collection<T> execute(String sql, BasicResultTranslator<T> translator) {
    try {
      statement.execute(sql);
      ResultSet result = statement.getResultSet();
      
      Collection<T> collection = Helper.newLinkedList();
      while (result.next()) {
        collection.add(translator.translate(result));
      }
      return collection;
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in execute", e);
      throw new RuntimeException(e);
    } 
  }
  

  
  public <T> T executeSingle(String sql, BasicResultTranslator<T> translator) {
    try {
      statement.execute(sql);
      ResultSet result = statement.getResultSet();
      if (result.next()) {
        T retval = translator.translate(result);
        if (result.next()) {
          logger.log(Level.SEVERE, "There should not be two results to " + sql);
        }
        return retval;
      } else {
        return null;
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in execute: " + sql, e);
      throw new RuntimeException(e);
    } 
  }

}
