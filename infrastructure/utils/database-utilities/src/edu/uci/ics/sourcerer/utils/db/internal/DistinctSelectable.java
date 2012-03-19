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

import edu.uci.ics.sourcerer.utils.db.sql.Selectable;
import edu.uci.ics.sourcerer.utils.db.sql.Table;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class DistinctSelectable<T> implements Selectable<T> {
  private final Selectable<T> selectable;
  
  private DistinctSelectable(Selectable<T> selectable) {
    this.selectable = selectable;
  }
  
  static <T> DistinctSelectable<T> create(Selectable<T> selectable) {
    return new DistinctSelectable<>(selectable);
  }
  
  @Override
  public Table getTable() {
    return selectable.getTable();
  }

  @Override
  public void toSql(StringBuilder builder) {
    builder.append("DISTINCT ");
    selectable.toSql(builder);
  }

  @Override
  public T from(String value) {
    return selectable.from(value);
  }
  
  @Override
  public void bind(T value, PreparedStatement statement, int index) throws SQLException {
    selectable.bind(value, statement, index);
  }
}
