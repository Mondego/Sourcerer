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

import edu.uci.ics.sourcerer.utils.db.sql.IColumn;
import edu.uci.ics.sourcerer.utils.db.sql.IComparisonCondition;
import edu.uci.ics.sourcerer.utils.db.sql.IQualifiedColumn;
import edu.uci.ics.sourcerer.utils.db.sql.IQualifiedTable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
abstract class Column <T> implements IColumn<T> {
  private InternalDatabaseTable table;
  private String name;
  private String type;
  private boolean nullable;
  private boolean indexed;  
  
  Column(InternalDatabaseTable table, String name, String type, boolean nullable) {
    this.table = table;
    this.name = name;
    this.type = type;
    this.nullable = nullable;
  }
  
  @Override
  public final String getName() {
    return name;
  }

  @Override
  public final String getType() {
    return type;
  }

  @Override
  public final IColumn<T> addIndex() {
    indexed = true;
    return this;
  }
  
  @Override
  public final boolean isIndexed() {
    return indexed;
  }

  @Override
  public final String getIndex() {
    if (indexed) {
      return "INDEX(" + name + ")";
    } else {
      throw new IllegalArgumentException(name + " is not indexed");
    }
  }

  @Override
  public final InternalDatabaseTable getTable() {
    return table;
  }
  
  @Override
  public IQualifiedColumn<T> qualify(IQualifiedTable table) {
    if (table.getQualifiedTable() == table) {
      return new QualifiedColumn<T>(this, table);
    } else {
      throw new IllegalArgumentException("This column is not for table " + table);
    }
  }
  
  @Override
  public IComparisonCondition compareEquality(IColumn<T> other) {
    return new ComparisonCondition(this, other);
  }
  
  @Override
  public String toSql() {
    return table.getName() + "." + name;
  }
  
  @Override
  public String toString() {
    return toSql();
  }
}
