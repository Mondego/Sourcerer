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
package edu.uci.ics.sourcerer.db.util.columns;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class StringColumn extends Column<String> {
  private int indexCharCount = 0;
  
  private StringColumn(String name, String table, String type, boolean nullable) {
    super(name, table, type, nullable);
  }
  
  public static StringColumn getVarchar32(String name, String table) {
    return new StringColumn(name, table, "VARCHAR(32)", true);
  }
  
  public static StringColumn getVarchar1024(String name, String table) {
    return new StringColumn(name, table, "VARCHAR(1024)", true);
  }
  
  public static StringColumn getVarchar1024NotNull(String name, String table) {
    return new StringColumn(name, table, "VARCHAR(1024) BINARY NOT NULL", false);
  }
  
  public static StringColumn getVarchar2048NotNull(String name, String table) {
    return new StringColumn(name, table, "VARCHAR(2048) BINARY NOT NULL", false);
  }
  
  public static StringColumn getVarchar4096(String name, String table) {
    return new StringColumn(name, table, "VARCHAR(4096) BINARY", true);
  }
  
  public static StringColumn getVarchar8192NotNull(String name, String table) {
    return new StringColumn(name, table, "VARCHAR(8192) BINARY NOT NULL", true);
  }
  
  public StringColumn addIndex(int indexCharCount) {
    this.indexCharCount = indexCharCount;
    return this;
  }
  
  @Override
  public String getIndex() {
    if (isIndexed()) {
      if (indexCharCount == 0) {
        return "INDEX(" + getName() + ")";
      } else {
        return "INDEX(" + getName() + "(" + indexCharCount + "))";
      }
    } else {
      throw new IllegalArgumentException(getName() + " is not indexed");
    }
  }

  @Override
  public String getLike(String value) {
    return getName() + " LIKE '" + value + "'";
  }
  
  @Override
  public String convertFromDB(String value) {
    return value;
  }

  @Override
  public String convertHelper(String value) {
    return value;
  }
  
  @Override
  protected String equalsHelper(String value) {
    return "'" + value + "'";
  }
}
