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
package edu.uci.ics.sourcerer.tools.java.repo.jars;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarIdentifier {
  public static final Argument<String> CONTAINING_FQN = new StringArgument("containing-fqn", "FQN to be identified.");
  public static final DualFileArgument IDENTIFIED_JARS_FILE = new DualFileArgument("identified-jar-files", "identified-jars.txt", "Identified jar files.");
  
  private JarIdentifier() {}
  
  public static void identifyJarsContainingFqn() {
    String fqnToFind = CONTAINING_FQN.getValue();
    
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Identifying jar files containing " + fqnToFind);

    JavaRepository repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    
    try (BufferedWriter bw = IOUtils.makeBufferedWriter(IDENTIFIED_JARS_FILE)) {
      int count = 0;
      task.start("Searching maven jar files", "jar files searched", 1000);
      for (JarFile jar : repo.getMavenJarFiles()) {
        task.progress();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jar.getFile().toFile()))) {
          for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
            if (entry.getName().endsWith(".class")) {
              String fqn = entry.getName();
              fqn = fqn.substring(0, fqn.lastIndexOf('.')).replace('/', '.');
              if (fqnToFind.equals(fqn)) {
                bw.write(jar.getProperties().HASH.getValue());
                bw.newLine();
                count++;
                break;
              }
            }
          }
        } catch (IOException | IllegalArgumentException e) {
          logger.log(Level.SEVERE, "Error reading jar file: " + jar, e);
        }
      }
      task.report("Identified " + count + " maven jar files containing " + fqnToFind);
      task.finish();
      
      count = 0;
      task.start("Searching project jar files", "jar files searched", 1000);
      for (JarFile jar : repo.getProjectJarFiles()) {
        task.progress();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jar.getFile().toFile()))) {
          for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
            if (entry.getName().endsWith(".class")) {
              String fqn = entry.getName();
              fqn = fqn.substring(0, fqn.lastIndexOf('.')).replace('/', '.');
              if (fqnToFind.equals(fqn)) {
                bw.write(jar.getProperties().HASH.getValue());
                bw.newLine();
                count++;
                break;
              }
            }
          }
        } catch (IOException | IllegalArgumentException e) {
          logger.log(Level.SEVERE, "Error reading jar file: " + jar, e);
        }
      }
      task.report("Identified " + count + " project jar files containing " + fqnToFind);
      task.finish();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing file.", e);
    }
    task.finish();
  }
  
  public static Collection<String> loadIdentifiedJars() {
    Collection<String> identifiedJars = new ArrayList<>();
    try (BufferedReader br = IOUtils.makeBufferedReader(IDENTIFIED_JARS_FILE)) {
      for (String hash = br.readLine(); hash != null; hash = br.readLine()) {
        identifiedJars.add(hash);
      }
      return identifiedJars;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to load identified jars", e);
      return Collections.emptyList();
    }
  }
}
