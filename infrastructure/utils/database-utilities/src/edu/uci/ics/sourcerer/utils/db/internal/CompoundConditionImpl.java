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

import edu.uci.ics.sourcerer.utils.db.sql.CompoundCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Condition;
import edu.uci.ics.sourcerer.utils.db.sql.Table;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class CompoundConditionImpl extends ConditionImpl implements CompoundCondition {
  enum Type {
    AND,
    OR;
  }
  
  private ConditionImpl left;
  private ConditionImpl right;
  private Type type;
  
  public CompoundConditionImpl(Condition left, Condition right, Type type) {
    this.left = (ConditionImpl) left;
    this.right = (ConditionImpl) right;
    this.type = type;
  }
  
  @Override
  public void verifyTables(Table ... tables) {
    left.verifyTables(tables);
    right.verifyTables(tables);
  }

  @Override
  public int bind(PreparedStatement statement, int index) throws SQLException {
    index = left.bind(statement, index);
    return right.bind(statement, index);
  }
  
  @Override
  public void toSql(StringBuilder builder) {
    if (type == Type.OR) {
      builder.append("((");
      left.toSql(builder);
      builder.append(") ").append(type.name()).append(" (");
      right.toSql(builder);
      builder.append("))");
    } else {
      left.toSql(builder);
      builder.append(" ").append(type.name()).append(" ");
      right.toSql(builder);
    }
  }
}
