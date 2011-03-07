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

import java.lang.reflect.Field;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class FieldConverter {
  private Field field;
  
  private FieldConverter(Field field) {
    this.field = field;
    field.setAccessible(true);
  }
  
  public void set(Object obj, String value) throws IllegalAccessException {
    field.set(obj, convert(value));
  }
  
  protected abstract Object convert(String value);
  
  protected static FieldConverter getFieldConverter(Field field) {
    if (field.getType().equals(Integer.class)) {
      return new FieldConverter(field) {
        protected Object convert(String value) {
          return Integer.parseInt(value);
        }
      };
    } else if (field.getType().equals(String.class)) {
      return new FieldConverter(field) {
        protected Object convert(String value) {
          return value;
        }
      };
    } else {
      throw new IllegalArgumentException("Unsupported type: " + field.getType().getName());
    }
  }
}
