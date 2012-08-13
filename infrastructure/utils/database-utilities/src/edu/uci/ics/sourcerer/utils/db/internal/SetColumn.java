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

import edu.uci.ics.sourcerer.util.BitEnumSet;
import edu.uci.ics.sourcerer.util.BitEnumSetFactory;
import edu.uci.ics.sourcerer.utils.db.internal.ConstantConditionImpl.Type;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class SetColumn<T extends Enum<T>, S extends BitEnumSet<T>> extends ColumnImpl<S> {
  private BitEnumSetFactory<T, S> factory;
  
  SetColumn(DatabaseTableImpl table, String name, T[] values, BitEnumSetFactory<T, S> factory, boolean nullable) {
    super(table, name, makeType(values, nullable), nullable, Types.INTEGER);
    this.factory = factory;
  }
  
  private static <T extends Enum<T>> String makeType(T[] values, boolean nullable) {
    StringBuilder builder = new StringBuilder();
    builder.append("SET(");
    for (Enum<T> value : values) {
      builder.append("'").append(value.name()).append("'").append(",");
    }
    builder.setCharAt(builder.length() - 1, ')');
    if (!nullable) {
      builder.append(" NOT NULL");
    }
    return builder.toString();
  }
  
  @Override
  protected ConstantCondition<S> createConstantCondition(Selectable<S> sel, Type type) {
    return new SetConstantConditionImpl<>(sel, type);
  }
  
  @Override
  protected void bindHelper(S value, PreparedStatement statement, int index) throws SQLException {
    StringBuilder builder = new StringBuilder('\'');
    for (T val : value) {
      builder.append(val.name()).append(',');
    }
    builder.setCharAt(builder.length() - 1, '\'');
    statement.setString(index, builder.toString());
  }
  
  @Override
  public void toSql(StringBuilder builder) {
    super.toSql(builder);
    builder.append("+0");
  }
  
  @Override
  public S fromHelper(String value) {
    return factory.make(Integer.valueOf(value));
  }

  @Override
  protected String toHelper(S value) {
    return Integer.toString(value.getValue());
  }
}
