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

import java.util.Collection;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.utils.db.sql.IColumn;
import edu.uci.ics.sourcerer.utils.db.sql.ITable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class InternalDatabaseTable implements ITable {
  private String name;
  private Collection<IColumn<?>> columns;
  
  protected InternalDatabaseTable(String name) {
    this.name = name;
    this.columns = Helper.newArrayList();
  }
  
  public final IColumn<String> addVarcharColumn(String name, int size, boolean nullable) {
    IColumn<String> col = new StringColumn(this, name, size, nullable); 
    columns.add(col);
    return col;
  }
  
  public final String getName() {
    return name;
  }
 
  public final Collection<IColumn<?>> getColumns() {
    return columns;
  }
  
  @Override
  public final String toSql() {
    return name;
  }
}
