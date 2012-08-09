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
package edu.uci.ics.sourcerer.util;

import java.util.Iterator;

import edu.uci.ics.sourcerer.util.io.logging.Logging;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Nullerator<T> {
  private final TaskProgressLogger task;
  private final String message;
  private Iterator<? extends T> iterator;
  
  private Nullerator(Iterable<? extends T> iterable, String message) {
    this.task = TaskProgressLogger.get();
    this.message = message;
    if (iterable != null) {
      iterator = iterable.iterator();
    }
  }
  
  public synchronized T next() {
    if (iterator == null) {
      return null;
    } else if (iterator.hasNext()) {
      T next = iterator.next();
      if (message != null) {
        task.report(Logging.THREAD_INFO, String.format(message, Thread.currentThread().getName(), next == null ? "null" : next.toString()));
      }
      return next;
    } else {
      iterator = null;
      return null;
    }
  }
  
  public static <T> Nullerator<T> createNullerator(Iterable<? extends T> iterable, String message) {
    return new Nullerator<>(iterable, message);
  }
}
