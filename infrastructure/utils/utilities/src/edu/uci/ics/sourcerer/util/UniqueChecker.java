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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.HashSet;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class UniqueChecker <T> {
  private final boolean errorOnDup;
  private final HashSet<T> seen;
  
  private UniqueChecker(boolean errorOnDup) {
    this.errorOnDup = errorOnDup;
    seen = new HashSet<>();
  }
  
  public static <T> UniqueChecker<T> create(boolean errorOnDup) {
    return new UniqueChecker<>(errorOnDup);
  }
  
  public boolean isUnique(T item) {
    if (item == null) {
      logger.severe("null not permitted");
      return false;
    } else  if (seen.contains(item)) {
      if (errorOnDup) {
        logger.severe("Duplicate item: " + item);
      }
      return false;
    } else {
      seen.add(item);
      return true;
    }
  }
}
