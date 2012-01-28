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
package edu.uci.ics.sourcerer.util.io.properties;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.IOUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractProperties {
  private Properties properties;
  private File file;
  private Map<String, Property<?>> props;
  
  protected AbstractProperties(File file) {
    props = new HashMap<>();
    this.file = file;
  }
  
  private void initialize() {
    properties = new Properties();
    if (file.exists()) {
      InputStream is = null;
      try {
        is = new FileInputStream(file);
        properties.load(is);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to load properties file: " + file.getPath(), e);
      } finally {
        IOUtils.close(is);
      }
    }
  }
  
  protected String getValue(String name) {
    if (properties == null) {
      initialize();
    }
    return properties.getProperty(name);
  }
  
  protected void registerProperty(Property<?> property) {
    props.put(property.getName(), property);
  }
  
  public void clear() {
    properties.clear();
    for (Property<?> prop : props.values()) {
      prop.reset();
    }
  }
  
  public void copy(AbstractProperties other) {
    for (Property<?> prop : props.values()) {
      Property<?> otherProp = other.props.get(prop.getName());
      if (otherProp != null) {
        prop.copy(otherProp);
      }
    }
  }
  
  public void save() {
    if (properties == null) {
      initialize();
    }
    for (Property<?> prop : props.values()) {
      String val = prop.getValueAsString();
      if (val == null) {
        properties.remove(prop.getName());
      } else {
        properties.setProperty(prop.getName(), val);
      }
    }
    OutputStream os = null;
    try {
      if (!file.exists()) {
        file.getParentFile().mkdirs();
      }
      os = new FileOutputStream(file);
      properties.store(os, null);
      os.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write properties file: " + file.getPath(), e);
    } finally {
      IOUtils.close(os);
    }
  }
}
