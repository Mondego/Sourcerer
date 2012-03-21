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

import edu.uci.ics.sourcerer.util.ArrayUtils;
import edu.uci.ics.sourcerer.utils.db.sql.InConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;
import edu.uci.ics.sourcerer.utils.db.sql.Table;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class InConstantConditionImpl<T> extends ConditionImpl implements InConstantCondition<T> {
  enum Type {
    IN,
    NOT_IN;
  }
  
  private Selectable<T> sel;
  private Type type;
  private Collection<T> values;
  
  public InConstantConditionImpl(Selectable<T> sel, Type type, Collection<T> values) {
    this.sel = sel;
    this.type = type;
    this.values = values;
    if (values.isEmpty()) {
      throw new IllegalArgumentException("Has to be in/not in something!");
    }
  }
  
  @Override 
  public void verifyTables(Table ... tables) {
    if (!ArrayUtils.containsReference(tables, sel.getTable())) {
      throw new IllegalStateException("Missing " + sel.getTable());
    }
  }
  
  @Override
  public Collection<T> getValues() {
    return values;
  }
  
  @Override
  public void toSql(StringBuilder builder) {
    sel.toSql(builder);
    switch (type) {
      case IN: builder.append(" IN ("); break;
      case NOT_IN: builder.append(" NOT IN ("); break;
    }
    for (T value : values) {
      builder.append(sel.to(value)).append(',');
    }
    builder.setCharAt(builder.length() - 1, ')');
  }
}
