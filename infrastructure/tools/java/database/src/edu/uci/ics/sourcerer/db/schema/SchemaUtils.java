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
package edu.uci.ics.sourcerer.db.schema;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class SchemaUtils {
  private SchemaUtils() {}
  
  public static String getSerialInsertValue(String... args) {
    StringBuilder builder = new StringBuilder("(NULL,");
    for (String arg : args) {
      builder.append(arg).append(',');
    }
    builder.setCharAt(builder.length() -1, ')');
    return builder.toString();
  }
  
  public static String getInsertValue(String... args) {
    StringBuilder builder = new StringBuilder("(");
    for (String arg : args) {
      builder.append(arg).append(',');
    }
    builder.setCharAt(builder.length() -1, ')');
    return builder.toString();
  }
  
  public static String convertNumber(String value) {
    if (value == null) {
      return "NULL";
    } else {
      return value;
    }
  }
  public static String convertNotNullNumber(String value) {
    if (value == null) {
      throw new NullPointerException();
    } else {
      return value;
    }
  }
      
  public static String convertVarchar(String value) {
    if (value == null) {
      return "NULL";
    } else {
      return "'" + value + "'";
    }
  }
  
  public static String convertNotNullVarchar(String value) {
    if (value == null) {
      throw new NullPointerException();
    } else {
      return "'" + value + "'";
    }
  }
  
  public static String convertOffset(String offset) {
    if ("-1".equals(offset)) {
      return convertNumber(null);
    } else {
      return convertNumber(offset);
    }
  }
  
  public static String convertLength(String length) {
    if ("0".equals(length)) {
      return convertNumber(null);
    } else {
      return convertNumber(length);
    }
  }
  
  public static <T extends Enum<T>> String getEnumCreate(Enum<T>[] values) {
    StringBuilder builder = new StringBuilder();
    builder.append("ENUM(");
    for (Enum<T> value : values) {
      builder.append("'").append(value.name()).append("'").append(",");
    }
    builder.setCharAt(builder.length() - 1, ')');
    return builder.toString();
  }
}
