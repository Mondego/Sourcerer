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
public final class TimeCounter {
  private long startTime;
  private long lapTime;
  private int totalCount;
  private int count;
  
  public TimeCounter() {
    startTime = System.currentTimeMillis();
    lapTime = startTime;
    count = 0;
  }
  
  public void reset() {
    totalCount = 0;
    count = 0;
    startTime = System.currentTimeMillis();
    lapTime = startTime;
  }
  public void lap() {
    totalCount += count;
    count = 0;
    lapTime = System.currentTimeMillis();
  }
  
  public void increment() {
    count++;
  }
  
  public void setCount(int count) {
    this.count = count;
  }
  
  private String formatTime(long time) {
    long elapsedTime = System.currentTimeMillis() - time;
    return elapsedTime / 1000 + "." + ((elapsedTime / 100) % 100) + "s.";
  }
  
  private static final String SPACES = "          ";
  public String reportTime(int spaces, String action) {
    return SPACES.substring(0, spaces) + action + " in " + formatTime(lapTime);
  }
  
  public String reportTimeAndCount(int spaces, String action) {
    return SPACES.substring(0, spaces) + count + " " + action + " in " + formatTime(lapTime);
  }
  
  public String reportTotalTime(int spaces, String action) {
    return SPACES.substring(0, spaces) + action + " in " + formatTime(startTime);
  }
  
  public String reportTotalTimeAndCount(int spaces, String action) {
    return SPACES.substring(0, spaces) + (totalCount + count) + " " + action + " in " + formatTime(startTime);
  }
  
  public String reportTimeAndTotalCount(int spaces, String action) {
    return SPACES.substring(0, spaces) + (totalCount + count) + " " + action + " in " + formatTime(lapTime);
  }
}
