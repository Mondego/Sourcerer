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
package edu.uci.ics.sourcerer.util.io.internal;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.CustomSimpleSerializable;
import edu.uci.ics.sourcerer.util.io.LineBuilder;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
abstract class ObjectSerializer {
  private ObjectSerializer() {}
  
  static ObjectSerializer makeCustomSerializer() {
    return new CustomSerializer();
  }
  
  static ObjectSerializer makeBasicSerializer(Field field) {
    return new BasicSerializer(field);
  }
  
  abstract String serialize(Object o);
  
  private static String writeToString(Object o) {
    if (o == null) {
      return "null";
    } else if (o instanceof CustomSimpleSerializable) {
      return ((CustomSimpleSerializable) o).serialize();
    } else if (o.getClass().isArray()) {
      LineBuilder result = new LineBuilder();
      int length = Array.getLength(o);
      result.addItem(length);
      for (int i = 0; i < length; i++) {
        result.addItem(writeToString(Array.get(o, i)));
      }
      return result.toLine();
    } else if (o instanceof String) {
      String val = o.toString();
      if (val.indexOf('*') != -1) {
        logger.log(Level.SEVERE, "* collission for " + val);
      }
      if (val.equals("null")) {
        logger.log(Level.SEVERE, "null collission");
      }
      return val.replace(' ', '*');
    } else if (o instanceof Enum<?>) {
      return ((Enum<?>)o).name();
    } else {
      String val = o.toString();
      if (val.equals("null")) {
        logger.log(Level.SEVERE, "null collission");
      }
      if (val.indexOf(' ') != -1) {
        logger.log(Level.SEVERE, "cannot write value with space: " + val);
        return null;
      } else {
        return val;
      }
    }
  }
  
  private static class BasicSerializer extends ObjectSerializer {
    private Field field;
    
    private BasicSerializer(Field field) {
      this.field = field;
      field.setAccessible(true);
    }

    @Override
    String serialize(Object o) {
      try {
        if (o == null) {
          return writeToString(null);
        } else {
          return writeToString(field.get(o));
        }
      } catch (IllegalArgumentException e) {
        logger.log(Level.SEVERE, "Error getting field value.", e);
        return writeToString(null);
      } catch (IllegalAccessException e) {
        logger.log(Level.SEVERE, "Error getting field value.", e);
        return writeToString(null);
      }
    }
  }
  
  private static class CustomSerializer extends ObjectSerializer {
    private CustomSerializer() {}

    @Override
    String serialize(Object o) {
      return writeToString(o);
    }
  }
}
