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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class ObjectDeserializer<T> {
  public ObjectDeserializer() {}
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static ObjectDeserializer<?> makeDeserializer(Class<?> klass) {
    if (CustomSimpleSerializable.class.isAssignableFrom(klass)) {
      try {
        Method method = klass.getMethod("deserialize", Scanner.class);
        return new CustomDeserializer(method);
      } catch (SecurityException e) {
        logger.log(Level.SEVERE, "JVM does not have sufficient permissions for custom deserialization.", e);
      } catch (NoSuchMethodException e) {
        logger.log(Level.SEVERE, klass.getName() + " is missing the readFromString method.", e);
      }
      return null;
    } else {
      if (klass.isArray()) {
        return new ArrayDeserializer(klass.getComponentType());
      } else if (klass.isEnum()) {
        return new EnumDeserializer(klass);
      } else {
        ObjectDeserializer<?> deserializer = deserializers.get(klass);
        if (deserializer != null) {
          return deserializer;
        } else {
          logger.log(Level.SEVERE, "No deserializer for " + klass.getName());
          return null;
        }
      }
    }
  }
  
  public abstract T deserialize(Scanner scanner);
  
  private static Map<Class<?>, ObjectDeserializer<?>> deserializers = Helper.newHashMap();
  
  private static ObjectDeserializer<?> intDeserializer = new ObjectDeserializer<Integer>() {
    @Override
    public Integer deserialize(Scanner scanner) {
      if (scanner.hasNextInt()) {
        return scanner.nextInt();
      } else {
        String next = scanner.next();
        if ("null".equals(next)) {
          return null;
        } else {
          logger.log(Level.SEVERE, "Int expected by deserialization, instead got " + next);
          return null;
        }
      }
    }
  };
  
  private static ObjectDeserializer<?> longDeserializer = new ObjectDeserializer<Long>() {
    @Override
    public Long deserialize(Scanner scanner) {
      if (scanner.hasNextLong()) {
        return scanner.nextLong();
      } else {
        String next = scanner.next();
        if ("null".equals(next)) {
          return null;
        } else {
          logger.log(Level.SEVERE, "Long expected by deserialization, instead got " + next);
          return null;
        }
      }
    }
  };
  
  private static ObjectDeserializer<?> booleanDeserializer = new ObjectDeserializer<Boolean>() {
    @Override
    public Boolean deserialize(Scanner scanner) {
      if (scanner.hasNextBoolean()) {
        return scanner.nextBoolean();
      } else {
        String next = scanner.next();
        if ("null".equals(next)) {
          return null;
        } else {
          logger.log(Level.SEVERE, "Boolean" + next);
          return null;
        }
      }
    }
  };
  
  private static ObjectDeserializer<?> stringDeserializer = new ObjectDeserializer<String>() {
    @Override
    public String deserialize(Scanner scanner) {
      String next = scanner.next();
      if ("null".equals(next)) {
        return null;
      } else {
        return next.replace('*', ' ');
      }
    }
  };
  
  static {
    deserializers.put(Integer.class, intDeserializer);
    deserializers.put(Integer.TYPE, intDeserializer);
    deserializers.put(Long.class, longDeserializer);
    deserializers.put(Long.TYPE, longDeserializer);
    deserializers.put(Boolean.class, booleanDeserializer);
    deserializers.put(Boolean.TYPE, booleanDeserializer);
    deserializers.put(String.class, stringDeserializer);
  }
  
  private static class EnumDeserializer<T extends Enum<T>> extends ObjectDeserializer<T> {
    private Class<T> klass;
    
    private EnumDeserializer(Class<T> klass) {
      this.klass = klass;
    }
    
    @Override
    public T deserialize(Scanner scanner) {
      String next = scanner.next();
      if ("null".equals(next)) {
        return null;
      } else {
        return Enum.valueOf(klass, next);
      }
    }
  }
  
  private static class ArrayDeserializer extends ObjectDeserializer<Object> {
    private Class<?> component;
    private ObjectDeserializer<?> componentDeserializer;
    
    private ArrayDeserializer(Class<?> component) {
      this.component = component;
    }
    
    @Override
    public Object deserialize(Scanner scanner) {
      String next = scanner.next();
      if ("null".equals(next)) {
        return null;
      } else {
        int length = Integer.parseInt(next);
        Object array = (Object)Array.newInstance(component, length);
        for (int i = 0; i < length; i++) {
          Array.set(array, i, componentDeserializer.deserialize(scanner));
        }
        return array;
      }
    }
  }
  
  private static class CustomDeserializer extends ObjectDeserializer<Object> {
    private Method method;
    
    private CustomDeserializer(Method method) {
      this.method = method;
    }
    
    @Override
    public Object deserialize(Scanner scanner) {
      try {
        return method.invoke(null, scanner);
      } catch (IllegalArgumentException e) {
        logger.log(Level.SEVERE, "Unable to invoke custom deserialization method.", e);
      } catch (IllegalAccessException e) {
        logger.log(Level.SEVERE, "JVM does not have sufficient permissions for custom deserialization.", e);
      } catch (InvocationTargetException e) {
        logger.log(Level.SEVERE, "Unable to invoke custom deserialization method.", e);
      }
      return null;
    }
  }
}
