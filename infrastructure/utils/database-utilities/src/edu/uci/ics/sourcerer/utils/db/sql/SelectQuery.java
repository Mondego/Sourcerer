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


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public interface SelectQuery extends Query {
  public void addSelect(Selectable<?> select);
  public void addSelects(Selectable<?> ... selects);
  public void clearSelect();
  public void addWhere(InConstantCondition<?> condition);
  public void addWhere(ConstantCondition<?> condition);
  public <T> void addWhere(ConstantCondition<T> condition, T value);
  public <T> void updateWhere(ConstantCondition<T> condition, T value);
  public void clearWhere();
  public void addOrderBy(Selectable<?> select, boolean ascending);
  
  public TypedQueryResult select();
  public TypedQueryResult selectStreamed();
}
