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

import edu.uci.ics.sourcerer.utils.db.internal.ConstantConditionImpl.Type;
import edu.uci.ics.sourcerer.utils.db.sql.BindVariable;
import edu.uci.ics.sourcerer.utils.db.sql.ComparisonCondition;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.InConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedColumn;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedTable;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class QualifiedColumnImpl<T> implements QualifiedColumn<T> {
  private ColumnImpl<T> column;
  private QualifiedTable table;
  
  QualifiedColumnImpl(ColumnImpl<T> column, QualifiedTable table) {
    this.column = column;
    this.table = table;
  }
   
  @Override
  public QualifiedTable getTable() {
    return table;
  }
  
  @Override
  public String toString() {
    return toSql();
  }

  @Override
  public ComparisonCondition compareEquals(Selectable<T> other) {
    return new ComparisonConditionImpl(this, other);
  }
  
  @Override
  public ConstantCondition<T> compareEquals() {
    return new ConstantConditionImpl<>(this, Type.EQUALS);
  }
  
  @Override
  public ConstantCondition<T> compareNotEquals() {
    return new ConstantConditionImpl<>(this, Type.NOT_EQUALS);
  }
  
  @Override
  public InConstantCondition<T> compareIn(Collection<T> values) {
    return new InConstantConditionImpl<T>(this, InConstantConditionImpl.Type.IN, values);
  }

  @Override
  public String toSql() {
    return table.getQualifier() + "." + column.getName();
  }
  
  @Override
  public String to(T value) {
    return column.to(value);
  }
  
  @Override
  public T from(String value) {
    return column.from(value);
  }


  @Override
  public BindVariable bind(T value) {
    return column.bind(value);
  }
}