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

import static edu.uci.ics.sourcerer.util.io.arguments.Arguments.OUTPUT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;
import edu.uci.ics.sourcerer.util.io.arguments.Command;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("serial")
public final class Logging {
  protected static final Argument<Boolean> SUPPRESS_FILE_LOGGING = new BooleanArgument("suppress-file-logging", false, "Suppresses all logging to files.").permit();
  public static final Argument<Boolean> REPORT_TO_CONSOLE = new BooleanArgument("report-to-console", false, "Prints all the logging messages to the console.").permit();
  protected static final Argument<String> ERROR_LOG = new StringArgument("error-log", "error.log", "Filename for error log.").permit();
  protected static final Argument<String> THREAD_LOG = new StringArgument("thread-log", "%t.log", "Filename for thread log.").permit();
  protected static final Argument<String> INFO_LOG = new StringArgument("info-log", "info.log", "Filename for the info log.").permit();
  protected static final Argument<String> RESUME_LOG = new StringArgument("resume-log", "resume.log", "Filename for the resume log.").permit();
  protected static final Argument<Boolean> CLEAR_RESUME_LOG = new BooleanArgument("clear-resume-log", false, "Clears the resume log before beginning.").permit(); 
  
  public static final Level RESUME = new Level("RESUME", 10000) {};
  public static final Level THREAD_INFO = new Level("TINFO", 100000) {};
  
  public static Logger logger;
  
  private Logging() {}
  
  private static boolean loggingInitialized = false;
  private static boolean resumeLoggingInitialized = false;
  private static StreamHandler defaultHandler;
  private static String day;
  private static String time;
  
  private static Map<File, Handler> handlerMap = new HashMap<>();
  
  private static long mainThread;
  private static Map<Long, Handler> threadHandlerMap = new HashMap<>();
  private static Command command;
  
  private static class FlushingStreamHandler extends StreamHandler {
    public FlushingStreamHandler(OutputStream out) {
      setOutputStream(out);
    }
    
    public void publish(LogRecord record) {
      super.publish(record);
      flush();
    }
  }
  
  static {
    logger = Logger.getLogger("edu.uci.ics.sourcerer.util.io");
    logger.setUseParentHandlers(false);
    
    Formatter formatter = new Formatter() {
      @Override
      public String format(LogRecord record) {
        return Logging.formatError(record);
      }
    };
    defaultHandler = new FlushingStreamHandler(System.out);
    defaultHandler.setFormatter(formatter);
    defaultHandler.setLevel(Level.INFO);
    logger.addHandler(defaultHandler);
    
    SimpleDateFormat format = new SimpleDateFormat("MMM-dd-yyyy");
    day = format.format(new Date()).toLowerCase();
    format = new SimpleDateFormat("HH-mm-ss");
    time = format.format(new Date()).toLowerCase();
  }

