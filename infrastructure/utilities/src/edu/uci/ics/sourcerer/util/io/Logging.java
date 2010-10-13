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

import static edu.uci.ics.sourcerer.util.io.Properties.OUTPUT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("serial")
public final class Logging {
  protected static final Property<Boolean> SUPPRESS_FILE_LOGGING = new BooleanProperty("suppress-file-logging", false, "Suppresses all logging to files.").register("Logging");
  protected static final Property<Boolean> REPORT_TO_CONSOLE = new BooleanProperty("report-to-console", false, "Prints all the logging messages to the console.").register("Logging");
  protected static final Property<String> ERROR_LOG = new StringProperty("error-log", "error.log", "Filename for error log.").register("Logging");
  protected static final Property<String> THREAD_LOG = new StringProperty("thread-log", "thread-%t.log", "Filename for thread log.").register("Logging");
  protected static final Property<String> INFO_LOG = new StringProperty("info-log", "info.log", "Filename for the info log.").register("Logging");
  protected static final Property<String> RESUME_LOG = new StringProperty("resume-log", "resume.log", "Filename for the resume log.");
  protected static final Property<Boolean> CLEAR_RESUME_LOG = new BooleanProperty("clear-resume-log", false, "Clears the resume log before beginning."); 
  
  public static final Level RESUME = new Level("RESUME", 10000) {};
  public static final Level THREAD_INFO = new Level("TINFO", 100000) {};
  
  public static Logger logger;
  
  private Logging() {}
  
  private static boolean loggingInitialized = false;
  private static boolean resumeLoggingInitialized = false;
  private static StreamHandler defaultHandler;
  private static String time;
  
  private static Map<File, Handler> handlerMap = Helper.newHashMap();
  
  private static long mainThread;
  private static Map<Long, Handler> threadHandlerMap = Helper.newHashMap();
  
  static {
    logger = Logger.getLogger("edu.uci.ics.sourcerer.util.io");
    logger.setUseParentHandlers(false);
    
    Formatter formatter = new Formatter() {
      @Override
      public String format(LogRecord record) {
        String msg = Logging.formatError(record);
        System.err.print(msg);
        return msg;
      }
    };
    defaultHandler = new StreamHandler(System.err, formatter);
    defaultHandler.setLevel(Level.INFO);
    logger.addHandler(defaultHandler);
    
    SimpleDateFormat format = new SimpleDateFormat("MMM-dd-yyyy-HH-mm-ss");
    time = format.format(new Date());
  }

  private static Set<String> getResumeSet(File resumeFile) {
    if (resumeFile.exists()) {
      Set<String> resumeSet = Helper.newHashSet();
      try {
        BufferedReader br = new BufferedReader(new FileReader(resumeFile));
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
  
  private static String getFileHandlerPattern(Property<String> prop) {
    return OUTPUT.getValue().getPath().replace('\\', '/') + "/" + time + "/" + prop.getValue().replace("%t", "" + Thread.currentThread().getId());
  }
  
  public synchronized static Set<String> initializeResumeLogger() {
    if (resumeLoggingInitialized) {
      throw new IllegalStateException("Resume logging may only be initialized once");
    }
    
    File resumeFile = new File(OUTPUT.getValue(), RESUME_LOG.getValue());
    
    if (CLEAR_RESUME_LOG.getValue()) {
      if (resumeFile.exists()) {
        resumeFile.delete();
      }
    }
    
    Set<String> resumeSet = getResumeSet(resumeFile);
    
    if (!loggingInitialized) {
      initializeLogger();
    }
    
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
  }
 
  public synchronized static void initializeLogger() {
    if (loggingInitialized) {
      throw new IllegalStateException("The logger may only be initialized once");
    }
    
    try {
      mainThread = Thread.currentThread().getId();
      
      boolean suppressFileLogging = SUPPRESS_FILE_LOGGING.getValue();
      
      if (suppressFileLogging && !REPORT_TO_CONSOLE.getValue()) {
        return;
      }
      
      if (!suppressFileLogging) {
        if (OUTPUT.hasValue()) {
          new File(OUTPUT.getValue(), time).mkdirs();
        } else {
          suppressFileLogging = true;
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
        errorHandler = new StreamHandler(System.err, errorFormatter);
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
        errorHandler = new FileHandler(getFileHandlerPattern(ERROR_LOG));
        errorHandler.setFormatter(errorFormatter);
      }
      errorHandler.setLevel(Level.INFO);
      
      StreamHandler infoHandler = null;
      if (!suppressFileLogging) {
//        Formatter infoFormatter = new Formatter() {
//          @Override
//          public String format(LogRecord record) {
//            return formatInfo(record);
//          }
//        };
//        infoHandler = new StreamHandler(System.out, infoFormatter);
//      } else {
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
        infoHandler = new FileHandler(getFileHandlerPattern(INFO_LOG));
        infoHandler.setFormatter(infoFormatter);
        infoHandler.setLevel(Level.INFO);
        logger.addHandler(infoHandler);
      }
            
      logger.addHandler(errorHandler);
      
      logger.removeHandler(defaultHandler);
      
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

    final long id = thread.getId();
    
    Formatter formatter = new Formatter() {
      @Override
      public String format(LogRecord record) {
        if (Thread.currentThread().getId() == id) {
          if (record.getLevel() == RESUME) {
            return "";
          } else if (record.getLevel() == Level.INFO || record.getLevel() == THREAD_INFO) {
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
      
      StreamHandler handler = new FileHandler(getFileHandlerPattern(THREAD_LOG));
      handler.setFormatter(formatter);
      handler.setLevel(Level.INFO);
      threadHandlerMap.put(id, handler);
      logger.addHandler(handler);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error adding file logger.", e);
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
