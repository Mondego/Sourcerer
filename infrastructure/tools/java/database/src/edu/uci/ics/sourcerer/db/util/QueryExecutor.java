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
package edu.uci.ics.sourcerer.db.util;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.schema.DatabaseTable;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class QueryExecutor {
  private Connection connection;
  private Statement statement;

  public QueryExecutor(Connection connection) {
    this.connection = connection;
    reset();
  }
  
  public void close() {
    try {
      if (statement != null) {
        statement.close();
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Unable to close statement", e);
    }
  }
  
  public void closeConnection() {
    try {
      connection.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Unable to close connection", e);
    }
  }
  public void reset() {
    close();
    try {
      statement = connection.createStatement();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Unable to create statement", e);
    }
  }
  
  public TableLocker getTableLocker() {
    return new TableLocker(this);
  }
  
  public InsertBatcher getInsertBatcher(String table) {
    return new InsertBatcher(this, table);
  }
  
  public <T> KeyInsertBatcher<T> getKeyInsertBatcher(String table, KeyInsertBatcher.KeyProcessor<T> processor) {
    return new KeyInsertBatcher<T>(this, table, processor);
  }
  
  public InFileInserter getInFileInserter(File tempDir, String table) {
    return InFileInserter.getInFileInserter(tempDir, this, table);
  }
  
  public void executeUpdate(String sql) {
    try {
      statement.executeUpdate(sql);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in update: " + sql, e);
    }
  }
  
  public QueryResult executeUpdateWithKeys(String sql) {
    try {
      statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
      return new QueryResult(statement.getGeneratedKeys());
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in update", e);
      logger.log(Level.SEVERE, sql);
      throw new RuntimeException(e);
    }
  }
  
  public String executeUpdateWithKey(String sql) {
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
      throw new RuntimeException(e);
    }
  }
  
  public String getRowCount(String table) {
    return executeSingle("SELECT COUNT(*) FROM " + table + ";");
  }
  
  public void delete(String table, String where) {
    executeUpdate("DELETE FROM " + table + " WHERE " + where + ";");
  }
  
  public String selectSingle(String table, String column, String where) {
    return executeSingle("SELECT " + column + " FROM " + table + " WHERE " + where + ";");
  }
  
  public <T> T selectSingle(String table, String column, String where, ResultTranslator<T> translator) {
    return executeSingle("SELECT " + column + " FROM " + table + " WHERE " + where + ";", translator);
  }
  
  public Collection<String> select(String table, String columns, String where) {
    return select(table, columns, where, ResultTranslator.SIMPLE_RESULT_TRANSLATOR);
  }
  
  public <T> Collection<T> select(String table, String columns, String where, ResultTranslator<T> translator) {
    return execute("SELECT " + columns + " FROM " + table + " WHERE " + where + ";", translator);
  }
  
  public <T> Collection<T> select(String where, ResultTranslator<T> translator) {
    return execute("SELECT " + translator.getSelect() + " FROM " + translator.getTable() + " WHERE " + where + ";", translator);
  }
  
  public <T> Iterable<T> selectStreamed(String where, ResultTranslator<T> translator) {
    return executeStreamed("SELECT " + translator.getSelect() + " FROM " + translator.getTable() + (where == null ? "" : (" WHERE " + where)) + ";", translator);
  }
    
  public void insertSingle(String table, String value) {
    executeUpdate("INSERT INTO " + table + " VALUES " + value + ";");
  }
  
  public String insertSingleWithKey(String table, String value) {
    return executeUpdateWithKey("INSERT INTO " + table + " VALUES " + value + ";");
  }
  
  public void dropTables(DatabaseTable... tables) {
    StringBuilder sql = new StringBuilder("DROP TABLE IF EXISTS ");
    for (DatabaseTable table : tables) {
      sql.append(table.getName()).append(',');
    }
    sql.setCharAt(sql.length() - 1, ';');
    executeUpdate(sql.toString());
  }
  
  public void createTable(String table, String... args) {
    StringBuilder sql = new StringBuilder("CREATE TABLE ");
    sql.append(table).append(" (");
    for (String arg : args) {
      sql.append(arg).append(',');
    }
    sql.setCharAt(sql.length() - 1, ')');
    executeUpdate(sql.toString());
  }
  
  public <T> IterableResult<T> executeStreamed(String sql, ResultTranslator<T> translator) {
    try {
      Statement streamingStatement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      streamingStatement.setFetchSize(Integer.MIN_VALUE);
      streamingStatement.execute(sql);
      return IterableResult.getResultIterable(streamingStatement.getResultSet(), translator);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in execute", e);
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
  
  public <T> Collection<T> execute(String sql, ResultTranslator<T> translator) {
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
  
  public String executeSingle(String sql) {
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
      throw new RuntimeException(e);
    } 
  }
  
  public <T> T executeSingle(String sql, ResultTranslator<T> translator) {
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
  
  public int executeSingleInt(String sql) {
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
      throw new RuntimeException(e);
    } 
  }
}
