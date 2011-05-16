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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FieldConverter {
  private Field field;
  private FieldConverterHelper helper;
  
  private FieldConverter(Field field) {
    this.field = field;
    field.setAccessible(true);
  }
  
  private FieldConverter(Field field, FieldConverterHelper helper) {
    this.field = field;
    this.helper = helper;
    field.setAccessible(true);
  }
  
  public void set(Object obj, Scanner scanner) throws IllegalAccessException {
    if (helper == null) {
      throw new IllegalArgumentException("Helper may not be null");
    } else {
      field.set(obj, helper.makeFromScanner(scanner));
    }
  }
  
  public String get(Object obj) throws IllegalAccessException {
    if (field == null) {
      return writeToString(obj);
    } else {
      Object val = field.get(obj);
      return writeToString(val);
    }
  }
  
  private String writeToString(Object o) throws IllegalAccessException {
    if (o == null) {
      return "null";
    } else {
      if (o instanceof LWRec) {
        return ((LWRec)o).writeToString();
      } else if (o.getClass().isArray()) {
        LineBuilder result = new LineBuilder();
        int length = Array.getLength(o);
        result.addItem(length);
        for (int i = 0; i < length; i++) {
          result.addItem(writeToString(Array.get(o, i)));
        }
        return result.toLine();
      } else {
        String val = o.toString();
        if (val.equals("null")) {
          logger.log(Level.SEVERE, "null collision!");
        } 
        return o.toString();
      }
    }
  }
  
  private static Map<Class<?>, FieldConverterHelper> helperMap = Helper.newHashMap();
  
  public static void registerConverterHelper(Class<? extends LWRec> klass, FieldConverterHelper helper) {
    helperMap.put(klass, helper);
  }
  
  protected static FieldConverter getFieldWriteConverter(Field field) {
    if (field == null) {
      throw new IllegalArgumentException("Field may not be null"); 
    } else {
      return new FieldConverter(field); 
    }
  }
  
  protected static FieldConverter getLWRecWriteConverter() {
    return new FieldConverter(null);
  }
  
  protected static FieldConverter getFieldReadConverter(Field field) {
    if (field == null) {
      throw new IllegalArgumentException("Field may not be null"); 
    } else {
      FieldConverterHelper helper = getHelper(field.getType());
      if (helper == null) {
        throw new IllegalArgumentException("Unsupported type: " + field.getType().getName());
      } else {
        return new FieldConverter(field, helper); 
      }
    }
  }
  
  public static FieldConverterHelper getHelper(final Class<?> type) {
    if (type.isArray()) {
      return new FieldConverterHelper() {
        private FieldConverterHelper componentHelper = getHelper(type.getComponentType());
        
        @Override
        protected Object makeFromScanner(Scanner scanner) throws IllegalAccessException {
          int length = scanner.nextInt();
          Object array = Array.newInstance(type.getComponentType(), length);
          for (int i = 0; i < length; i++) {
            Array.set(array, i, componentHelper.makeFromScanner(scanner));
          }
          return array;
        }
      };
    } else {
      FieldConverterHelper helper = helperMap.get(type);
      if (helper == null) {
        try {
          Method method = type.getMethod("registerConverterHelper");
          method.invoke(null);
          helper = helperMap.get(type);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
      }
      return helper;
    }
  }
  
  protected static FieldConverter getNullReadConverter(Field field) {
    if (field == null) {
      throw new IllegalArgumentException("Field may not be null");
    } else {
      final FieldConverterHelper helper = helperMap.get(field.getType());
      if (helper == null) {
        throw new IllegalArgumentException("Unsupported type: " + field.getType().getName());
      } else {
        return new FieldConverter(field, new FieldConverterHelper() {
          @Override
          protected Object makeFromScanner(Scanner scanner) throws IllegalAccessException {
            helper.makeFromScanner(scanner);
            return null;
          }
        }) ;
      }
    }
  }
  
  public abstract static class FieldConverterHelper {
    protected abstract Object makeFromScanner(Scanner scanner) throws IllegalAccessException;
  }
  
  private static final FieldConverterHelper INT_HELPER = new FieldConverterHelper() {
    @Override
    protected Object makeFromScanner(Scanner scanner) {
      return scanner.nextInt();
    }
  };
  
  private static final FieldConverterHelper LONG_HELPER = new FieldConverterHelper() {
    @Override
    protected Object makeFromScanner(Scanner scanner) {
      return scanner.nextLong();
    }
  };
  
  
  private static final FieldConverterHelper BOOLEAN_HELPER = new FieldConverterHelper() {
    @Override
    protected Object makeFromScanner(Scanner scanner) {
      return scanner.nextBoolean();
    }
  };
  
  private static final FieldConverterHelper STRING_HELPER = new FieldConverterHelper() {
    @Override
    protected Object makeFromScanner(Scanner scanner) {
      return scanner.next();
    }
  };
  
  static {
    helperMap.put(Integer.class, INT_HELPER);
    helperMap.put(Integer.TYPE, INT_HELPER);
    helperMap.put(Long.class, LONG_HELPER);
    helperMap.put(Long.TYPE, LONG_HELPER);
    helperMap.put(Boolean.class, BOOLEAN_HELPER);
    helperMap.put(Boolean.TYPE, BOOLEAN_HELPER);
    helperMap.put(String.class, STRING_HELPER);
  }
}
