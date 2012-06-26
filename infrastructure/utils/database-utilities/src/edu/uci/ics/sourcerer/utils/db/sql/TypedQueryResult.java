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

import java.io.Closeable;
import java.util.Collection;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public interface TypedQueryResult extends Closeable {
  public boolean next();
  public int getCount();
  public int toCount();
  public <T> T getResult(Selectable<T> selectable);
  public <T> Collection<T> toCollection(Selectable<T> selectable);
  public <T> Collection<T> toCollection(ResultConstructor<T> constructor);
  public <T> Iterable<T> toIterable(Selectable<T> selectable);
  public <T> Iterable<T> toIterable(ResultConstructor<T> constructor);
  public <T> T toSingleton(Selectable<T> selectable, boolean permitMissing);
  public <T> T toSingleton(ResultConstructor<T> selectable, boolean permitMissing);
  public void close();
}
