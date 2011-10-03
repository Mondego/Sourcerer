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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class EnumColumn<T extends Enum<T>> extends ColumnImpl<T> {
  private Class<T> klass;
  
  EnumColumn(DatabaseTableImpl table, String name, T[] values, boolean nullable) {
    super(table, name, makeType(values, nullable), nullable, Types.INTEGER);
    klass = values[0].getDeclaringClass();
  }

  private static <T extends Enum<T>> String makeType(T[] values, boolean nullable) {
    StringBuilder builder = new StringBuilder();
    builder.append("ENUM(");
		for (T value : values) {
		  builder.append('\'').append(value.name()).append("',");
		}
    builder.setCharAt(builder.length() - 1, ')');
    if (nullable) {
      builder.append(" NOT NULL");
    }
    return builder.toString();
  }
  
  @Override
  protected void bindHelper(T value, PreparedStatement statement, int index) throws SQLException {
    statement.setString(index, value.name());
  }
  
  @Override
  public T fromHelper(String value) {
    return Enum.valueOf(klass, value);
  }
  
  @Override
  public String toHelper(T value) {
    return "'" + value.name() + "'";
  }
}
