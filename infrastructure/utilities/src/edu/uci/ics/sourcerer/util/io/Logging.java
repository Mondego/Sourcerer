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
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("serial")
public final class Logging {
  private Logging() {}
  
  public static final Level RESUME = new Level("RESUME", 10000) {};
  public static Logger logger = null;
    
  private static Set<String> getResumeSet(String resumeFile) {
    File file = new File(resumeFile);
    if (file.exists()) {
      Set<String> resumeSet = Helper.newHashSet();
      try {
        BufferedReader br = new BufferedReader(new FileReader(file));
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
    
  private static boolean resumeLoggingEnabled = false;
  public synchronized static Set<String> initializeResumeLogger() {
    if (resumeLoggingEnabled) {
      throw new IllegalStateException("Resume logging may only be initialized once");
    }
    PropertyManager properties = PropertyManager.getProperties();
    String resumeFile = properties.getValue(PropertyOld.OUTPUT) + File.separatorChar + properties.getValue(PropertyOld.RESUME_LOG);
    
    if (properties.isSet(PropertyOld.CLEAR_RESUME_LOG)) {
      File file = new File(resumeFile);
      if (file.exists()) {
        file.delete();
      }
    }
    
    Set<String> resumeSet = getResumeSet(resumeFile);
    
    if (logger == null) {
      initializeLogger();
    }
    
    try {
      FileHandler resumeHandler = new FileHandler(resumeFile, true);
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
    
    resumeLoggingEnabled = true;
    return resumeSet;
  }
 
  public synchronized static void initializeLogger() {
    if (logger != null) {
      throw new IllegalStateException("The logger may only be initialized once");
    }
    PropertyManager properties = PropertyManager.getProperties();
    logger = Logger.getLogger("edu.uci.ics.sourcerer.util.io");
    logger.setUseParentHandlers(false);
    
    try {
      final boolean suppressFileLogging = properties.isSet(PropertyOld.SUPPRESS_FILE_LOGGING);
      final boolean reportToConsole = properties.isSet(PropertyOld.REPORT_TO_CONSOLE);
      
      if (suppressFileLogging && !reportToConsole) {
        return;
      }
      
      if (!suppressFileLogging) {
        File dir = new File(properties.getValue(PropertyOld.OUTPUT));
        dir.mkdirs();        
      }
      
      Formatter errorFormatter = null;
      StreamHandler errorHandler = null;
      if (suppressFileLogging) {
        errorFormatter = new Formatter() {
          @Override
          public String format(LogRecord record) {
            if (record.getLevel() == RESUME) {
              return "";
            } else {
              return Logging.formatError(record);
            }
          }
        };
        errorHandler = new StreamHandler(System.err, errorFormatter);
      } else {
        errorFormatter = new Formatter() {
          @Override
          public String format(LogRecord record) {
            if (record.getLevel() == RESUME) {
              return "";
            } else {
              String msg = Logging.formatError(record);
              if (reportToConsole && record.getLevel() == Level.SEVERE) {
                System.err.print(msg);
              }
              return msg;
            }
          }
        };
        errorHandler = new FileHandler(properties.getValue(PropertyOld.OUTPUT) + File.separatorChar + properties.getValue(PropertyOld.ERROR_LOG));
        errorHandler.setFormatter(errorFormatter);
      }
      errorHandler.setLevel(Level.WARNING);
      
      Formatter infoFormatter = null;
      StreamHandler infoHandler = null;
      if (suppressFileLogging) {
        infoFormatter = new Formatter() {
          @Override
          public String format(LogRecord record) {
            return formatInfo(record);
          }
        };
        infoHandler = new StreamHandler(System.out, infoFormatter);
      } else {
        infoFormatter = new Formatter() {
          @Override
          public String format(LogRecord record) {
            if (record.getLevel() == Level.INFO) {
              String msg = formatInfo(record);
              if (reportToConsole) {
                System.out.print(msg);
              }
              return msg;
            } else {
              return "";
            }
          }
        };
        infoHandler = new FileHandler(properties.getValue(PropertyOld.OUTPUT) + File.separatorChar + properties.getValue(PropertyOld.INFO_LOG));
        infoHandler.setFormatter(infoFormatter);
      }
      infoHandler.setLevel(Level.INFO);
            
      logger.addHandler(errorHandler);
      logger.addHandler(infoHandler);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
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
