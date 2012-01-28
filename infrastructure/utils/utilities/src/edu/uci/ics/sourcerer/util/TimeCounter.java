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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class TimeCounter {
  private long startTime;
  private long lapTime;
  private int totalCount;
  private int count;
  private final int progressInterval;
  private final int progressIndent;
  private final String progressAction;
  
  public TimeCounter() {
    this(-1, -1, null);
  }
  
  public TimeCounter(int progressInterval, int progressIndent, String progressAction) {
    startTime = System.currentTimeMillis();
    lapTime = startTime;
    count = 0;
    this.progressInterval = progressInterval;
    this.progressIndent = progressIndent;
    this.progressAction = progressAction;
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
    if (progressInterval >= 0 && count % progressInterval == 0) {
      logTimeAndCount(progressIndent, progressAction);
    }
  }
  
  public void setCount(int count) {
    this.count = count;
  }
  
  private String formatTime(long time) {
    long elapsedTime = System.currentTimeMillis() - time;
    long seconds = elapsedTime / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;
    StringBuilder result = new StringBuilder();
    if (days > 0) {
      result.append(days).append(days == 1 ? " day " : " days ");
    }
    if (hours > 0) {
      hours = hours % 24;
      result.append(hours).append(hours == 1 ? " hour " : " hours ");
    }
    if (minutes > 0) {
      minutes = minutes % 60;
      result.append(minutes).append(minutes == 1 ? " minute " : " minutes ");
    }
    result.append(seconds % 60).append(".").append((elapsedTime / 100) % 100).append(" seconds.");
    return result.toString();
  }
  
  private static final String SPACES = "          ";
  public void logTime(int spaces, String action) {
    logger.info(SPACES.substring(0, spaces) + action + " in " + formatTime(lapTime));
  }
  
  public void logTimeAndCount() {
    logTimeAndCount(progressIndent, progressAction);
  }
  
  public void logTimeAndCount(int spaces, String action) {
    logger.info(SPACES.substring(0, spaces) + count + " " + action + " in " + formatTime(lapTime));
  }
  
  public void logTotalTime(int spaces, String action) {
    logger.info(SPACES.substring(0, spaces) + action + " in " + formatTime(startTime));
  }
  
  public void logTotalTimeAndCount(int spaces, String action) {
    logger.info(SPACES.substring(0, spaces) + (totalCount + count) + " " + action + " in " + formatTime(startTime));
  }
  
  public void logTimeAndTotalCount(int spaces, String action) {
    logger.info(SPACES.substring(0, spaces) + (totalCount + count) + " " + action + " in " + formatTime(lapTime));
  }
}
