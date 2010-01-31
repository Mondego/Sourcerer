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
package edu.uci.ics.sourcerer.repo.general;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractProperties {
  protected Properties properties;
  
  protected static final String NAME = "name";
  
  protected static final String EXTRACTED = "extracted";
  protected static final String MISSING_TYPES = "missingTypes";
  protected static final String FROM_SOURCE = "fromSource";
  protected static final String SOURCE_EXCEPTIONS = "sourceExceptions";
  protected static final String FIRST_ORDER_JARS = "firstOrderJars";
  protected static final String JARS = "jars";
  
  // Base properties
  protected String name;
  
  // Extraction properties
  protected boolean extracted;

  protected boolean missingTypes;
  protected int fromSource;
  protected int sourceExceptions;
  protected int firstOrderJars;
  protected int jars;
  
  protected void loadProperties(File file) {
    properties = new Properties();
    if (file.exists()) {
      InputStream is = null;
      try {
        is = new FileInputStream(file);
        properties.load(is);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to load properties file: " + file.getPath(), e);
      } finally {
        FileUtils.close(is);
      }
      
      name = properties.getProperty(NAME);
      extracted = readBooleanProperty(EXTRACTED);
      missingTypes = readBooleanProperty(MISSING_TYPES);
      fromSource = readIntProperty(FROM_SOURCE);
      sourceExceptions = readIntProperty(SOURCE_EXCEPTIONS);
      firstOrderJars = readIntProperty(FIRST_ORDER_JARS);
      jars = readIntProperty(JARS);
    }
  }
  
  protected boolean readBooleanProperty(String name) {
    return "true".equals(properties.get(name));
  }
  
  protected int readIntProperty(String name) {
    String value = properties.getProperty(name);
    if (value != null) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        logger.log(Level.SEVERE, "Error reading int property: " + value);
        return -1;
      }
    } else {
      return -1;
    }
  }

  protected void set(String property, boolean value) {
    properties.setProperty(property, "" + value);
  }
  
  protected void set(String property, int value) {
    properties.setProperty(property, "" + value);
  }
  
  protected void set(String property, String value) {
    if (value == null) {
      properties.remove(property);
    } else {
      properties.setProperty(property, value);
    }
  }

  public void save(File file) {
    set(NAME, name);
    set(EXTRACTED, extracted);
    set(MISSING_TYPES, missingTypes);
    set(FROM_SOURCE, fromSource);
    set(SOURCE_EXCEPTIONS, sourceExceptions);
    set(FIRST_ORDER_JARS, firstOrderJars);
    set(JARS, jars);
    
    write(file, properties);
  }
  
  protected static void write(File file, Properties properties) {
    try {
      OutputStream os = new FileOutputStream(file);
      properties.store(os, null);
      os.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write properties file: " + file.getPath(), e);
    }
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public void setExtracted(boolean extracted) {
    this.extracted = extracted;
  }

  public void setMissingTypes(boolean missingTypes) {
    this.missingTypes = missingTypes;
  }

  public void setFromSource(int fromSource) {
    this.fromSource = fromSource;
  }

  public void setSourceExceptions(int sourceExceptions) {
    this.sourceExceptions = sourceExceptions;
  }
  
  public void setFirstOrderJars(int firstOrderJars) {
    this.firstOrderJars = firstOrderJars;
  }
  
  public void setJars(int jars) {
    this.jars = jars;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean extracted() {
    return extracted;
  }
  
  public boolean missingTypes() {
    return missingTypes;
  }
  
  public int getFirstOrderJars() {
    return firstOrderJars;
  }
  
  public int getJars() {
    return jars;
  }
  
  protected void verifyExtracted() {
    if (!extracted) {
      throw new IllegalStateException("This item has not been extracted yet.");
    }
  }
  
  public int getExtractedFromSource() {
    verifyExtracted();
    return fromSource;
  }
  
  public int getSourceExceptions() {
    verifyExtracted();
    return sourceExceptions;
  }
}
