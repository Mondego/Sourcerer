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
  
  protected IndexedJar(String basePath, String jarName) {
    this(basePath, null, jarName, null);
  }
  
  protected IndexedJar(String basePath, String relativePath, String jarName) {
    this(basePath, relativePath, jarName, null);
  }
  
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
    if (relativePath == null) {
      return new File(basePath + "/" + jarName);
    } else {
      return new File(basePath + "/" + relativePath + "/" + jarName);
    }
  }
  
  public File getJarFile() {
    return getJarFile(basePath);
  }
  
  private File getSourceFile(String basePath) {
    return new File(basePath + "/" + relativePath + "/" + sourceName);
  }
  
  public File getSourceFile() {
    if (sourceName == null) {
      return null;
    } else {
      return getSourceFile(basePath);
    }
  }
  
  public boolean hasSource() {
    return sourceName != null;
  }
  
  public boolean isMavenJar() {
    return relativePath != null;
  }
  
  private File getPropertiesFile(String basePath) {
    if (relativePath == null) {
      return new File(basePath + "/" + jarName + ".properties");
    } else {
      return new File(basePath + "/" + relativePath + "/" + jarName + ".properties");
    }
  }
  
  public File getPropertiesFile() {
    return getPropertiesFile(basePath);
  }
  
  private File getInfoFile(String basePath) {
    if (relativePath == null) {
      return null;
    } else {
      return new File(basePath + "/" + jarName + ".info");
    }
  }
  
  public File getInfoFile() {
    return getInfoFile(basePath);
  }
  
  public String getOutputPath(File baseDir) {
    if (relativePath == null) {
      return baseDir.getPath() + "/" + jarName;
    } else {
      return baseDir.getPath() + "/" + relativePath;
    }
  }
  
  public void copyPropertiesFile(File baseDir) {
    File outputDir = new File(getOutputPath(baseDir));
    File output = new File(outputDir, jarName + ".properties");
    FileUtils.copyFile(getPropertiesFile(), output);
  }
  
  public String toString() {
    if (relativePath == null) {
      return jarName;
    } else {
      return relativePath + "/" + jarName;
    }
  }
}
