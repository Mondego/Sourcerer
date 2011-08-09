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
package edu.uci.ics.sourcerer.repo.java.core.source;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.AbstractRepository;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.ProjectLocationImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaSourceRepository extends AbstractRepository<JavaSourceProject> {
  public static final Argument<String> JARS_DIR = new StringArgument("jars-dir", "jars", "The subdirectory containing the jar files.");
  public static final Argument<String> PROJECT_JARS_DIR = new StringArgument("project-jars-dir", "project", "The subdirectory containing the project jar files.");
  public static final Argument<String> MAVEN_JARS_DIR = new StringArgument("maven-jars-dir", "maven", "The subdirectory containing the maven jar files.");
  public static final Argument<String> JAR_INDEX_FILE = new StringArgument("jar-index", "index.txt", "The filename of the jar index.");
  
  protected final RepoFileImpl jarsRoot;
  protected final RepoFileImpl projectJarsRoot;
  protected final RepoFileImpl mavenJarsRoot;
  
  protected JavaSourceRepository(RepoFileImpl repoRoot) {
    super(repoRoot);
    jarsRoot = repoRoot.getChild(JARS_DIR.getValue());
    projectJarsRoot = jarsRoot.getChild(PROJECT_JARS_DIR.getValue());
    mavenJarsRoot = jarsRoot.getChild(MAVEN_JARS_DIR.getValue());
  }

  public static JavaSourceRepository make() {
    logger.info("Loading Java Source Repository at " + INPUT_REPO.getValue().getPath() + "...");
    return new JavaSourceRepository(RepoFileImpl.makeRoot(INPUT_REPO));
  }
  
  @Override
  protected JavaSourceProject createProject(ProjectLocationImpl loc) {
    return new JavaSourceProject(loc);
  }
}
