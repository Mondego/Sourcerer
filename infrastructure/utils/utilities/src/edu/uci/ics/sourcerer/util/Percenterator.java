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

import java.text.NumberFormat;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class Percenterator {
  private final double total;
  private final NumberFormat format;
  
  private Percenterator(int total) {
    this.total = total;
    format = NumberFormat.getPercentInstance();
    format.setMaximumFractionDigits(1);
  }
  
  public static Percenterator create(int total) {
    return new Percenterator(total);
  }
  
  public String format(int value) {
    return value + " (" + format.format(value / total) + ")";
  }
}