  private static Set<String> getResumeSet(File resumeFile) {
    if (resumeFile.exists()) {
      Set<String> resumeSet = Helper.newHashSet();
      try (BufferedReader br = new BufferedReader(new FileReader(resumeFile))) {
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          resumeSet.add(line);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      return resumeSet;
    } else {
      return Collections.emptySet();
    }
  }
  
  private static String getOutputDir(Command command) {
    return OUTPUT.getValue().getPath().replace('\\', '/') + "/" + command.getName() + "/" + day + "/" + time;
  }
  
  private static String getFileHandlerPattern(Command command, Argument<String> prop) {
    return getOutputDir(command) + "/" + prop.getValue().replace("%t", "" + Thread.currentThread().getName());
  }
  
  public synchronized static Set<String> initializeResumeLogger() {
    if (resumeLoggingInitialized) {
      throw new IllegalStateException("Resume logging may only be initialized once");
    } else if (!loggingInitialized) {
      throw new IllegalStateException("Logging must be initialized before resume logging");
    }
    if (OUTPUT.hasValue()) {
      File resumeFile = new File(OUTPUT.getValue(), command.getName() + "/" + RESUME_LOG.getValue());
      
      if (CLEAR_RESUME_LOG.getValue()) {
        if (resumeFile.exists()) {
          resumeFile.delete();
        }
      }
      
      Set<String> resumeSet = getResumeSet(resumeFile);
      
      try {
        FileHandler resumeHandler = new FileHandler(resumeFile.getPath(), true);
        resumeHandler.setLevel(RESUME);
        resumeHandler.setFormatter(new Formatter() {
          @Override
          public String format(LogRecord record) {
            return record.getMessage() + "\n";
          }
        });
        logger.addHandler(resumeHandler);
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
      
      resumeLoggingInitialized = true;
      return resumeSet;
    } else {
      return Collections.emptySet();
    }
  }
 
  public synchronized static void initializeLogger(Command command) {
    Logging.command = command;
    if (loggingInitialized) {
      throw new IllegalStateException("The logger may only be initialized once");
    }
    
    try {
      mainThread = Thread.currentThread().getId();
      
      boolean suppressFileLogging = SUPPRESS_FILE_LOGGING.getValue();
      
      if (suppressFileLogging && !REPORT_TO_CONSOLE.getValue()) {
        logger.removeHandler(defaultHandler);
        return;
      }
      
      if (!suppressFileLogging) {
        if (OUTPUT.hasValue()) {
          new File(getOutputDir(command)).mkdirs();
        } else {
          suppressFileLogging = true;
          SUPPRESS_FILE_LOGGING.setValue(true);
        }
      }
      
      StreamHandler errorHandler = null;
      if (suppressFileLogging) {
        Formatter errorFormatter = new Formatter() {
          @Override
          public String format(LogRecord record) {
            if (record.getLevel() == RESUME || Thread.currentThread().getId() != mainThread) {
              return "";
            } else {
              return Logging.formatError(record);
            }
          }
        };
        errorHandler = new FlushingStreamHandler(System.out);
        errorHandler.setFormatter(errorFormatter);
      } else {
        Formatter errorFormatter = new Formatter() {
          @Override
          public String format(LogRecord record) {
            if (record.getLevel() != THREAD_INFO && (record.getLevel() == RESUME || Thread.currentThread().getId() != mainThread)) {
              return "";
            } else {
              String msg = Logging.formatError(record);
              if (REPORT_TO_CONSOLE.getValue() && record.getLevel() == Level.SEVERE) {
                System.err.print(msg);
              }
              return msg;
            }
          }
        };
        errorHandler = new FileHandler(getFileHandlerPattern(command, ERROR_LOG));
        errorHandler.setFormatter(errorFormatter);
      }
      errorHandler.setLevel(Level.INFO);
      
      StreamHandler infoHandler = null;
      if (!suppressFileLogging) {
        Formatter infoFormatter = new Formatter() {
          @Override
          public String format(LogRecord record) {
            if (record.getLevel() == THREAD_INFO || (record.getLevel() == Level.INFO && Thread.currentThread().getId() == mainThread)) {
              String msg = formatInfo(record);
              if (REPORT_TO_CONSOLE.getValue()) {
                System.out.print(msg);
              }
              return msg;
            } else {
              return "";
            }
          }
        };
        infoHandler = new FileHandler(getFileHandlerPattern(command, INFO_LOG));
        infoHandler.setFormatter(infoFormatter);
        infoHandler.setLevel(Level.INFO);
        logger.addHandler(infoHandler);
      }
            
      logger.addHandler(errorHandler);
      
      logger.removeHandler(defaultHandler);
      
      logger.info(Logging.command.getName());
      loggingInitialized = true;
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  public synchronized static void addThreadLogger() {
    Thread thread = Thread.currentThread();
    if (!loggingInitialized) {
      throw new IllegalStateException("Logging must be initialized before error logs can be added.");
    } else if (threadHandlerMap.containsKey(thread.getId())) {
      throw new IllegalArgumentException("Error logging may not be added to the same thread twice: " + thread.getId());
    }

    if (!SUPPRESS_FILE_LOGGING.getValue()) {
      final long id = thread.getId();
      
      Formatter formatter = new Formatter() {
        @Override
        public String format(LogRecord record) {
          if (Thread.currentThread().getId() == id) {
            if (record.getLevel() == RESUME || record.getLevel() == THREAD_INFO) {
              return "";
            } else if (record.getLevel() == Level.INFO) {
              return Logging.formatInfo(record);
            } else {
              return Logging.formatError(record);
            }
          } else {
            return "";
          }
        }
      };
      
      try {
        String handlerPath = getFileHandlerPattern(command, THREAD_LOG);
        StreamHandler handler = new FileHandler(handlerPath);
        handler.setFormatter(formatter);
        handler.setLevel(Level.INFO);
        threadHandlerMap.put(id, handler);
        logger.addHandler(handler);
      } catch (IOException e) {
        logger.log(THREAD_INFO, "Error adding file logger.", e);
      }
    }
  }
  
  public synchronized static void removeThreadLogger() {
    if (!loggingInitialized) {
      throw new IllegalStateException("Logging must be initialized before error logs can be removed.");
    }
    Thread thread = Thread.currentThread();
    Handler handler = threadHandlerMap.get(thread.getId());
    if (handler != null) {
      logger.removeHandler(handler);
      handler.close();
      threadHandlerMap.remove(thread.getId());
    }
  }
  
  public synchronized static void addFileLogger(File file) {
    if (!loggingInitialized) {
      throw new IllegalStateException("Logging must be initialized before error logs can be added.");
    } else if (handlerMap.containsKey(file)) {
      throw new IllegalArgumentException("Error logging may not be added to the same file twice: " + file.getPath());
    }

    Formatter formatter = new Formatter() {
      @Override
      public String format(LogRecord record) {
        
        if (record.getLevel() == RESUME) {
          return "";
        } else if (record.getLevel() == Level.INFO) {
          return Logging.formatInfo(record);
        } else {
          return Logging.formatError(record);
        }
      }
    };
    
    try {
      file.mkdirs();
      StreamHandler handler = new FileHandler(new File(file, "log").getPath(), true);
      handler.setFormatter(formatter);
      handler.setLevel(Level.INFO);
      handlerMap.put(file, handler);
      logger.addHandler(handler);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error adding file logger.", e);
    }
  }
  
  public synchronized static void removeFileLogger(File file) {
    if (!loggingInitialized) {
      throw new IllegalStateException("Logging must be initialized before error logs can be removed.");
    }
    
    Handler handler = handlerMap.get(file);
    if (handler != null) {
      logger.removeHandler(handler);
      handler.close();
      handlerMap.remove(file);
    }
  }
  
  private static final DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
  private static synchronized String formatError(LogRecord record) {
    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);
    pw.print("[" + format.format(new Date(record.getMillis())) + " - " + record.getLevel() + "] ");
    pw.print(record.getMessage());
    if (record.getParameters() != null) {
      for (Object o : record.getParameters()) {
        pw.print(" " + o);
      }
    }
    if (record.getThrown() != null) {
      pw.println();
      record.getThrown().printStackTrace(pw);
    }
    pw.println();
    return writer.toString();
  }
  
  private static synchronized String formatInfo(LogRecord record) {
    return "[" + format.format(new Date(record.getMillis())) + "] " + record.getMessage() + "\n";
  }
}
