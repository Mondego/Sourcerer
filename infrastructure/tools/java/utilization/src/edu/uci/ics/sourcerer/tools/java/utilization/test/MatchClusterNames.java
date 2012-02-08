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
package edu.uci.ics.sourcerer.tools.java.utilization.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import edu.uci.ics.sourcerer.tools.java.utilization.identifier.Identifier;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterMatcher;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;
import edu.uci.ics.sourcerer.util.io.arguments.IntegerArgument;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MatchClusterNames {
  private MatchClusterNames() {}
  
  public static Argument<File> TEST_REPO = new FileArgument("test-repo", "Repository containing test jar files.");
  public static Argument<File> TEST_REPO_CACHE = new RelativeFileArgument("test-repo-cache", "test-repo-cache", Arguments.CACHE, "Test repo cache directory.").permit();
  public static Argument<Integer> MATCH_COUNT = new IntegerArgument("match-count", 100, "Number of matches to check").permit();
  
  public static void matchClusterNames() {
    TaskProgressLogger task = TaskProgressLogger.create();
    task.start("Matching jars against cluster names");
    
    ClusterCollection clusters = null;
    
    {
      JarCollection jars = JarCollection.make(task);
      
      if (ClusterCollection.CLUSTER_COLLECTION.asInput().getValue().exists()) {
        clusters = ClusterCollection.load(task, jars);
      }
      if (clusters == null) {
        clusters = Identifier.identifyClusters(task, jars);
        clusters.save();
        clusters.printStatistics(task);
      }
      
      Identifier.identifyClusterExemplars(task, clusters);
    }
    
    JarCollection jars = JarCollection.make(task, TEST_REPO, TEST_REPO_CACHE);
    ArrayList<VersionedFqnNode> fqns = new ArrayList<>();
    for (VersionedFqnNode fqn : jars.getRoot().getLeavesIterable()) {
      fqns.add(fqn);
    }
    ClusterMatcher matcher = clusters.getClusterMatcher();
    Random random = new Random();
    for (int i = 0, max = MATCH_COUNT.getValue(); i < max; i++) {
      int next = random.nextInt(fqns.size());
      VersionedFqnNode fqn = fqns.get(next);
      String fqnName = fqn.getFqn();
      Cluster cluster = matcher.getMatch(fqnName);
      if (cluster == null) {
        task.report(fqnName + " from " + fqn.getVersions().getJars() + " MISSING!");
      } else {
        task.report(fqnName + " from " + fqn.getVersions().getJars() + " vs " + cluster.getExemplars());
      }
    }
    task.finish();
  }
}
