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

import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Table;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class InsertImpl implements Insert {
  private final Table table;
  private final String value;
  
  private InsertImpl(Table table, String value) {
    this.table = table;
    this.value = value;
  }
  
  static InsertImpl create(Table table, String ... values) {
    StringBuilder builder = new StringBuilder("(");
    for (String value : values) {
      builder.append(value).append(',');
    }
    builder.setCharAt(builder.length() - 1, ')');
    return new InsertImpl(table, builder.toString());
  }
  
  static InsertImpl makeSerial(Table table, String ... values) {
    StringBuilder builder = new StringBuilder("(NULL,");
    for (String value : values) {
      builder.append(value).append(',');
    }
    builder.setCharAt(builder.length() - 1, ')');
    return new InsertImpl(table, builder.toString());
  }
  
  @Override
  public Table getTable() {
    return table;
  }
  
  @Override
  public String toString() {
    return value;
  }
}
