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
package edu.uci.ics.sourcerer.util.io.logging;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Strings;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TaskProgressLogger {
  private static final ThreadLocal<TaskProgressLogger> task = 
      new ThreadLocal<TaskProgressLogger>() {
        @Override
        protected TaskProgressLogger initialValue() {
          return new TaskProgressLogger(new LinkedList<TaskInfo>());
        }
  };
  
  private String spaces;
  private final Deque<TaskInfo> tasks;
  
  private TaskProgressLogger(Deque<TaskInfo> tasks) {
    this.tasks = tasks;
  }
  
  public static TaskProgressLogger get() {
    return task.get();
  }
  
  public static void makeNull() {
    task.set(new TaskProgressLogger(null) {
      @Override
      protected void start(String taskName, int indent, String finishedText, int progressInterval) {}
      @Override
      public void report(Level level, String text) {}
      @Override
      public void progress(String message) {}
      @Override
      public void finish() {}
      @Override
      public void cancel() {}
    });
  }
  
  public TaskProgressLogger createChild() {
    TaskProgressLogger child = new TaskProgressLogger(new LinkedList<TaskInfo>());
    child.tasks.add(new TaskInfo(null, null, tasks.peek().indent, -1));
    return child;
  }
  
  private String getSpaces(int count) {
    if (spaces == null || count > spaces.length()) {
      spaces = Strings.create(' ' , 10 + count);
    }
    return spaces.substring(0 , count);
  }
  
  private int getIndent() {
    if (tasks == null || tasks.isEmpty()) {
      return 0;
    } else {
      return tasks.peek().indent + 2;
    }
  }
  
  public Checkpoint checkpoint() {
    return new Checkpoint(tasks.size());
  }
  
  protected void start(String taskName, int indent, String finishedText, int progressInterval) {
    logger.info(getSpaces(indent) + taskName + "...");
    tasks.push(new TaskInfo(taskName, finishedText, indent, progressInterval));
  }
  
  public void start(String taskName) {
    start(taskName, getIndent(), null, -1);
  }
  
  public void start(String taskName, String finishedText) {
    start(taskName, getIndent(), finishedText, 0);
  }
  
  public void start(String taskName, String finishedText, int progressInterval) {
    start(taskName, getIndent(), finishedText, progressInterval);
  }
  
  public void report(Level level, String text) {
    logger.log(level, getSpaces(getIndent()) + text);
  }
  
  public void report(String text) {
    report(Level.INFO, text);
  }
  
  public void progress(String message) {
    TaskInfo info = tasks.peek();
    if (info.progressInterval == -1) {
      throw new IllegalStateException("May not progress this task.");
    } else if (info.progressInterval == 0) {
      info.count++;
    } else {
      if (++info.count % info.progressInterval == 0) {
        if (message == null) {
          logger.info(getSpaces(info.indent + 1) + info.count + " " + info.finishedText + " in " + formatTime(info.startTime));
        } else {
          try {
            logger.info(getSpaces(info.indent + 1) + String.format(message, info.count, formatTime(info.startTime)));
          } catch (Exception e) {
            logger.info(getSpaces(info.indent + 1) + message + " " + info.count + " " + formatTime(info.startTime));
          }
        }
      }
    }
  }
  
  public void progress() {
    progress(null);
  }
  
  public void reportException(Exception e) {
    if (tasks.isEmpty()) {
      throw new IllegalStateException("Cannot finish a non-existant task.", e);
    } else {
      TaskInfo info = tasks.peek();
      logger.log(Level.SEVERE, "Exception while " + info.taskText, e);
    }
  }
  
  public void exception(Exception e) {
    if (tasks.isEmpty()) {
      throw new IllegalStateException("Cannot finish a non-existant task.", e);
    } else {
      TaskInfo info = tasks.pop();
      logger.info(getSpaces(info.indent + 1) + "Unable to finish " + info.taskText + " due to exception.");
      logger.log(Level.SEVERE, "Exception while " + info.taskText, e);
    }
  }
  
  public void finish() {
    if (tasks.isEmpty()) {
      throw new IllegalStateException("Cannot finish a non-existant task.");
    } else {
      TaskInfo info = tasks.pop();
      if (info.progressInterval == -1) {
        logger.info(getSpaces(info.indent + 1) + "Finished " + info.taskText + " in " + formatTime(info.startTime));
      } else {
        logger.info(getSpaces(info.indent + 1) + info.count + " " + info.finishedText + " in " + formatTime(info.startTime));
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

  
  private static class TaskInfo {
    public final String taskText;
    public final String finishedText;
    public final int indent;
    public final long startTime;
    public int count;
    public int progressInterval;
    
    public TaskInfo(String taskText, String finishedText, int indent, int progressInterval) {
      this.taskText = taskText != null ? taskText.toLowerCase() : null;
      this.finishedText = finishedText;
      this.indent = indent;
      this.startTime = System.currentTimeMillis();
      this.count = 0;
      this.progressInterval = progressInterval;
    }
  }
  
  public class Checkpoint {
    private int stackSize;
    
    private Checkpoint(int stackSize) {
      this.stackSize = stackSize;
    }
    
    public void activate() {
      while (tasks.size() > stackSize) {
        finish();
      }
    }
  }
}
