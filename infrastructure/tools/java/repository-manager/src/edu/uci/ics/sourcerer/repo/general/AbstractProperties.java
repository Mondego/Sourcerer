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
package edu.uci.ics.sourcerer.repo.general;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractProperties {
  protected Properties properties;
  
  protected void loadProperties(File file) {
    properties = new Properties();
    if (file.exists()) {
      InputStream is = null;
      try {
        is = new FileInputStream(file);
        properties.load(is);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to load properties file: " + file.getPath(), e);
      } finally {
        FileUtils.close(is);
      }
    }
  }

  protected void write(File file) {
    write(file, properties);
  }
  
  protected static void write(File file, Properties properties) {
    try {
      OutputStream os = new FileOutputStream(file);
      properties.store(os, null);
      os.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write properties file: " + file.getPath(), e);
    }
  }
}