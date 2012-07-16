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
package edu.uci.ics.sourcerer.tools.java.model.extracted.io;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.arguments.Argument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class WriterFactory {
  private WriterFactory() {}
  
  @SuppressWarnings("unchecked")
  public static <T> T createWriter(File output, Argument<Class<?>> arg) {
    try {
      Class<?> klass = arg.getValue();
      Constructor<?> constructor = klass.getConstructor(File.class);
      return (T)constructor.newInstance(output);
    } catch (Exception e) {
      Class<?> klass = arg.getDefaultValue();
      if (klass != null) {
        try {
          return (T)klass.newInstance();
        } catch (Exception e2) {
          logger.log(Level.SEVERE, "Unable to create dummy writer: " + arg.getName(), e);
          return null;
        }
      } else {
        return null;
      }
    }
  }
}
