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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter.Alignment;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;
import edu.uci.ics.sourcerer.util.io.properties.FileProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class PropertyManager {
  public static final Property<Boolean> PRINT_USAGE = new BooleanProperty("usage", false, "General", "Prints usage information.");
  public static final Property<File> PROPERTIES_FILE = new FileProperty("properties-file", "General", "File containing Java-style property bindings.").makeOptional();
  public static final Property<Boolean> REQUIRE_REGISTERED = new BooleanProperty("require-registered-properties", true, "General", "Terminate execution if registered property not present.");
  
  private static PropertyManager singleton = null;
  private static Map<String, Collection<Property<?>>> usedProperties = null;
  private Map<String, String> propertyMap;
  
  private PropertyManager() {
    propertyMap = Helper.newHashMap();
  }
  
  protected synchronized String getValue(String name) {
    return propertyMap.get(name);
  }
  
  public synchronized static void registerResumeLoggingProperties() {
    
  }
  
  private static void addProperty(Property<?> prop) {
    Helper.getFromMap(usedProperties, prop.category, HashSet.class).add(prop);
  }
  
  public synchronized static void registerAndVerify(Property<?> ... properties) {
    registerUsedProperties(properties);
    verifyUsage();
  }
  
  public synchronized static void registerUsedProperties(Property<?> ... properties) {
    if (usedProperties == null) {
      usedProperties = Helper.newHashMap();   
      addProperty(PRINT_USAGE);
      addProperty(REQUIRE_REGISTERED);
    }
    for (Property<?> prop : properties) {
      addProperty(prop);
    }
  }
  
  public synchronized static void printUsage() {
    TablePrettyPrinter printer = TablePrettyPrinter.getCommandLinePrettyPrinter();
    printer.beginTable(3);
    printer.addDividerRow();
    for (Map.Entry<String, Collection<Property<?>>> entry : usedProperties.entrySet()) {
      printer.beginRow();
      printer.addCell(entry.getKey() + " Properties", 3, Alignment.CENTER);
      printer.addDividerRow();
      for (Property<?> property : entry.getValue()) {
        printer.beginRow();
        printer.addCell(property.getName());
        printer.addCell(property.getType());
        printer.addCell(property.getDescriptionWithDefault());
      }
      printer.addDividerRow();
    }
    printer.endTable();
    System.exit(0);
  }
  
  public synchronized static void verifyUsage() {
    boolean problem = false;
    for (Collection<Property<?>> properties : usedProperties.values()) {
      for (Property<?> prop : properties) {
        if (!prop.hasValue() && prop.isNotOptional()) {
          logger.log(Level.SEVERE, prop.getName() + " not specified.");
          problem = true;
        }
      }
    }
    if (problem && REQUIRE_REGISTERED.getValue()) {
      printUsage();
    }
    printUsageIfRequested();
  }
  
  public synchronized static void printUsageIfRequested() {
    if (PRINT_USAGE.getValue()) {
      printUsage();
    }
  }
  
  public synchronized static void initializeProperties() {
    initializeProperties(null);
  }
  
  @SuppressWarnings("unchecked")
  public synchronized static void initializeProperties(String[] args) {
    if (singleton == null) {
      singleton = new PropertyManager();
      
      if (args != null) {
        for (int index = 0; index < args.length; index++) {
          if (args[index].startsWith("--")) {
            String prop = args[index].substring(2);
            if (index == args.length - 1 || args[index + 1].startsWith("--")) {
              singleton.propertyMap.put(prop, "true");
            } else {
              singleton.propertyMap.put(prop, args[++index]);
            }
          } else {
            throw new IllegalArgumentException(args[index] + " from " + args + " is invalid");
          }
        }
      
        if (PROPERTIES_FILE.hasValue()) {
          try {
            FileInputStream fis = new FileInputStream(PROPERTIES_FILE.getValue());
    
            Properties props = new Properties();
            props.load(fis);
            fis.close();
  
            for (Entry<String, String> entry : (Set<Entry<String,String>>)(Object)props.entrySet()) {
              singleton.propertyMap.put(entry.getKey(), entry.getValue());
            }
          } catch (IOException e) {
            throw new IllegalArgumentException("Unable to find properties file at " + PROPERTIES_FILE.getValue().getPath());
          }
        }
      }
    } else {
      throw new IllegalStateException("Attempt to initialize PropertyManager twice!");
    }
  }
  
  protected synchronized static PropertyManager getProperties() {
    if (singleton == null) {
      throw new IllegalStateException("Properties not initialized");
    } else {
      return singleton;
    }
  }
}