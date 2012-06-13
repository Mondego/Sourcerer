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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class LetterCounter {
  private int count = 0;
  
  public int getCount() {
    return count;
  }
  
  public String getNext() {
    StringBuilder result = new StringBuilder();
    int val = count++;
    while (val >= 0) {
      result.insert(0, (char)('A' + val % 26));
      val = val / 26 - 1;
    }
    return result.toString();
  }
}
