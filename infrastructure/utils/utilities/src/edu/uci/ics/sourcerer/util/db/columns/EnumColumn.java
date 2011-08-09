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
public abstract class EnumColumn<T extends Enum<T>> extends Column<T> {
  public EnumColumn(String name, String table, Enum<T>[] values, boolean nullable) {
    super(name, table, getEnumCreate(values, nullable), nullable);
  }
  
  private static <T extends Enum<T>> String getEnumCreate(Enum<T>[] values, boolean nullable) {
    StringBuilder builder = new StringBuilder();
    builder.append("ENUM(");
    for (Enum<T> value : values) {
      builder.append("'").append(value.name()).append("'").append(",");
    }
    builder.setCharAt(builder.length() - 1, ')');
    if (nullable) {
      builder.append(" NOT NULL");
    }
    return builder.toString();
  }

  @Override
  protected String convertHelper(T value) {
    return "'" + value.name() + "'";
  }
  
  @Override
  protected String equalsHelper(T value) {
    return "'" + value.name() + "'";
  }
}
