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

import edu.uci.ics.sourcerer.util.BitEnumSet;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SetConstantConditionImpl<T extends Enum<T>, S extends BitEnumSet<T>> extends ConstantConditionImpl<S> {
  SetConstantConditionImpl(Selectable<S> sel, Type type) {
    super(sel, type);
  }
  
  @Override
  public ConstantCondition<S> setValue(S value) {
    if (value.isEmpty()) {
      this.value = null;
    } else {
      this.value = value;
    }
    return this;
  }
  
  @Override
  public int bind(PreparedStatement statement, int index) throws SQLException {
    if (value.size() > 1) {
      return super.bind(statement, index);
    } else {
      statement.setString(index, value.iterator().next().name());
      return index + 1;
    }
  }
  
  @Override
  public void toSql(StringBuilder builder) {
    if (value.size() > 1) {
      super.toSql(builder);
    } else {
      builder.append("FIND_IN_SET(?,");
      sel.toSql(builder);
      builder.append(")");
      switch (type) {
        case EQUALS: builder.append(">0"); break;
        case NOT_EQUALS: builder.append("=0"); break;
        default: throw new IllegalStateException("Invalid type: " + type);
      }
    }
  }
}
