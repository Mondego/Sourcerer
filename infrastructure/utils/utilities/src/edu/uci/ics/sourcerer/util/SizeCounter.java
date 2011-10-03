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
public class SizeCounter implements Comparable<SizeCounter> {
  private NumberFormat format;
  private int count;
  private long bytes;
  
  public SizeCounter() {
    count = 0;
    bytes = 0;
    format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(2);
  }
  
  public void add(long value) {
    count++;
    bytes += value;
  }
  
  public String getCountString() {
    return Integer.toString(count);
  }
  
  private static final long KB = 1024l;
  private static final long MB = 1024l * KB;
  private static final long GB = 1024l * MB;
  public String getSizeString() {
    if (bytes >= GB) {
      return format.format((double) bytes / GB) + "G";
    } else if (bytes >= MB) {
      return format.format((double) bytes / MB) + "M";
    } else if (bytes >= KB) {
      return format.format((double) bytes / KB) + "K";
    } else {
      return bytes + "B";
    }
  }

  @Override
  public int compareTo(SizeCounter o) {
    if (this == o) {
      return 0;
    } else if (bytes == o.bytes) {
      return hashCode() - o.hashCode();
    } else {
      return (bytes < o.bytes) ? -1 : 1;
    }
  }
}
