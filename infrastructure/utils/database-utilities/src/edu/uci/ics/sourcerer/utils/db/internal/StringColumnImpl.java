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
import java.sql.Types;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.internal.ConstantConditionImpl.Type;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.StringColumn;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class StringColumnImpl extends ColumnImpl<String> implements StringColumn {
  private final int maxSize;
  private int indexedCharCount;
  
  StringColumnImpl(DatabaseTableImpl table, String name, int size, boolean nullable) {
    super(table, name, "VARCHAR(" + size + ") BINARY" + (nullable ? "" : " NOT NULL"), nullable, Types.VARCHAR);
    maxSize = size;
  }

  @Override
  public ConstantCondition<String> compareLike() {
    return new ConstantConditionImpl<>(this, Type.LIKE);
  }
  
  @Override
  protected void bindHelper(String value, PreparedStatement statement, int index) throws SQLException {
    statement.setString(index, value);
  }
  
  @Override
  protected String fromHelper(String value) {
    return value;
  }
  
  @Override
  public String truncate(String value) {
    if (value.length() > maxSize) {
      return value.substring(0, maxSize);
    } else {
      return value;
    }
  }
  
  @Override
  protected String toHelper(String value) {
    value = value.replace("\\", "\\\\").replace("'", "\\'");
    if (value.length() > maxSize) {
      String trunc = value.substring(0, maxSize);
      TaskProgressLogger.get().report(Level.WARNING, "Forced to truncate " + toString() + " to " + trunc);
      return "'" + truncate(value) + "'"; 
    } else {
      return "'" + value + "'";
    }
  }
  
  @Override 
  public StringColumn addIndex(int numChars) {
    indexedCharCount = numChars;
    addIndex();
    return this;
  }
  
  @Override
  public String getIndex() {
    if (isIndexed()) {
      if (indexedCharCount == 0) {
        return super.getIndex();
      } else {
        return "INDEX(" + getName() + "(" + indexedCharCount + "))";
      }
    } else {
      throw new IllegalArgumentException(getName() + " is not indexed");
    }
  }
}
