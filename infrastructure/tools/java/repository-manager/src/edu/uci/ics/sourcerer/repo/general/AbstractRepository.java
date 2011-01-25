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
import java.util.Set;
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
  public static final Property<String> JAR_INDEX_FILE = new StringProperty("jar-index", "index.txt", "The filename of the jar index.");

  public static final Property<String> PROJECT_NAMES_FILE = new StringProperty("project-names-file", "project-names.txt", "File for project names.");
  public static final Property<String> PROJECT_SIZES_FILE = new StringProperty("project-sizes-file", "project-sizes.txt", "File for project sizes.");
  public static final Property<String> FILTERED_FILES_FILE = new StringProperty("filtered-files-file", "filtered-files.txt", "File for filtered file listing.");

  protected RepoFile repoRoot;
  protected RepoFile libsRoot;
  protected RepoFile jarsRoot;
  protected RepoFile projectJarsRoot;
  protected RepoFile mavenJarsRoot;
  
  
  protected RepoFile jarIndexFile;
  protected JarIndex jarIndex;
  
  protected AbstractRepository(File repoRoot) {
    this.repoRoot = RepoFile.make(repoRoot);
    libsRoot = this.repoRoot.getChild("libs");
    jarsRoot = this.repoRoot.getChild(JARS_DIR.getValue());
    jarIndexFile = jarsRoot.getChild(JAR_INDEX_FILE.getValue());
    projectJarsRoot = jarsRoot.getChild("project");
    mavenJarsRoot = jarsRoot.getChild("maven");
  }
  
  protected abstract void addProject(RepoFile path);
  
  protected abstract void addLibrary(RepoFile path);
  
  protected void populateRepository() {
    if (repoRoot.exists()) { 
      Pattern pattern = Pattern.compile("\\d*");
      for (File batch : repoRoot.toFile().listFiles()) {
        if (batch.isDirectory() && pattern.matcher(batch.getName()).matches()) {
          for (File checkout : batch.listFiles()) {
            if (pattern.matcher(checkout.getName()).matches()) {
              addProject(repoRoot.getChild(batch.getName() + "/" + checkout.getName()));
            }
          }
        }
      }
    }
  }
  
  protected void populateFilteredRepository(Set<String> filter) {
    if (repoRoot.exists()) {
      for (String projectPath : filter) {
        addProject(repoRoot.getChild(projectPath));
      }
    }
  }
  
  protected void populateLibraries() {
    if (libsRoot.exists()) {
      for (File lib : libsRoot.toFile().listFiles()) {
        if (lib.isDirectory()) {
          addLibrary(libsRoot.getChild(lib.getName()));
        }
      }
    }
  }
  
  private void loadJarIndex() {
    jarIndex = JarIndex.getJarIndex(this);
  }
  
  protected RepoFile getJarIndexFile() {
    return jarIndexFile;
  }
  
  protected RepoFile getJarsPath() {
    return jarsRoot;
  }
  
  protected RepoFile getProjectJarsPath() {
    return projectJarsRoot;
  }
  
  protected RepoFile getMavenJarsPath() {
    return mavenJarsRoot;
  }
  
  protected JarIndex getJarIndex() {
    if (jarIndex == null && jarIndexFile.exists()) {
      loadJarIndex();
    }
    return jarIndex;
  }
  
  protected void copyJarIndex(AbstractRepository copyFrom) {
    FileUtils.copyFile(copyFrom.jarIndexFile.toFile(), jarIndexFile.toFile());
  }
  
  public RepoFile rebasePath(RepoFile toRebase) {
    return repoRoot.rebaseFile(toRebase);
  }
  
  public String toString() {
    return repoRoot.toString();
  }
}
