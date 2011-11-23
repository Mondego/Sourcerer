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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class Property<T> {
  private String name;
  private T value;
  private boolean initialized;
  private AbstractProperties properties;
  
  protected Property(String name, AbstractProperties properties) {
    this.name = name;
    this.properties = properties;
    properties.registerProperty(this);
  }
  
  protected abstract T parseValue(String value);
  
  protected String toString(T value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }
  
  protected String getName() {
    return name;
  }
  
  public T getValue() {
    if (!initialized) {
      String val = properties.getValue(name);
      if (val != null) {
        value = parseValue(val);
      }
      initialized = true;
    }
    return value;
  }
  
  protected void copy(Property<?> other) {
    setValue(parseValue(other.getValueAsString()));
  }
  
  protected String getValueAsString() {
    return toString(getValue());
  }
  
  public void setValue(T value) {
    this.value = value;
    this.initialized = true;
  }
  
  protected void reset() {
    this.value = null;
    initialized = false;
  }
}
