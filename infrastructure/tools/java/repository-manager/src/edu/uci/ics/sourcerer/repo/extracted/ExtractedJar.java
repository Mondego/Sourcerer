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
package edu.uci.ics.sourcerer.repo.extracted;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedJar extends Extracted {
  private String name;
  private String group;
  private String version;
  private String hash;
  
  public ExtractedJar(File content) {
    super(content);
    loadProperties();
  }
  
  private void loadProperties() {
    String name = content.getName();
    name = name.substring(0, name.lastIndexOf('.'));
    File propFile = new File(content, name + ".properties");
    if (propFile.exists()) {
      Properties properties = new Properties();
      try {
        InputStream is = new FileInputStream(propFile);
        properties.load(is);
        name = properties.getProperty("name");
        group = properties.getProperty("group");
        version = properties.getProperty("version");
        hash = properties.getProperty("hash");
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to read properties", e);
      }
    } else {
      logger.log(Level.SEVERE, "Unable to find properties: " + propFile.getPath());
    }
  }
  
  public String getName() {
    return name;
  }
  
  public String getGroup() {
    return group;
  }
  
  public String getVersion() {
    return version;
  }
  
  public String getHash() {
    return hash;
  }
}
