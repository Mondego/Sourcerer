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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.FileProperty;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractRepository {
  public static final Property<File> INPUT_REPO = new FileProperty("input-repo", "The root directory of the input repository.");
  public static final Property<File> OUTPUT_REPO = new FileProperty("output-repo", "The root directory of the output repository.");
  
  public static final Property<File> JAR_FILTER = new FileProperty("jar-filter", "Only extract these jars.").makeOptional();
  public static final Property<File> PROJECT_FILTER = new FileProperty("project-filter", "Only extract these projects.").makeOptional();
  
  public static final Property<String> JARS_DIR = new StringProperty("jars-dir", "jars", "The subdirectory containing the jar files.");
  public static final Property<String> LIBS_DIR = new StringProperty("libs-dir", "libs", "The subdirectory containing the library files.");

  public static final Property<String> JAR_INDEX_FILE = new StringProperty("jar-index", "index.txt", "The filename of the jar index.");
  
  public static final Property<String> PROJECT_NAMES_FILE = new StringProperty("project-names-files", "project-names.txt", "File for project names.");

  protected final String JARS;
  protected final String LIBS;
  protected final String PROJECT_JARS;
  protected final String MAVEN_JARS;
  
  protected File repoRoot;
  
  protected File jarIndexFile;
  protected JarIndex jarIndex;
  
  protected AbstractRepository(File repoRoot) {
    JARS = JARS_DIR.getValue();
    LIBS = LIBS_DIR.getValue();
    PROJECT_JARS = JARS + "/project";
    MAVEN_JARS = JARS + "/maven";
    
    this.repoRoot = repoRoot;
    this.jarIndexFile = getJarsPath().getChildFile(JAR_INDEX_FILE.getValue());
  }
  
  private RepoPath getPath(String relativePath) {
    return RepoPath.getNewPath(new File(repoRoot, relativePath), relativePath);
  }
  
  protected RepoPath getMavenJarsPath() {
    return getPath(MAVEN_JARS);
  }
  
  protected RepoPath getProjectJarsPath() {
    return getPath(PROJECT_JARS);
  }
  
  protected RepoPath getJarsPath() {
    return getPath(JARS);
  }
  
  protected RepoPath getLibsPath() {
    return getPath(LIBS);
  }
  
  public RepoPath convertPath(RepoPath other) {
    return other.rebasePath(repoRoot.getPath());
  }
  
  protected abstract void addProject(RepoPath path);
  
  protected void populateProjects() {
    if (repoRoot.exists()) { 
      if (PROJECT_FILTER.hasValue()) {
        Set<String> filter = FileUtils.getFileAsSet(PROJECT_FILTER.getValue());
        for (String projectPath : filter) {
          addProject(RepoPath.getNewPath(repoRoot, projectPath));
        }
      } else {
        Pattern pattern = Pattern.compile("\\d*");
        for (File batch : repoRoot.listFiles()) {
          if (batch.isDirectory() && pattern.matcher(batch.getName()).matches()) {
            for (File checkout : batch.listFiles()) {
              if (pattern.matcher(checkout.getName()).matches()) {
                addProject(RepoPath.getNewPath(checkout, batch.getName() + "/" + checkout.getName()));
              }
            }
          }
        }
      }
    } else {
      logger.log(Level.SEVERE, "No repository at: " + repoRoot.getPath());
    }
  }
  
  protected abstract void addJar(RepoPath path);
  
  private void populateJars() {
    if (jarIndexFile.exists()) {
      
    } else {
      logger.log(Level.SEVERE, "No jar index at: " + jarIndexFile.getPath());
    }
  }
  
  

  
  public String toString() {
    return repoRoot.getPath();
  }
  
  protected class JarIndex {
    
  }
}
