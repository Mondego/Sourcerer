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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class IterableResult <T> implements Iterable<T> {
  private ResultSet result;
  private BasicResultTranslator<T> translator;
 
  private IterableResult(ResultSet result, BasicResultTranslator<T> translator) {
    this.result = result;
    this.translator = translator;
  }
  
  public static <T> IterableResult<T> getResultIterable(ResultSet result, BasicResultTranslator<T> translator) {
    return new IterableResult<T>(result, translator);
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      private T next;
      @Override
      public boolean hasNext() {
        if (next != null) {
          return true;
        } else {
          try {
            if (result.next()) {
              next = translator.translate(result);
              return true;
            } else {
              return false;
            }
          } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to get next result", e);
            return false;
          }
        }
      }

      @Override
      public T next() {
        if (hasNext()) {
          T retval = next;
          next = null;
          return retval;
        } else {
          throw new NoSuchElementException();
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  } 
}
