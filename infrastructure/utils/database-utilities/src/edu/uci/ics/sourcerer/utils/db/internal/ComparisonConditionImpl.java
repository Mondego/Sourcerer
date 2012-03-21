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
import edu.uci.ics.sourcerer.utils.db.sql.ComparisonCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;
import edu.uci.ics.sourcerer.utils.db.sql.Table;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class ComparisonConditionImpl extends ConditionImpl implements ComparisonCondition {
  private Selectable<?> left;
  private Selectable<?> right;
  
  <T> ComparisonConditionImpl(Selectable<T> left, Selectable<T> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public Table getLeftTable() {
    return left.getTable();
  }

  @Override
  public Table getRightTable() {
    return right.getTable();
  }
  
  @Override
  public void verifyTables(Table ... tables) {
    if (!ArrayUtils.containsReference(tables, left.getTable())) {
      throw new IllegalStateException("Missing " + left.getTable());
    }
    if (!ArrayUtils.containsReference(tables, right.getTable())) {
      throw new IllegalStateException("Missing " + right.getTable());
    }
  }
  
  @Override
  public void toSql(StringBuilder builder) {
    left.toSql(builder);
    builder.append("=");
    right.toSql(builder);
  }
}
