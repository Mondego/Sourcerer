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
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.FileProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractRepository {
  public static final Property<File> INPUT_REPO = new FileProperty("input-repo", "Repository Manager", "The root directory of the input repository.");
  public static final Property<File> OUTPUT_REPO = new FileProperty("output-repo", "Repository Manager", "The root directory of the output repository.");
  
  protected File repoRoot;
  
  protected File jarIndexFile;
  protected JarIndex jarIndex;
  
  protected AbstractRepository(File repoRoot) {
    this.repoRoot = repoRoot;
    this.jarIndexFile = new File(getJarsDir(), JarIndex.JAR_INDEX_FILE.getValue());
  }
  
  protected abstract void addFile(File checkout);
  
  protected void populateRepository() {
    if (repoRoot.exists()) { 
      Pattern pattern = Pattern.compile("\\d*");
      for (File batch : repoRoot.listFiles()) {
        if (batch.isDirectory() && pattern.matcher(batch.getName()).matches()) {
          for (File checkout : batch.listFiles()) {
            if (pattern.matcher(checkout.getName()).matches()) {
              addFile(checkout);
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
  
  public File getMavenJarsDir() {
    return new File(getJarsDir(), "maven");
  }
  
  public File getProjectJarsDir() {
    return new File(getJarsDir(), "project");
  }
  
  public File getJarsDir() {
    return new File(repoRoot, "jars");
  }
  
  public File getLibsDir() {
    return new File(repoRoot, "libs");
  }
  
  public String convertToRelativePath(File file) {
    return convertToRelativePath(file.getPath(), repoRoot.getPath());
  }
  
  public String convertToRelativePath(String path) {
    return convertToRelativePath(path, repoRoot.getPath());
  }
  
  public String convertToRelativePath(File file, File base) {
    return convertToRelativePath(file.getPath(), base.getPath());
  }
  
  public String convertToRelativePath(String path, String basePath) {
    path = path.replace('\\', '/');
    basePath = basePath.replace('\\', '/');
    if (basePath == null) {
      return path.replace(' ', '*');
    } else {
      if (path.startsWith(basePath)) {
        return path.substring(basePath.length()).replace(' ', '*');
      } else {
        logger.severe("Unable to convert " + path + " to relative path");
        return path.replace(' ', '*');
      }
    }
  }
  
  public String toString() {
    return repoRoot.getPath();
  }
}
