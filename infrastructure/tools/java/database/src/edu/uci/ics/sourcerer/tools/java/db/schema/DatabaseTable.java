///* 
// * Sourcerer: an infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
//
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.uci.ics.sourcerer.tools.java.db.schema;
//
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//
//import java.io.File;
//import java.util.logging.Level;
//import java.util.regex.Pattern;
//
//import edu.uci.ics.sourcerer.db.util.InFileInserter;
//import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
//import edu.uci.ics.sourcerer.db.util.QueryExecutor;
//import edu.uci.ics.sourcerer.db.util.TableLocker;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public abstract class DatabaseTable {
//  protected final QueryExecutor executor;
//  protected final TableLocker locker;
//  protected final String table;
//  
//  protected InFileInserter inserter;
//  
//  protected DatabaseTable(QueryExecutor executor, TableLocker locker, String table) {
//    this.executor = executor;
//    this.locker = locker;
//    this.table = table;
//  }
//  
//  public String getName() {
//    return table;
//  }
//  
//  // ---- CREATE ----
//  public abstract void createTable();
//  
//  // ---- INSERT ----
//  public <T> KeyInsertBatcher<T> getKeyInsertBatcher(KeyInsertBatcher.KeyProcessor<T> processor) {
//    return executor.getKeyInsertBatcher(table, processor);
//  }
//  
//  public void initializeInserter(File tempDir) {
//    inserter = executor.getInFileInserter(tempDir, table);
//  }
//  
//  public void flushInserts() {
//    if (inserter == null) {
//      throw new IllegalStateException("This table does not have flushable inserts");
//    } else {
//      inserter.insert();
//      inserter = null;
//    }
//  }
//
//  // ---- STATIC UTILITIES ----
//  protected static String buildSerialInsertValue(String ... args) {
//    StringBuilder builder = new StringBuilder("(NULL,");
//    for (String arg : args) {
//      builder.append(arg).append(',');
//    }
//    builder.setCharAt(builder.length() -1, ')');
//    return builder.toString();
//  }
//  
//  protected static String buildInsertValue(String... args) {
//    StringBuilder builder = new StringBuilder("(");
//    for (String arg : args) {
//      builder.append(arg).append(',');
//    }
//    builder.setCharAt(builder.length() -1, ')');
//    return builder.toString();
//  }
//  
//  private static Pattern number = Pattern.compile("\\d+");
//  protected static String convertNumber(String value) {
//    if (value == null) {
//      return "NULL";
//    } else if (!number.matcher(value).matches()) {
//      logger.log(Level.SEVERE, value + " is not a number");
//      return "NULL";
//    } else {
//      return value;
//    }
//  }
//  
//  protected static String convertNotNullNumber(String value) {
//    if (value == null || !number.matcher(value).matches()) {
//      throw new IllegalArgumentException(value + " is not a number");
//    } else {
//      return value;
//    }
//  }
//  
//  private static Pattern decimalNumber =  Pattern.compile("(-{0,1})((\\d+)|(\\d+[.]\\d+))");
//  protected static String convertDecimalNumber(String value) {
//    if (value == null) {
//      return "NULL";
//    } else if (!decimalNumber.matcher(value).matches()) {
//      logger.log(Level.SEVERE, value + " is not a decimal number");
//      return "NULL";
//    } else {
//      return value;
//    }
//  }
//  
//  protected static String convertNotNullDecimalNumber(String value) {
//    if (value == null || !decimalNumber.matcher(value).matches()) {
//      throw new IllegalArgumentException(value + " is not a decimal number");
//    } else {
//      return value;
//    }
//  }
//  
//  protected static String convertOffset(String offset) {
//    if ("-1".equals(offset)) {
//      return convertNumber(null);
//    } else {
//      return convertNumber(offset);
//    }
//  }
//  
//  protected static String convertLength(String length) {
//    if ("0".equals(length)) {
//      return convertNumber(null);
//    } else {
//      return convertNumber(length);
//    }
//  }
//  
//  protected static String convertVarchar(String value) {
//    if (value == null) {
//      return "NULL";
//    } else {
//      return "'" + value + "'";
//    }
//  }
//  
//  protected static String convertNotNullVarchar(String value) {
//    if (value == null) {
//      throw new IllegalArgumentException("varchar may not be null");
//    } else {
//      return "'" + value + "'";
//    }
//  }
//  
//  protected static String convertBoolean(Boolean bool) {
//    if (bool == null) {
//      return "NULL";
//    } else {
//      return bool ? "TRUE" : "FALSE";
//    }
//  }
//  
//  protected static String convertNotNullBoolean(Boolean bool) {
//    if (bool == null) {
//      throw new IllegalArgumentException("bool may not be null");
//    } else {
//      return bool ? "TRUE" : "FALSE";
//    }
//  }
//}
