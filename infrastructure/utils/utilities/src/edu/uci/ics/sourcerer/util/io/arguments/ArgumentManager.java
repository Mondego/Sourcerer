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
package edu.uci.ics.sourcerer.util.io.arguments;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter.Alignment;
import edu.uci.ics.sourcerer.util.io.logging.Logging;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ArgumentManager {
  private static ArgumentManager singleton = null;
  
  public static final Argument<Boolean> HELP = new BooleanArgument("help", false, "Prints usage information.").permit();
  public static final Argument<File> PROPERTIES_FILE = new FileArgument("properties-file", null, "File containing Java-style property bindings.").permit();
  public static final Argument<InputStream> PROPERTIES_STREAM = new StreamArgument("properties-stream", "Stream containing Java-style property bindings.").permit();
  
  private Map<String, String> propertyMap;
  
  private ArgumentManager() {
    propertyMap = new HashMap<>();
  }
  
  protected synchronized String getValue(String name) {
    return propertyMap.get(name);
  }
  
  static void executeCommand(String[] args, Class<?> klass) {
    Command activeCommand = null;
    Collection<Argument<?>> commandProps = null;
    Collection<Argument<?>> missingProperties = new LinkedList<>();
    Collection<Argument<?>[]> invalidConditionals = new LinkedList<>();
    
    synchronized(ArgumentManager.class) {
      initializeProperties(args);
      ArgumentManager properties = getProperties();
      
      // Populate the command list reflectively
      LinkedList<Command> commands = new LinkedList<>();
      for (Field field : klass.getFields()) {
        if (Command.class.isAssignableFrom(field.getType()) && field.getAnnotation(Command.Disable.class) == null) {
          try {
            commands.add((Command) field.get(klass));
          } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Unable to access " + field.getName() + " from " + klass.getName(), e);
          }
        }
      }
      
      LinkedList<Command> activeCommands = new LinkedList<>();
      for (Command command : commands) {
        if (properties.getValue(command.getName()) != null) {
          activeCommands.add(command);
        }
      }
           
      // Make sure only one command was selected
      if (activeCommands.size() == 0) {
        logger.log(Level.SEVERE, "No command selected.");
        printCommands(commands);
        return;
      } else if (activeCommands.size() > 1) {
        StringBuilder mult = new StringBuilder();
        for (Command c : activeCommands) {
          mult.append(" ").append(c.getName()).append(",");
        }
        mult.setCharAt(mult.length() - 1, '.');
        logger.log(Level.SEVERE, "Multiple commands selected:" + mult.toString());
        printCommands(commands);
        return;
      } else {
        activeCommand = activeCommands.getFirst();
      }
         
      // Add all the command properties
      Deque<Argument<?>> stack = Helper.newStack();
      stack.addAll(Arrays.asList(activeCommand.getProperties()));
      for (Argument<?>[] conditionals : activeCommand.getConditionalProperties()) {
        for (Argument<?> prop : conditionals) {
//          stack.add(prop.makeOptional());
          stack.add(prop);
        }
      }
      commandProps = new LinkedList<>();
      while (!stack.isEmpty()) {
        Argument<?> prop = stack.pop();
        commandProps.add(prop.permit());
        for (Argument<?> required : prop.getRequiredArguments()) {
          if (!Boolean.FALSE.equals(prop.getValue()) && (prop.hasValue() || Boolean.TRUE.equals(prop.getValue()))) {
            required.isRequiredBy(prop);
            stack.add(required);            
          }
        }
      }
      
      // Verify that the required properties are present

      
      // Check the conditional properties
      for (Argument<?>[] conditionals : activeCommand.getConditionalProperties()) {
        boolean foundOne = false;
        for (Argument<?> prop : conditionals) {
          if (prop.hasValue()) {
            if (foundOne) {
              invalidConditionals.add(conditionals);
              break;
            } else {
              foundOne = true;
            }
          }
        }
        if (!foundOne) {
          invalidConditionals.add(conditionals);
        }
      }
      
      // Check all of the registered properties
      for (Argument<?> prop : commandProps) {
        if (/*prop.isNotOptional() && */!prop.hasValue()) {
          missingProperties.add(prop);
        }
      }
    }
    
    if (HELP.getValue()) {
      printHelp(activeCommand, commandProps);
    } else if (missingProperties.isEmpty() && invalidConditionals.isEmpty()) {
      activeCommand.execute();
    } else {
      printCommand(activeCommand, missingProperties, invalidConditionals);
    }
  }
  
  private static void printCommands(Collection<Command> commands) {
    Logging.REPORT_TO_CONSOLE.setValue(true);
    TablePrettyPrinter printer = TablePrettyPrinter.getLoggerPrettyPrinter();
    printer.addHeader("A single command must be chosen per execution. All commands should be prefixed by '--'. To view the list of properties for a single command, type --<command> --help. The following commands are available...");
    printer.beginTable(2, 57);
    printer.makeColumnWrappable(1);
    printer.addDividerRow();
    for (Command command : commands) {
      printer.beginRow();
      printer.addCell(command.getName());
      printer.addCell(command.getDescription());
    }
    printer.addDividerRow();
    printer.endTable();
    printer.close();
  }
  
  private static void printCommand(Command command, Collection<Argument<?>> missingProperties, Collection<Argument<?>[]> invalidConditionals) {
    Logging.REPORT_TO_CONSOLE.setValue(true);
    TablePrettyPrinter printer = TablePrettyPrinter.getLoggerPrettyPrinter();
    if (!missingProperties.isEmpty()) {
      printer.addHeader("The following required properties for " + command.getName() + " were not supplied. Properties should be specified as --<name> <value>.");
      printer.beginTable(3, 57);
      printer.makeColumnWrappable(2);
      printer.addDividerRow();
      for (Argument<?> prop : missingProperties) {
        printer.beginRow();
        printer.addCell(prop.getName());
        printer.addCell(prop.getType());
        printer.addCell(prop.getDescription());
      }
      printer.addDividerRow();
      printer.endTable();
    }
    if (!invalidConditionals.isEmpty()) {
      for (Argument<?>[] conditional : invalidConditionals) {
        printer.addHeader("Exactly one of following required properties for " + command.getName() + " must be supplied. Properties should be specified as --<name> <value>.");
        printer.beginTable(3, 57);
        printer.makeColumnWrappable(2);
        printer.addDividerRow();
        for (Argument<?> prop : conditional) {
          printer.beginRow();
          printer.addCell(prop.getName());
          printer.addCell(prop.getType());
          printer.addCell(prop.getDescription());
        }
        printer.addDividerRow();
        printer.endTable();
      }
    }
    printer.close();
  }
  
  private synchronized static void printHelp(Command command, Collection<Argument<?>> args) {
    Logging.REPORT_TO_CONSOLE.setValue(true);
    TablePrettyPrinter printer = TablePrettyPrinter.getLoggerPrettyPrinter();
    printer.addHeader("The following properties are available for " + command.getName() + ". Properties should be specified as --<name> <value>.");
    printer.beginTable(3, 57);
    printer.makeColumnWrappable(2);
    
    // Start with the command properties
    printer.addDividerRow();
    printer.beginRow();
    printer.addCell(command.getName(), 3, Alignment.CENTER);
    printer.addDividerRow();
    for (Argument<?> prop : args) {
      printer.beginRow();
      printer.addCell(prop.getName());
      printer.addCell(prop.getType());
      printer.addCell(prop.getDescription());
    }
    
    printer.addDividerRow();
    printer.endTable();
    printer.close();
  }
  
  public synchronized static void initializeProperties() {
    initializeProperties(null);
  }
  
  @SuppressWarnings("unchecked")
  public synchronized static void initializeProperties(String[] args) {
    if (singleton == null) {
      singleton = new ArgumentManager();
    }
      
    if (args != null) {
      for (int index = 0; index < args.length; index++) {
        if (args[index].startsWith("--")) {
          String prop = args[index].substring(2);
          String value = null;
          if (index == args.length - 1 || args[index + 1].startsWith("--")) {
            value = "true";
          } else {
            value = args[++index];
          }
          if (singleton.propertyMap.containsKey(prop)) {
            logger.log(Level.SEVERE, "Duplicate value for " + prop + ": ignoring " + value + " for " + singleton.propertyMap.get(prop));
          } else {
            singleton.propertyMap.put(prop, value);
          }
        } else {
          logger.log(Level.SEVERE, args[index] + " is invalid (expecting --)");
        }
      }
    }

    if (PROPERTIES_FILE.getValue() != null) {
      try {
        FileInputStream fis = new FileInputStream(PROPERTIES_FILE.getValue());

        Properties props = new Properties();
        props.load(fis);
        fis.close();

        for (Entry<String, String> entry : (Set<Entry<String, String>>) (Object) props.entrySet()) {
          if (singleton.propertyMap.containsKey(entry.getKey())) {
            logger.log(Level.SEVERE, "Duplicate value for " + entry.getKey() + ": ignoring " + entry.getValue() + " for " + singleton.propertyMap.get(entry.getKey()));
          } else {
            singleton.propertyMap.put(entry.getKey(), entry.getValue());
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to find properties file at " + PROPERTIES_FILE.getValue().getPath());
      }
    }

    if (PROPERTIES_STREAM.getValue() != null) {
      try {
        Properties props = new Properties();
        props.load(PROPERTIES_STREAM.getValue());
        PROPERTIES_STREAM.getValue().close();

        for (Entry<String, String> entry : (Set<Entry<String, String>>) (Object) props.entrySet()) {
          if (singleton.propertyMap.containsKey(entry.getKey())) {
            logger.log(Level.SEVERE, "Duplicate value for " + entry.getKey() + ": ignoring " + entry.getValue() + " for " + singleton.propertyMap.get(entry.getKey()));
          } else {
            singleton.propertyMap.put(entry.getKey(), entry.getValue());
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to load properties stream", e);
      }
    }
  }
  
  protected synchronized static ArgumentManager getProperties() {
    if (singleton == null) {
      throw new IllegalStateException("Properties not initialized");
    } else {
      return singleton;
    }
  }
}