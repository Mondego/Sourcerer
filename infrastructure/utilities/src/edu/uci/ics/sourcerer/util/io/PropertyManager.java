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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class PropertyManager {
  private static PropertyManager singleton = null;
  private Map<Property, String> propertyMap;
  
  private PropertyManager() {
    propertyMap = Helper.newEnumMap(Property.class);
  }
    
  public synchronized void setProperty(Property prop, String value) {
    propertyMap.put(prop, value);
  }
  
  public synchronized void setBooleanProperty(Property prop, boolean value) {
    if (value) {
      propertyMap.put(prop, "");
    } else {
      propertyMap.remove(prop);
    }
  }
  
  public synchronized boolean hasValue(Property prop) {
    return propertyMap.containsKey(prop) || prop.hasDefaultValue();
  }
  
  public synchronized String getValue(Property prop) {
    if (propertyMap.containsKey(prop)) {
      return propertyMap.get(prop);
    } else if (prop.hasDefaultValue()) {
      return prop.getDefaultValue();
    } else if (isSet(Property.PROMPT_MISSING)) {
      try {
        System.out.print("Please enter value for " + prop.getName() + ":");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String value = br.readLine();
        propertyMap.put(prop, value);
        return value;
      } catch (IOException e) {
        e.printStackTrace();
        throw new IllegalArgumentException(prop.getName() + " never specified.");
      }
    } else {
      throw new IllegalArgumentException(prop.getName() + " never specified.");
    }
  }
  
  public File getValueAsFile(Property prop) {
    return new File(getValue(prop));
  }
  
  public int getValueAsInt(Property prop) {
    return Integer.parseInt(getValue(prop));
  }

  public synchronized boolean isSet(Property prop) {
    if (!prop.isFlag()) {
      throw new IllegalArgumentException(prop.getName() + " is not a flag");
    } else {
      return propertyMap.containsKey(prop);
    }
  }
  
  public synchronized static void initializeProperties() {
    initializeProperties(null);
  }
  
  @SuppressWarnings("unchecked")
  public synchronized static void initializeProperties(String[] args) {
    if (singleton == null) {
      PropertyManager properties = new PropertyManager();
      
      if (args != null) {
        for (int index = 0; index < args.length;) {
          if (args[index].startsWith("--")) {
            Property prop = Property.parse(args[index].substring(2));
            if (prop == null) {
              throw new IllegalArgumentException("Unknown property " + args[index]);
            } else {
              if (prop.isFlag()) {
                properties.setProperty(prop, "");
                index++;
              } else if (++index < args.length) {
                properties.setProperty(prop, args[index++]);
              }
            }
          } else {
            index++;
          }
        }
      
        if (properties.hasValue(Property.PROPERTIES_FILE)) {
          try {
            FileInputStream fis = new FileInputStream(properties.getValue(Property.PROPERTIES_FILE));
    
            Properties props = new Properties();
            props.load(fis);
            fis.close();
            
            for (Entry<String, String> entry : (Set<Entry<String,String>>)(Object)props.entrySet()) {
              Property prop = Property.parse(entry.getKey());
              if (prop == null) {
                throw new IllegalArgumentException("Unknown property from file " + entry.getKey());
              } else {
                properties.setProperty(prop, entry.getValue());
              }
            }
          } catch (IOException e) {
            throw new IllegalArgumentException("Unable to find properties file at " + properties.getValue(Property.PROPERTIES_FILE));
          }
        }
      }
      singleton = properties;
    } else {
      throw new IllegalStateException("Attempt to initialize PropertyManager twice!");
    }
  }
  
  public synchronized static PropertyManager getProperties() {
    if (singleton == null) {
      throw new IllegalStateException("Properties not initialized");
    } else {
      return singleton;
    }
  }
}