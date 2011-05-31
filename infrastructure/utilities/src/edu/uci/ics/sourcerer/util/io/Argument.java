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
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class Argument <T> {
  public static final Argument<Boolean> PROMPT_MISSING = new BooleanArgument("prompt-missing", false, "Prompt for missing properties.").register("General");
  
  protected String name;
  protected boolean initialized;
  protected T value;
  protected T defaultValue;
  protected Argument<?>[] requiredProperties;

  protected Collection<Argument<?>> requiredBy;
  protected String description;
  
  protected boolean optional = false;
  protected boolean allowNull = false;
  
  protected Argument(String name, T defaultValue, String description) {
    this.name = name;
    this.defaultValue = defaultValue;
    this.description = description;
    if (defaultValue == null) {
      allowNull = true;
    }
  }
  
  protected void isRequiredBy(Argument<?> prop) {
    if (requiredBy == null) {
      requiredBy = Helper.newLinkedList();
    }
    requiredBy.add(prop);
  }
  
  public Argument<T> makeOptional() {
    optional = true;
    return this;
  }
  
  public Argument<T> register(String category) {
    ArgumentManager.registerProperty(category, this);
    return this;
  }
  
  public Argument<T> setRequiredProperties(Argument<?> ... properties) {
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
  
  protected Argument<?>[] getRequiredProperties() {
    if (requiredProperties == null) {
      return new Argument<?>[0];
    } else {
      return requiredProperties;
    }
  }
  
  public String getDescription() {
    String required = "";
    if (requiredBy != null) {
      StringBuilder builder = new StringBuilder(" Required by");
      for (Argument<?> prop : requiredBy) {
        builder.append(' ').append(prop.getName()).append(',');
      }
      builder.setCharAt(builder.length() - 1, '.');
      required = builder.toString();
    }
    if (hasDefaultValue()) {
      return description + " Defaults to " + getDefaultString() + "." + required;
    } else {
      return description + required;
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
    return value != null || hasDefaultValue();
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
  
  public boolean hasDefaultValue() {
    return getDefaultValue() != null;
  }
  
  public T getDefaultValue() {
    return defaultValue;
  }
  
  protected String getDefaultString() {
    return defaultValue.toString();
  }
  
  protected abstract T parseString(String value);
  
  private synchronized void initializeValue() {
    if (!initialized) {
      ArgumentManager properties = ArgumentManager.getProperties();
      String stringValue = properties.getValue(name);
      if (stringValue == null) {
        stringValue = System.getProperty(name);
        if (stringValue == null) {
          if (hasDefaultValue() && this != PROMPT_MISSING && PROMPT_MISSING.getValue() && isNotOptional()) {
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
            value = getDefaultValue();
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
