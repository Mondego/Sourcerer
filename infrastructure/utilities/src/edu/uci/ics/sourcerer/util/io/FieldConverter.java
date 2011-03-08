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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FieldConverter {
  private Field field;
  private FieldConverterHelper helper;
  
  private FieldConverter(Field field, FieldConverterHelper helper) {
    this.field = field;
    this.helper = helper;
    field.setAccessible(true);
  }
  
  public void set(Object obj, String value) throws IllegalAccessException {
    if (!value.equals("null")) {
      field.set(obj, helper.makeFromString(value));
    }
  }
  
  public String get(Object obj) throws IllegalAccessException {
    Object val = field.get(obj);
    return helper.writeToString(val);
  }
  
  private static Map<Class<?>, FieldConverterHelper> helperMap = Helper.newHashMap();
  
  public static void registerConverterHelper(Class<? extends LWRec> klass, FieldConverterHelper helper) {
    helperMap.put(klass, helper);
  }
  
  protected static FieldConverter getFieldConverter(Field field) {
    FieldConverterHelper helper = helperMap.get(field.getType());
    if (helper == null) {
      throw new IllegalArgumentException("Unsupported type: " + field.getType().getName());
    } else {
      return new FieldConverter(field, helper); 
    }
  }
  
  public abstract static class FieldConverterHelper {
    protected abstract Object makeFromString(String value) throws IllegalAccessException;
    protected final String writeToString(Object o) throws IllegalAccessException {
      if (o == null) {
        return "null";
      } else {
        if (o instanceof LWRec) {
          return ((LWRec)o).writeToString();
        } else {
          String val = o.toString();
          if (val.equals("null")) {
            logger.log(Level.SEVERE, "null collision!");
          } 
          return o.toString();
        }
      }
    }
  }
  
  private static final FieldConverterHelper INT_HELPER = new FieldConverterHelper() {
    @Override
    protected Object makeFromString(String value) {
      return Integer.parseInt(value);
    }
  };
  
  private static final FieldConverterHelper BOOLEAN_HELPER = new FieldConverterHelper() {
    @Override
    protected Object makeFromString(String value) {
      return Boolean.parseBoolean(value);
    }
  };
  
  private static final FieldConverterHelper STRING_HELPER = new FieldConverterHelper() {
    @Override
    protected Object makeFromString(String value) {
      return value;
    }
  };
  
  static {
    helperMap.put(Integer.class, INT_HELPER);
    helperMap.put(Integer.TYPE, INT_HELPER);
    helperMap.put(Boolean.class, BOOLEAN_HELPER);
    helperMap.put(Boolean.TYPE, BOOLEAN_HELPER);
    helperMap.put(String.class, STRING_HELPER);
  }
}
