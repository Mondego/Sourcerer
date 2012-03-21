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

import edu.uci.ics.sourcerer.util.ArrayUtils;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;
import edu.uci.ics.sourcerer.utils.db.sql.Table;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class ConstantConditionImpl<T> extends ConditionImpl implements ConstantCondition<T> {
  enum Type {
    EQUALS,
    NOT_EQUALS,
    LIKE;
  }
  
  protected Selectable<T> sel;
  protected Type type;
  protected T value;
  
  ConstantConditionImpl(Selectable<T> sel, Type type) {
    this.sel = sel;
    this.type = type;
  }

  @Override
  public void verifyTables(Table ... tables) {
    if (!ArrayUtils.containsReference(tables, sel.getTable())) {
      throw new IllegalStateException("Missing " + sel.getTable());
    }
  }
  
  @Override
  public ConstantCondition<T> setValue(T value) {
    this.value = value;
    return this;
  }
  
  @Override
  public int bind(PreparedStatement statement, int index) throws SQLException {
    sel.bind(value, statement, index);
    return index + 1;
  }

  @Override
  public void toSql(StringBuilder builder) {
    sel.toSql(builder);
    switch (type) {
      case EQUALS: builder.append("=?"); break;
      case NOT_EQUALS: builder.append("<>?"); break;
      case LIKE: builder.append(" like ?"); break;
      default: throw new IllegalStateException("Unknown type: " + type);
    }
  }
}
