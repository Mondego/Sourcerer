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
package edu.uci.ics.sourcerer.tools.java.utilization;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.utilization.identifier.Identifier;
import edu.uci.ics.sourcerer.tools.java.utilization.identifier.LibraryCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.JarCollection;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.util.io.arguments.Command;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  public static final Command IDENTIFY_LIBRARIES = new Command("identify-libraries", "Cluster and identify the libraries.") {
    
    @Override
    protected void action() {
      TaskProgressLogger task = new TaskProgressLogger();
      LibraryCollection libraries = Identifier.identifyLibraries(task, JarCollection.make(task));
      libraries.printStatistics(task);
    }
  }.setProperties(JavaRepositoryFactory.INPUT_REPO);
//  public static final Command COMPUTE_MAVEN_FQN_USAGE_STATS = new Command("compute-maven-fqn-usage-stats", "Computes some statistics on FQN usage in Maven.") {
//    @Override
//    protected void action() {
//      TaskProgressLogger task = new TaskProgressLogger();
//      task.start("Computing maven fqn usage statistics");
//      FqnUsageStatistics.printFqnUsageStatistics(task, FqnUsageTreeBuilder.buildWithMaven(task));
//      task.finish();
//    }
//  }.setProperties(JavaRepositoryFactory.INPUT_REPO);
//  
//  public static final Command COMPUTE_MAVEN_JAR_ENTROPY = new Command("compute-maven-jar-entropy", "Computes entropy of Maven jars.") {
//    @Override
//    protected void action() {
//      TaskProgressLogger task = new TaskProgressLogger();
//      task.start("Computing maven jar entropy");
//      JarEntropyClassifier.classify(task, JarEntropyCalculator.computeMavenJarEntropy(task));
//      task.finish();
//    }
//  }.setProperties(JavaRepositoryFactory.INPUT_REPO);
  
  public static void main(String[] args) {
    Command.execute(args, Main.class);
  }
}
