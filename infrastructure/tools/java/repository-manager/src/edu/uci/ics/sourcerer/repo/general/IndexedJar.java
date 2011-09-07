///* 
// * Sourcerer: an infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
//
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.uci.ics.sourcerer.repo.general;
//
//import java.io.File;
//
//import edu.uci.ics.sourcerer.repo.extracted.ExtractedJar;
//import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
//import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
//import edu.uci.ics.sourcerer.util.io.internal.FileUtils;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class IndexedJar {
//  private boolean maven;
//  private String hash;
//  private String groupName;
//  private String version;
//  private String artifactName;
//  private RepoFile path;
//  private String jarName;
//  private String sourceName;
//  
//  protected IndexedJar(String hash, RepoFile path, String jarName) {
//    this(false, hash, null, null, null, path, jarName, null);
//  }
//  
//  protected IndexedJar(String hash, String groupName, String version, String artifactName, RepoFile path, String jarName) {
//    this(true, hash, groupName, version, artifactName, path, jarName, null);
//  }
//  
//  protected IndexedJar(String hash, String groupName, String version, String artifactName, RepoFile path, String jarName, String sourceName) {
//    this(true, hash, groupName, version, artifactName, path, jarName, sourceName);
//  }
//  
//  private IndexedJar(boolean maven, String hash, String groupName, String version, String artifactName, RepoFile path, String jarName, String sourceName) {
//    this.maven = maven;
//    this.hash = hash;
//    this.groupName = groupName;
//    this.version = version;
//    this.artifactName = artifactName;
//    this.path = path;
//    this.jarName = jarName;
//    this.sourceName = sourceName;
//  }
//  
//  public void migrateIndexedJar(RepoFile newBase) {
//    RepoFile newPath = newBase.getChild(path.getRelativePath());
//    
//    FileUtils.copyFile(getJarFile(), getJarFile(newPath));
//    if (sourceName != null) {
//      FileUtils.copyFile(getSourceFile(), getSourceFile(newPath));
//    }
//    FileUtils.copyFile(getPropertiesFile(), getPropertiesFile(newPath));
//    FileUtils.copyFile(getInfoFile(), getInfoFile(newPath));
//  }
//    
//  private File getInfoFile(RepoFile path) {
//    return path.getChildFile(jarName + ".info");
//  }
//  
//  public File getInfoFile() {
//    return getInfoFile(path);
//  }
//  
//  private File getJarFile(RepoFile path) {
//    return path.getChildFile(jarName);
//  }
//  
//  public File getJarFile() {
//    return getJarFile(path);
//  }
//  
//  private File getSourceFile(RepoFile path) {
//    return path.getChildFile(sourceName);
//  }
//  
//  public File getSourceFile() {
//    if (sourceName == null) {
//      return null;
//    } else {
//      return getSourceFile(path);
//    }
//  }
//  
//  public boolean hasSource() {
//    return sourceName != null;
//  }
//  
//  public boolean isMavenJar() {
//    return maven;
//  }
//  
//  public String getName() {
//    return jarName;
//  }
//  
//  private File getPropertiesFile(RepoFile path) {
//    return path.getChildFile(jarName + ".properties");
//  }
//  
//  private File getPropertiesFile() {
//    return getPropertiesFile(path);
//  }
//  
//  public ExtractedJarProperties getProperties() {
//    return ExtractedJarProperties.loadProperties(getPropertiesFile());
//  }
//  
//  public String getHash() {
//    return hash;
//  }
//  
//  public String getGroupName() {
//    return groupName;
//  }
//  
//  public String getVersion() {
//    return version;
//  }
//  
//  public String getArtifactName() {
//    return artifactName;
//  }
//  
//  public ExtractedJar getExtractedJar(ExtractedRepository repo) {
//    RepoFile rebasedPath = repo.rebasePath(path);
//    if (maven) {
//      return new ExtractedJar(rebasedPath, getPropertiesFile());
//    } else {
//      return new ExtractedJar(rebasedPath.getChild(jarName), getPropertiesFile());
//    }
//  }
//  
//  public ExtractedJar getExtractedJar() {
//    if (maven) {
//      return new ExtractedJar(path);
//    } else {
//      return new ExtractedJar(path.getChild(jarName));
//    }
//  }
//
//  public String toString() {
//    if (maven) {
//      return path.toString();
//    } else {
//      return path.toString() + "/" + jarName;
//    }
//  }
//}
