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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class Property <T> {
  public static final Property<Boolean> PROMPT_MISSING = new BooleanProperty("prompt-missing", false, "Prompt for missing properties.").register("General");
  
  protected String name;
  protected boolean initialized;
  protected T value;
  protected T defaultValue;
  protected Property<?>[] requiredProperties;

  protected Collection<Property<?>> requiredBy;
  protected String description;
  
  protected boolean optional = false;
  protected boolean allowNull = false;
  
  protected Property(String name, T defaultValue, String description) {
    this.name = name;
    this.defaultValue = defaultValue;
    this.description = description;
    if (defaultValue == null) {
      allowNull = true;
    }
  }
  
  protected void isRequiredBy(Property<?> prop) {
    if (requiredBy == null) {
      requiredBy = Helper.newLinkedList();
    }
    requiredBy.add(prop);
  }
  
  public Property<T> makeOptional() {
    optional = true;
    return this;
  }
  
  public Property<T> register(String category) {
    PropertyManager.registerProperty(category, this);
    return this;
  }
  
  public Property<T> setRequiredProperties(Property<?> ... properties) {
    if (requiredProperties == null) {
      requiredProperties = properties;
      return this;
    } else {
      throw new IllegalStateException("May not require more than one set of properties.");
    }
  }
  
  public String getName() {
    return name;
  }
  
  public abstract String getType();
  
  protected Property<?>[] getRequiredProperties() {
    if (requiredProperties == null) {
      return new Property<?>[0];
    } else {
      return requiredProperties;
    }
  }
  
  protected String toString(T value) {
    return value.toString();
  }
  
  public String getDescription() {
    String required = "";
    if (requiredBy != null) {
      StringBuilder builder = new StringBuilder(" Required by");
      for (Property<?> prop : requiredBy) {
        builder.append(' ').append(prop.getName()).append(',');
      }
      builder.setCharAt(builder.length() - 1, '.');
      required = builder.toString();
    }
    if (defaultValue == null) {
      return description + required;
    } else {
      return description + " Defaults to " + toString(defaultValue) + "." + required;
    }
  }
  
  public String toString() {
    return name;
  }
  
  public boolean isNotOptional() {
    return !optional;
  }
  
  public boolean hasValue() {
    if (!initialized) {
      initializeValue();
    }
    return value != null || defaultValue != null;
  }
  
  public synchronized void setValue(T value) {
    this.value = value;
    initialized = true;
  }
  
  public synchronized T getValue() {
    if (!initialized) {
      initializeValue();
    }
    if (value == null && !allowNull) {
      throw new IllegalStateException(name + " never specified.");
    } else {
      return value;
    }
  }
  
  public T getDefaultValue() {
    return defaultValue;
  }
  
  protected abstract T parseString(String value);
  
  private synchronized void initializeValue() {
    if (!initialized) {
      PropertyManager properties = PropertyManager.getProperties();
      String stringValue = properties.getValue(name);
      if (stringValue == null) {
        stringValue = System.getProperty(name);
        if (stringValue == null) {
          if (defaultValue == null && this != PROMPT_MISSING && PROMPT_MISSING.getValue() && isNotOptional()) {
            try {
              System.out.print("Please enter value for " + name + ":");
              BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
              stringValue = br.readLine();
              value = parseString(stringValue);
            } catch (Exception e) {
              logger.log(Level.SEVERE, "Unable to read value for " + name, e);
              throw new RuntimeException(e);
            }
          } else {
            value = defaultValue;
          }
        } else {
          value = parseString(stringValue);
        }
      } else {
        value = parseString(stringValue);
      }
      initialized = true;
    }
  }
}
