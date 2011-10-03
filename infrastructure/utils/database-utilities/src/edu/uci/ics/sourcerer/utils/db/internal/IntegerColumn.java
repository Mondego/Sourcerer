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


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class IntegerColumn extends ColumnImpl<Integer> {
  private boolean unsigned;
  
  IntegerColumn(DatabaseTableImpl table, String name, String type, boolean nullable, boolean unsigned, int sqlType) {
    super(table, name, type + ("SERIAL".equals(type) ? "" : ((unsigned ? " UNSIGNED" : "") + (nullable ? "" : " NOT NULL"))), nullable, sqlType);
    this.unsigned = unsigned;
  }
  
  @Override
  protected void bindHelper(Integer value, PreparedStatement statement, int index) throws SQLException {
    if (unsigned && value < 0) {
      throw new IllegalArgumentException(getName() + " does not accept negative values, such as " + value);
    } else {
      statement.setInt(index, value);
    }
  }
  
  @Override
  public Integer fromHelper(String value) {
    return Integer.valueOf(value);
  }
  
  @Override
  public String toHelper(Integer value) {
    return value.toString();
  }
}
