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
package edu.uci.ics.sourcerer.utils.db.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public interface Selectable<T> {
  public Table getTable();
  
  public ComparisonCondition compareEquals(Selectable<T> other);
  public ConstantCondition<T> compareEquals();
  public ConstantCondition<T> compareEquals(T value);
  public ConstantCondition<T> compareNotEquals();
  public ConstantCondition<T> compareNotEquals(T value);
  public InConstantCondition<T> compareIn(Collection<T> values);
  public InConstantCondition<T> compareNotIn(Collection<T> values);
  public Condition compareNull();
  public Condition compareNotNull();
  
  public void toSql(StringBuilder builder);
  
  public String to(T value);
  public T from(String value);
  
  public void bind(T value, PreparedStatement statement, int index) throws SQLException;
}
