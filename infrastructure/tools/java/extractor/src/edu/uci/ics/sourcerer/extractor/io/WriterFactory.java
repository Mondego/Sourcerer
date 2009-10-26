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
package edu.uci.ics.sourcerer.extractor.io;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.io.Property;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class WriterFactory {
  private WriterFactory() {}
  
  @SuppressWarnings("unchecked")
  public static <T> T createWriter(File output, Repository input, Property<Class<?>> property) {
    try {
      Class<?> klass = property.getValue();
      Constructor<?> constructor = klass.getConstructor(File.class, Repository.class);
      return (T)constructor.newInstance(output, input);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to create writer: " + property.getName(), e);
      return null;
    }
  }
}
