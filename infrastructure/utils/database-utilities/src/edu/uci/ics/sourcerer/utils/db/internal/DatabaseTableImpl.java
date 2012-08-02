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

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.uci.ics.sourcerer.util.BitEnumSet;
import edu.uci.ics.sourcerer.util.BitEnumSetFactory;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedTable;
import edu.uci.ics.sourcerer.utils.db.sql.StringColumn;
import edu.uci.ics.sourcerer.utils.db.sql.Table;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class DatabaseTableImpl implements Table {
  private String name;
  private List<Column<?>> columns;
  private boolean serial = false;
  
  protected DatabaseTableImpl(String name) {
    this.name = name;
    this.columns = new ArrayList<>();
  }
  
  public final StringColumn addVarcharColumn(String name, int size, boolean nullable) {
    StringColumn col = new StringColumnImpl(this, name, size, nullable); 
    columns.add(col);
    return col;
  }
  
  public final Column<Integer> addSerialColumn(String name) {
    if (columns.isEmpty()) {
      Column<Integer> col = new IntegerColumn(this, name, "SERIAL", false, true, Types.BIGINT);
      columns.add(col);
      serial = true;
      return col;
    } else {
      throw new IllegalStateException("Serial column must be first.");
    }
  }
  
  public final Column<Integer> addIDColumn(String name, boolean nullable) {
    Column<Integer> col = new IntegerColumn(this, name, "BIGINT", nullable, true, Types.BIGINT);
    columns.add(col);
    return col;
  }
  
  public final Column<Integer> addIntColumn(String name, boolean unsigned, boolean nullable) {
    Column<Integer> col = new IntegerColumn(this, name, "INT", nullable, unsigned, Types.INTEGER);
    columns.add(col);
    return col;
  }
  
  public final Column<Double> addDoubleColumn(String name, boolean nullable) {
    Column<Double> col = new DoubleColumn(this, name, nullable);
    columns.add(col);
    return col;
  }
  
  public final Column<Double> addDoubleColumn(String name, int totalDigits, int decimalDigits, boolean nullable) {
    Column<Double> col = new DoubleColumn(this, name, totalDigits, decimalDigits, nullable);
    columns.add(col);
    return col;
  }
  
  public final Column<Boolean> addBooleanColumn(String name, boolean nullable) {
    Column<Boolean> col = new BooleanColumn(this, name, nullable);
    columns.add(col);
    return col;
  }
  
  public final <T extends Enum<T>> Column<T> addEnumColumn(String name, T[] values, boolean nullable) {
    Column<T> col = new EnumColumn<>(this, name, values, nullable);
    columns.add(col);
    return col;
  }
  
  public final <T extends Enum<T>, S extends BitEnumSet<T>> Column<S> addSetColumn(String name, T[] values, BitEnumSetFactory<T, S> factory, boolean nullable) {
    Column<S> col = new SetColumn<>(this, name, values, factory, nullable);
    columns.add(col);
    return col;
  }
  
  protected final Insert createInsert(String ... values) {
    if (serial) {
      if (values.length != columns.size() - 1) {
        throw new IllegalArgumentException("Expected " + (columns.size() - 1) + " insert values, received " + Arrays.toString(values));
      } else {
        return InsertImpl.makeSerial(this, values);
      }
    } else if (values.length != columns.size()) {
      throw new IllegalArgumentException("Expected " + columns.size() + " insert values, received " + Arrays.toString(values));
    } else {
      return InsertImpl.create(this, values);
    }
  }
  
  public final String getName() {
    return name;
  }
 
  public final Collection<Column<?>> getColumns() {
    return columns;
  }
  
  @Override
  public final String toSql() {
    return name;
  }
  
  @Override
  public final QualifiedTable qualify(String qualifier) {
    return new QualifiedTableImpl(this, qualifier);
  }
}
