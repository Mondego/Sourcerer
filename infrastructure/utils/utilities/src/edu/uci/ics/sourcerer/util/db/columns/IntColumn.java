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
package edu.uci.ics.sourcerer.util.db.columns;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class IntColumn extends Column<Integer> {
  private boolean unsigned;

  private IntColumn(String name, String table, String type, boolean nullable, boolean unsigned) {
    super(name, table, type, nullable);
    this.unsigned = unsigned;
  }
  
  public static Column<Integer> getSerial(String name, String table) {
    return new IntColumn(name, table, "SERIAL", false, true).addIndex();
  }
  
  public static IntColumn getOptionalID(String name, String table) {
    return new IntColumn(name, table, "BIGINT UNSIGNED", true, true);
  }
  
  public static IntColumn getID(String name, String table) {
    return new IntColumn(name, table, "BIGINT UNSIGNED NOT NULL", false, true);
  }
  
  public static IntColumn getUnsignedInt(String name, String table) {
    return new IntColumn(name, table, "INT UNSIGNED", true, true);
  }
  
  public static IntColumn getUnsignedIntNotNull(String name, String table) {
    return new IntColumn(name, table, "INT UNSIGNED NOT NULL", false, true);
  }

  @Override
  public Integer convertFromDB(String value) {
    if (value == null) {
      return null;
    } else {
      return Integer.valueOf(value);
    }
  }

  @Override
  protected String convertHelper(Integer value) {
    if (unsigned && value.intValue() < 0) {
      throw new IllegalArgumentException(getName() + " should be > 0, not " + value);
    } else {
      return value.toString();
    }
  }
  
  @Override
  protected String equalsHelper(Integer value) {
    return value.toString();
  }
}
