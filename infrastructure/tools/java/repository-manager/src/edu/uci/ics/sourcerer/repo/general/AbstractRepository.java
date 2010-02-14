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
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.FileProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractRepository {
  public static final Property<File> INPUT_REPO = new FileProperty("input-repo", "Repository Manager", "The root directory of the input repository.");
  public static final Property<File> OUTPUT_REPO = new FileProperty("output-repo", "Repository Manager", "The root directory of the output repository.");
  
  public static final Property<File> JAR_FILTER = new FileProperty("jar-filter", "Extractor", "Only extract these jars.");
  public static final Property<File> PROJECT_FILTER = new FileProperty("project-filter", "Extractor", "Only extract these projects.");

  protected static final String JARS = "jars";
  protected static final String LIBS = "libs";
  protected static final String PROJECT_JARS = JARS + "/project";
  protected static final String MAVEN_JARS = JARS + "/maven";
  
  protected File repoRoot;
  
  protected File jarIndexFile;
  protected JarIndex jarIndex;
  
  protected AbstractRepository(File repoRoot) {
    this.repoRoot = repoRoot;
    this.jarIndexFile = getJarsPath().getChildFile(JarIndex.JAR_INDEX_FILE.getValue());
  }
  
  protected abstract void addProject(RepoPath path);
  
  protected void populateRepository() {
    if (repoRoot.exists()) { 
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
  }
  
  private void loadJarIndex() {
    jarIndex = JarIndex.getJarIndex(this);
  }
  
  public File getJarIndexFile() {
    return jarIndexFile;
  }
  
  public JarIndex getJarIndex() {
    if (jarIndex == null && jarIndexFile.exists()) {
      loadJarIndex();
    }
    return jarIndex;
  }
  
  public File getBaseDir() {
    return repoRoot;
  }
  
  protected RepoPath getPath(String relativePath) {
    return RepoPath.getNewPath(new File(repoRoot, relativePath), relativePath);
  }
  
  public RepoPath getMavenJarsPath() {
    return getPath(MAVEN_JARS);
  }
  
  protected RepoPath getProjectJarsPath() {
    return getPath(PROJECT_JARS);
  }
  
  protected RepoPath getJarsPath() {
    return getPath(JARS);
  }
  
  public RepoPath getLibsPath() {
    return getPath(LIBS);
  }
  
  public RepoPath convertPath(RepoPath other) {
    return other.getNewPath(repoRoot.getPath());
  }
  
  public String toString() {
    return repoRoot.getPath();
  }
}
