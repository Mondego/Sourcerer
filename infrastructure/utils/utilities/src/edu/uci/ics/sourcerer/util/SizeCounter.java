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
  private static NumberFormat format;
  static {
    format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(2);
  }
  
  private static final long KB = 1024l;
  private static final long MB = 1024l * KB;
  private static final long GB = 1024l * MB;
  public static String formatSize(long value) {
    if (value >= GB) {
      return format.format((double) value / GB) + "G";
    } else if (value >= MB) {
      return format.format((double) value / MB) + "M";
    } else if (value >= KB) {
      return format.format((double) value / KB) + "K";
    } else {
      return value + "B";
    }
  }
  
  private int count;
  private long bytes;
  
  public SizeCounter() {
    count = 0;
    bytes = 0;
  }
  
  public void add(long value) {
    count++;
    bytes += value;
  }
  
  public String getCountString() {
    return Integer.toString(count);
  }
  
  
  public String getSizeString() {
    return formatSize(bytes);
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
