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
import java.util.Collection;

import edu.uci.ics.sourcerer.utils.db.internal.ConstantConditionImpl.Type;
import edu.uci.ics.sourcerer.utils.db.sql.ComparisonCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Condition;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.InConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedColumn;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedTable;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class QualifiedColumnImpl<T> implements QualifiedColumn<T> {
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
    StringBuilder builder = new StringBuilder();
    toSql(builder);
    return builder.toString();
  }

  @Override
  public ComparisonCondition compareEquals(Selectable<T> other) {
    return new ComparisonConditionImpl(this, other);
  }
  
  @Override
  public ConstantCondition<T> compareEquals() {
    return column.createConstantCondition(this, Type.EQUALS);
  }
  
  @Override
  public ConstantCondition<T> compareEquals(T value) {
    return column.createConstantCondition(this, Type.EQUALS).setValue(value);
  }
  
  @Override
  public ConstantCondition<T> compareNotEquals() {
    return column.createConstantCondition(this, Type.NOT_EQUALS);
  }
  
  @Override
  public ConstantCondition<T> compareNotEquals(T value) {
    return column.createConstantCondition(this, Type.NOT_EQUALS).setValue(value);
  }
  
  @Override
  public InConstantCondition<T> compareIn(Collection<T> values) {
    return new InConstantConditionImpl<T>(this, InConstantConditionImpl.Type.IN, values);
  }
  
  @Override
  public InConstantCondition<T> compareNotIn(Collection<T> values) {
    return new InConstantConditionImpl<T>(this, InConstantConditionImpl.Type.NOT_IN, values);
  }
  
  @Override
  public Condition compareNull() {
    if (!column.isNullable()) {
      throw new IllegalArgumentException(toString() + " is a non-nullable column");
    }
    return new NullCondition<>(this, NullCondition.Type.NULL);
  }
  
  @Override
  public Condition compareNotNull() {
    if (!column.isNullable()) {
      throw new IllegalArgumentException(toString() + " is a non-nullable column");
    }
    return new NullCondition<>(this, NullCondition.Type.NOT_NULL);
  }
  

  @Override
  public void toSql(StringBuilder builder) {
    builder.append(table.getQualifier()).append(".").append(column.getName());
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
  public void bind(T value, PreparedStatement statement, int index) throws SQLException {
    column.bind(value, statement, index);
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof QualifiedColumnImpl) {
      QualifiedColumnImpl<?> other = (QualifiedColumnImpl<?>) o;
      return column.equals(other.column) && table.equals(other.table);
    } else {
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    return 37 * column.hashCode() + table.hashCode(); 
  }
}