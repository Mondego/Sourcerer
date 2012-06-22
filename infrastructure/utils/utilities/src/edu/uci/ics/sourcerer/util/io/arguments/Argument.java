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

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class Argument <T> {
  public static final Argument<Boolean> PROMPT_MISSING = new BooleanArgument("prompt-missing", false, "Prompt for missing properties.").permit();
  
  private String name;
  private boolean initialized;
  private T value;
  private T defaultValue;
  private Argument<?>[] requiredArguments;

  private Collection<Argument<?>> requiredBy;
  private String description;
  
  private final boolean hasDefault;
  
  private boolean permitted = false;
  
  protected Argument(String name, String description) {
    this.name = name;
    this.defaultValue = null;
    this.hasDefault = false;
    this.description = description;
  }
  
  protected Argument(String name, T defaultValue, String description) {
    this.name = name;
    this.defaultValue = defaultValue;
    this.hasDefault = true;
    this.description = description;
  }
  
  protected final void isRequiredBy(Argument<?> prop) {
    if (requiredBy == null) {
      requiredBy = new LinkedList<>();
    }
    requiredBy.add(prop);
  }
  
  public Argument<T> permit() {
    permitted = true;
    return this;
  }
   
  public final Argument<T> setRequiredArguments(Argument<?> ... properties) {
    if (requiredArguments == null) {
      requiredArguments = properties;
      return this;
    } else {
      throw new IllegalStateException("May not require more than one set of properties.");
    }
  }
  
  public final String getName() {
    return name;
  }
  
  public abstract String getType();
  
  protected final Argument<?>[] getRequiredArguments() {
    if (requiredArguments == null) {
      return new Argument<?>[0];
    } else {
      return requiredArguments;
    }
  }
  
  public final String getDescription() {
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
  
  public final String toString() {
    if (hasValue()) {
      return name + " (" + getValue() + ")";
    } else {
      return name + " (N/A)";
    }
  }
  
  public final boolean hasValue() {
    if (!initialized) {
      initializeValue();
    }
    return value != null || hasDefault;
  }
  
  public final synchronized void setValue(T value) {
    this.value = value;
    initialized = true;
  }
  
  public final synchronized T getValue() {
    if (!permitted) {
      throw new IllegalStateException(name + " is not registered, and so access is not permitted.");
    }
    if (!initialized) {
      initializeValue();
    }
    if (value == null && !hasDefault) {
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
  
  private final synchronized void initializeValue() {
    if (!initialized) {
      ArgumentManager properties = ArgumentManager.getProperties();
      String stringValue = properties.getValue(name);
      if (stringValue == null) {
        stringValue = System.getProperty(name);
        if (stringValue == null) {
          if (hasDefaultValue()) {
            value = getDefaultValue();
          }
//          if (hasDefaultValue() && this != PROMPT_MISSING && PROMPT_MISSING.getValue() && isNotOptional()) {
//            try {
//              System.out.print("Please enter value for " + name + ":");
//              BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//              stringValue = br.readLine();
//              value = parseString(stringValue);
//            } catch (Exception e) {
//              logger.log(Level.SEVERE, "Unable to read value for " + name, e);
//              throw new RuntimeException(e);
//            }
//          } else {
//            value = getDefaultValue();
//          }
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
