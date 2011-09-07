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
package edu.uci.ics.sourcerer.util.io;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TaskProgressLogger {
  private static final String SPACES;
  static {
    char[] arr = new char[20];
    Arrays.fill(arr, ' ');
    SPACES = new String(arr); 
  }
  
  private final Deque<TaskInfo> tasks;
  
  public TaskProgressLogger(String taskName) {
    tasks = new LinkedList<>();
    start(taskName, 0, taskName.toLowerCase(), -1);
  }
  
  private TaskProgressLogger(int startingIndent) {
    tasks = new LinkedList<>();
    tasks.add(new TaskInfo(null, startingIndent, -1));
  }
  
  public TaskProgressLogger spawnChild() {
    return new TaskProgressLogger(tasks.peek().indent);
  }
  
  private void start(String taskName, int indent, String finishedText, int progressInterval) {
    logger.info(SPACES.substring(0, indent) + taskName + "...");
    tasks.push(new TaskInfo(finishedText, indent, progressInterval));
  }
  
  public void start(String taskName) {
    start(taskName, tasks.peek().indent + 2, taskName.toLowerCase(), -1);
  }
  
  public void start(String taskName, String finishedText) {
    start(taskName, tasks.peek().indent + 2, finishedText, 0);
  }
  
  public void start(String taskName, String finishedText, int progressInterval) {
    start(taskName, tasks.peek().indent + 2, finishedText, progressInterval);
  }
  
  public void report(Level level, String text) {
    logger.log(level, SPACES.substring(0, tasks.peek().indent + 2) + text);
  }
  
  public void report(String text) {
    logger.info(SPACES.substring(0, tasks.peek().indent + 2) + text);
  }
  
  public void progress() {
    TaskInfo info = tasks.peek();
    if (info.progressInterval == -1) {
      throw new IllegalStateException("May not progress this task.");
    } else if (info.progressInterval == 0) {
      info.count++;
    } else {
      if (++info.count % info.progressInterval == 0) {
        logger.info(SPACES.substring(0, info.indent + 1) + info.count + " " + info.finishedText + " in " + formatTime(info.startTime));
      }
    }
  }
  
  public void progress(String message) {
    TaskInfo info = tasks.peek();
    logger.info(SPACES.substring(0, info.indent + 1) + String.format(message, ++info.count));
  }
  
  public void finish() {
    if (tasks.isEmpty()) {
      throw new IllegalStateException("Cannot finish a non-existant task.");
    } else {
      TaskInfo info = tasks.pop();
      if (info.progressInterval == -1) {
        logger.info(SPACES.substring(0, info.indent + 1) + "Finished " + info.finishedText + " in " + formatTime(info.startTime));
      } else {
        logger.info(SPACES.substring(0, info.indent + 1) + info.count + " " + info.finishedText + " in " + formatTime(info.startTime));
      }
    }
  }
  
  public void cancel() {
    if (tasks.isEmpty()) {
      throw new IllegalStateException("Cannot finish a non-existant task.");
    } else {
      tasks.pop();
    }
  }
  
  private static class TaskInfo {
    public final String finishedText;
    public final int indent;
    public final long startTime;
    public int count;
    public int progressInterval;
    
    public TaskInfo(String finishedText, int indent, int progressInterval) {
      this.finishedText = finishedText;
      this.indent = indent;
      this.startTime = System.currentTimeMillis();
      this.count = 0;
      this.progressInterval = progressInterval;
    }
  }
  
  private static String formatTime(long time) {
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
}
