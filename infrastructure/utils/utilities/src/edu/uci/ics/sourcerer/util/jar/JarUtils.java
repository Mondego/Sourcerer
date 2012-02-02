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
package edu.uci.ics.sourcerer.util.jar;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarUtils {
  private JarUtils() {}
  
  public static Collection<String> extractFQNs(File file) {
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
      Collection<String> fqns = new LinkedList<>();
      for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
        if (entry.getName().endsWith(".class")) {
          String fqn = entry.getName();
          fqns.add(fqn.substring(0, fqn.lastIndexOf('.')).replace('/', '.'));
        }
      }
      return fqns;
    } catch (IOException | IllegalArgumentException e) {
      logger.log(Level.SEVERE, "Error reading jar file: " + file.getPath(), e);
      return Collections.emptyList();
    }
  }
}
