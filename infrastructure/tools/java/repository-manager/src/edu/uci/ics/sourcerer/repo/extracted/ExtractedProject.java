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

import edu.uci.ics.sourcerer.util.io.PropertyOld;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedProject extends Extracted {
  private String relativetPath;
  private String name;
  
  public ExtractedProject(File content, String relativePath) {
    super(content);
    this.relativetPath = relativePath;
    loadProperties();
  }
  
  private void loadProperties() {
    File properties = new File(content, "project.properties");
    if (properties.exists()) {
      try {
        Properties props = new Properties();
        InputStream is = new FileInputStream(properties);
        props.load(is);
        is.close();
        name = props.getProperty("name");
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to load properties", e);
      }
    }
  }
  
  public String getRelativePath() {
    return relativetPath;
  }
  
  public String getName() {
    return name;
  }
  
  public InputStream getFileInputStream() throws IOException {
    return getInputStream(PropertyOld.FILE_FILE);
  }
  
  public InputStream getProblemInputStream() throws IOException {
    return getInputStream(PropertyOld.PROBLEM_FILE);
  }
  
  public InputStream getJarInputStream() throws IOException {
    return getInputStream(PropertyOld.JAR_FILE_FILE);
  }
  
  public InputStream getImportInputStream() throws IOException {
    return getInputStream(PropertyOld.IMPORT_FILE);
  }
}
