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

import edu.uci.ics.sourcerer.utils.db.sql.Assignment;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class AssignmentImpl <T> implements Assignment<T> {
  private final Selectable<T> sel;
  private T value;

  AssignmentImpl(Selectable<T> sel) {
    this.sel = sel;
  }
  
  @Override
  public void setValue(T value) {
    this.value = value;
  }

  public int bind(PreparedStatement statement, int index) throws SQLException {
    sel.bind(value, statement, index);
    return index + 1;
  }

  public void toSql(StringBuilder builder) {
    sel.toSql(builder);
    builder.append("=?");
  }
}
