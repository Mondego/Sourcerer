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
package edu.uci.ics.sourcerer.repo.java.core;

import java.io.File;

import edu.uci.ics.sourcerer.repo.core.AbstractRepository;
import edu.uci.ics.sourcerer.repo.core.RepoFile;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaSourceRepository extends AbstractRepository<JavaProject> {
  public static final Property<String> JARS_DIR = new StringProperty("jars-dir", "jars", "The subdirectory containing the jar files.");
  public static final Property<String> JAR_INDEX_FILE = new StringProperty("jar-index", "index.txt", "The filename of the jar index.");
  
  protected RepoFile jarsRoot;
  protected RepoFile projectJarsRoot;
  protected RepoFile mavenJarsRoot;
  
  protected JavaSourceRepository(RepoFile repoRoot) {
    super(repoRoot);
    jarsRoot = repoRoot.getChild(JARS_DIR.getValue()); 
  }

  public static JavaSourceRepository make(Property<File> root) {
    return new JavaSourceRepository(RepoFile.makeRoot(root.getValue()));
  }
  
  @Override
  protected JavaProject createProject(RepoFile file) {
    return new JavaProject(file);
  }

  protected void populateJars
}
