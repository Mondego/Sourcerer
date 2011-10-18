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
package edu.uci.ics.sourcerer.tools.java.utilization.fqn;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnUsageTreeBuilder {
  public static FqnUsageTree<JarFile> buildWithMaven(TaskProgressLogger task) {
    task.start("Loading repository");
    JavaRepository repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    task.finish();
    
    task.start("Extracting FQNs from jar files", "jar files extracted", 500);
    FqnUsageTree<JarFile> tree = new FqnUsageTree<>();
    for (JarFile jar : repo.getMavenJarFiles()) {
      task.progress();
      // Extract the class file names and add them to the tree
      try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jar.getFile().toFile()))) {
        for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
          if (entry.getName().endsWith(".class")) {
            String fqn = entry.getName();
            fqn = fqn.substring(0, fqn.lastIndexOf('.'));
            tree.addSlashFqn(fqn, jar);
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error reading jar file: " + jar, e);
      }
    }
    task.finish();
    
    return tree;
  }
}
