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
package edu.uci.ics.sourcerer.repo;

import java.io.File;

import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class IndexedJar {
  private String basePath;
  private String relativePath;
  private String jarName;
  private String sourceName;
  
  protected IndexedJar(String basePath, String relativePath, String jarName, String sourceName) {
    this.basePath = basePath;
    this.relativePath = relativePath;
    this.jarName = jarName;
    this.sourceName = sourceName;
  }
  
  public void migrateIndexedJar(File newBasePath) {
    String basePath = newBasePath.getPath();
    
    FileUtils.copyFile(getJarFile(), getJarFile(basePath));
    if (sourceName != null) {
      FileUtils.copyFile(getSourceFile(), getSourceFile(basePath));
    }
    FileUtils.copyFile(getPropertiesFile(), getPropertiesFile(basePath));
  }

  private File getJarFile(String basePath) {
    return new File(basePath + File.separatorChar + relativePath + File.separatorChar + jarName);
  }
  
  public File getJarFile() {
    return getJarFile(basePath);
  }
  
  private File getSourceFile(String basePath) {
    return new File(basePath + File.separatorChar + relativePath + File.separatorChar + sourceName);
  }
  
  public File getSourceFile() {
    if (sourceName == null) {
      return null;
    } else {
      return getSourceFile(basePath);
    }
  }
  
  private File getPropertiesFile(String basePath) {
    return new File(basePath + File.separatorChar + relativePath + File.separatorChar + jarName + ".properties");
  }
  
  public File getPropertiesFile() {
    return getPropertiesFile(basePath);
  }
    
  public String getRelativePath() {
    return relativePath;
  }
  
  public String getOutputPath(File baseDir) {
    return baseDir.getPath() + "/" + relativePath;
  }
  
  public void copyPropertiesFile(File baseDir) {
    File outputDir = new File(getOutputPath(baseDir));
    String name = outputDir.getName();
    name = name.substring(0, name.lastIndexOf('.'));
    File output = new File(outputDir, name + ".properties");
    FileUtils.copyFile(getPropertiesFile(), output);
  }
}
