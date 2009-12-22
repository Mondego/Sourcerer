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

import java.io.File;

import edu.uci.ics.sourcerer.repo.extracted.ExtractedJar;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class IndexedJar {
  private boolean maven;
  private String hash;
  private String groupName;
  private String version;
  private String artifactName;
  private String basePath;
  private String relativePath;
  private String jarName;
  private String sourceName;
  
  protected IndexedJar(String hash, String basePath, String relativePath, String jarName) {
    this(false, hash, null, null, null, basePath, relativePath, jarName, null);
  }
  
  protected IndexedJar(String hash, String groupName, String version, String artifactName, String basePath, String relativePath, String jarName) {
    this(true, hash, groupName, version, artifactName, basePath, relativePath, jarName, null);
  }
  
  protected IndexedJar(String hash, String groupName, String version, String artifactName, String basePath, String relativePath, String jarName, String sourceName) {
    this(true, hash, groupName, version, artifactName, basePath, relativePath, jarName, sourceName);
  }
  
  private IndexedJar(boolean maven, String hash, String groupName, String version, String artifactName, String basePath, String relativePath, String jarName, String sourceName) {
    this.maven = maven;
    this.hash = hash;
    this.groupName = groupName;
    this.version = version;
    this.artifactName = artifactName;
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
    FileUtils.copyFile(getInfoFile(), getInfoFile(basePath));
  }

  private File getJarFile(String basePath) {
    return new File(basePath + "/" + relativePath + "/" + jarName);
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
    return maven;
  }
  
  public String getName() {
    return jarName;
  }
  
  private File getPropertiesFile(String basePath) {
    return new File(basePath + "/" + relativePath + "/" + jarName + ".properties");
  }
  
  private File getPropertiesFile() {
    return getPropertiesFile(basePath);
  }
  
  public JarProperties getProperties() {
    return JarProperties.load(getPropertiesFile());
  }
  
  private File getInfoFile(String basePath) {
    return new File(basePath + "/" + jarName + ".info");
  }
  
  public File getInfoFile() {
    return getInfoFile(basePath);
  }
  
  public String getHash() {
    return hash;
  }
  
  public String getGroupName() {
    return groupName;
  }
  
  public String getVersion() {
    return version;
  }
  
  public String getArtifactName() {
    return artifactName;
  }
  
  public ExtractedJar getExtractedJar(ExtractedRepository repo) {
    if (maven) {
      return new ExtractedJar(getOutputPath(repo.getMavenJarsDir()), getPropertiesFile());
    } else {
      return new ExtractedJar(getOutputPath(repo.getProjectJarsDir()), getPropertiesFile());
    }
  }
  
  public ExtractedJar getExtractedJar() {
    return new ExtractedJar(new File(getOutputPath(basePath)));
  }
  
  private File getOutputPath(File baseDir) {
    return new File(getOutputPath(baseDir.getPath()));
  }
  
  private String getOutputPath(String baseDir) {
    if (maven) {
      return baseDir + "/" + relativePath;
    } else {
      return baseDir + "/" + relativePath + "/" + jarName;
    }
  }
  
  public String toString() {
    if (relativePath == null) {
      return jarName;
    } else {
      return relativePath + "/" + jarName;
    }
  }
}
