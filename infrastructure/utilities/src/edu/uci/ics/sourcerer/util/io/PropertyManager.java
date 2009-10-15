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
  public static final Property<Boolean> PROMPT_MISSING = null;
  public static final Property<File> PROPERTIES_FILE = null;
  
  private static PropertyManager singleton = null;
  private Map<Property<?>, PropertyValue<?>> propertyMap;
  private Map<String, String> inputMap;
  
  private PropertyManager() {
    propertyMap = Helper.newHashMap();
    inputMap = Helper.newHashMap();
  }
    
  public synchronized void setProperty(Property<String> prop, String value) {
    if (prop.isStringProperty()) {
      Helper.getSubFromMap(propertyMap, prop, PropertyValue.StringValue.class).setValue(value);
    } else {
      throw new IllegalArgumentException(prop + " is not a string property.");
    }
  }
  
  public synchronized void setProperty(Property<Boolean> prop, boolean value) {
    if (prop.isBooleanProperty()) {
      Helper.getSubFromMap(propertyMap, prop, PropertyValue.BooleanValue.class).setValue(value);
    } else {
      throw new IllegalArgumentException(prop + " is not a boolean property.");
    }
  }
  
  public synchronized boolean hasValue(Property<?> prop) {
    return propertyMap.containsKey(prop) || prop.hasDefaultValue();
  }
  
  @SuppressWarnings("unchecked")
  public synchronized <T> T getValue(Property<T> prop) {
    if (propertyMap.containsKey(prop)) {
      return (T) propertyMap.get(prop).getValue();
    } else if (prop.hasDefaultValue()) {
      return prop.getDefaultValue().getValue();
    } else if (isSet(PROMPT_MISSING)) {
      try {
        System.out.print("Please enter value for " + prop.getName() + ":");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String value = br.readLine();
        PropertyValue<T> propValue = prop.parseValue(value);
        propertyMap.put(prop, propValue);
        return propValue.getValue();
      } catch (IOException e) {
        e.printStackTrace();
        throw new IllegalArgumentException(prop.getName() + " never specified.");
      }
    } else {
      throw new IllegalArgumentException(prop.getName() + " never specified.");
    }
  }
  
  public synchronized <T> boolean isSet(Property<T> prop) {
    if (prop.isBooleanProperty()) {
      return (Boolean) getValue(prop);
    } else {
      throw new IllegalArgumentException(prop.getName() + " is not a boolean property");
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
            String prop = args[index].substring(2);
            if (args[index + 1].startsWith("--")) {
              properties.inputMap.put(prop, "--");
            } else {
              properties.inputMap.put(prop, args[++index]);
            }
          } else {
            throw new IllegalArgumentException(args[index] + " from " + args + " is invalid");
          }
        }
      
        if (properties.hasValue(PROPERTIES_FILE)) {
          try {
            FileInputStream fis = new FileInputStream(properties.getValue(PROPERTIES_FILE));
    
            Properties props = new Properties();
            props.load(fis);
            fis.close();
  
            for (Entry<String, String> entry : (Set<Entry<String,String>>)(Object)props.entrySet()) {
              properties.inputMap.put(entry.getKey(), entry.getValue());
            }
          } catch (IOException e) {
            throw new IllegalArgumentException("Unable to find properties file at " + properties.getValue(PROPERTIES_FILE));
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