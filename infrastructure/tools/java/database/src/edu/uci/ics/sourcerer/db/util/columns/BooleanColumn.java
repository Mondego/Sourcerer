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
public class BooleanColumn extends Column<Boolean> {
  private BooleanColumn(String name, String table, String type, boolean nullable) {
    super(name, table, type, nullable);
  }

  public static Column<Boolean> getBoolean(String name, String table) {
    return new BooleanColumn(name, table, "BOOLEAN", true);
  }
  
  public static Column<Boolean> getBooleanNotNull(String name, String table) {
    return new BooleanColumn(name, table, "BOOLEAN NOT NULL", false);
  }
  
  @Override
  public Boolean convertFromDB(String value) {
    if (value == null) {
      return null; 
    } else {
      return Boolean.valueOf(value);
    }
  }

  @Override
  protected String convertHelper(Boolean value) {
    return value.toString();
  }
  
  @Override
  protected String equalsHelper(Boolean value) {
    return value.toString();
  }
}
