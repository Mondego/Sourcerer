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

import edu.uci.ics.sourcerer.util.ArrayUtils;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;
import edu.uci.ics.sourcerer.utils.db.sql.Table;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class NullCondition<T> extends ConditionImpl {
  enum Type {
    NULL,
    NOT_NULL;
  }
  
  private Selectable<T> sel;
  private Type type;
  
  NullCondition(Selectable<T> sel, Type type) {
    this.sel = sel;
    this.type = type;
  }
  
  @Override 
  public void verifyTables(Table ... tables) {
    if (!ArrayUtils.containsReference(tables, sel.getTable())) {
      throw new IllegalStateException("Missing " + sel.getTable());
    }
  }
  
  @Override
  public void toSql(StringBuilder builder) {
    sel.toSql(builder);
    switch (type) {
      case NULL: builder.append(" IS NULL"); break;
      case NOT_NULL: builder.append(" IS NOT NULL"); break;
    }
  }
}
