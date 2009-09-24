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

import java.lang.reflect.Constructor;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class WriterFactory {
  private WriterFactory() {}
  
  @SuppressWarnings("unchecked")
  public static <T> T createWriter(Repository input, Property property, Class<?> backup) {
    PropertyManager properties = PropertyManager.getProperties();
    String name = properties.getValue(property);
    if (name != null) {
      try {
        Class<?> klass = Class.forName(name);
        Constructor<?> constructor = klass.getConstructor(Repository.class);
        return (T)constructor.newInstance(input);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Unable to create writer: " + name, e);
      }
    }
    try {
      return (T)backup.newInstance();
    } catch (Exception e2) {
      logger.log(Level.SEVERE, "Unable to create backup writer!");
      return null;
    }
  }
}
