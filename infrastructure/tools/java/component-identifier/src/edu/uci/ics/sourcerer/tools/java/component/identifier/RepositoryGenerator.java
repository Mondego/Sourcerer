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
package edu.uci.ics.sourcerer.tools.java.component.identifier;

import java.io.File;

import edu.uci.ics.sourcerer.tools.java.component.identifier.internal.ClusterIdentifier;
import edu.uci.ics.sourcerer.tools.java.component.identifier.internal.ClusterMerger;
import edu.uci.ics.sourcerer.tools.java.component.identifier.internal.ComponentRepositoryBuilder;
import edu.uci.ics.sourcerer.tools.java.component.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.component.model.repo.ComponentRepository;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepositoryGenerator {
  public static final Argument<File> JAR_FILTER_FILE = new FileArgument("jar-filter-file", null, "Jar filter file");
  
  private RepositoryGenerator() {}
      
  public static final ComponentRepository generateArtifactRepository() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Generating artifact repository");
    try {
      JarCollection jars = null;

      if (JAR_FILTER_FILE.getValue() != null) {
        jars = JarCollection.create(FileUtils.readFileToCollection(JAR_FILTER_FILE.getValue()));
      } else {
        jars = JarCollection.create();
      }
      
      ClusterCollection clusters = ClusterIdentifier.identifyFullyMatchingClusters(jars);
      ClusterMerger.mergeByVersions(clusters);
      
      return ComponentRepositoryBuilder.buildRepository(jars, clusters);
    } finally {
      task.finish();
    }
  }
}
