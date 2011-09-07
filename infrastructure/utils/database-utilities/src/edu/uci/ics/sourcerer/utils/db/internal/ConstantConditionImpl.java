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

import edu.uci.ics.sourcerer.utils.db.sql.BindVariable;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;
import edu.uci.ics.sourcerer.utils.db.sql.Table;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class ConstantConditionImpl<T> implements ConstantCondition<T> {
  enum Type {
    EQUALS,
    NOT_EQUALS,
    LIKE;
  }
  
  private Selectable<T> sel;
  private Type type;
  
  ConstantConditionImpl(Selectable<T> sel, Type type) {
    this.sel = sel;
    this.type = type;
  }

  @Override
  public Table getTable() {
    return sel.getTable();
  }
  
  @Override
  public BindVariable bind(T value) {
    return sel.bind(value);
  }

  @Override
  public String toSql() {
    switch (type) {
      case EQUALS: return sel.toSql() + "=?";
      case NOT_EQUALS: return sel.toSql() + "<>?";
      case LIKE: return sel.toSql() + " like ?";
      default: throw new IllegalStateException("Unknown type: " + type);
    }
  }
}
