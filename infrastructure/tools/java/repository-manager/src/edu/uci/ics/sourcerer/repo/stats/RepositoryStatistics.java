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
package edu.uci.ics.sourcerer.repo.stats;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedProject;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.general.IndexedJar;
import edu.uci.ics.sourcerer.repo.general.JarIndex;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.arguments.IOFileArgumentFactory;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepositoryStatistics {
  public static final IOFileArgumentFactory JAR_STATS_FILE = new IOFileArgumentFactory("jar-stats-file", "jar-stats.txt", "File containing repository jar statistics.");
  public static void printJarStatistics(Repository repo) {
    JarIndex index = repo.getJarIndex();
    int project = 0;
    int projectSource = 0;
    int maven = 0;
    int mavenSource = 0;
    for (IndexedJar jar : index.getJars()) {
      if (jar.isMavenJar()) {
        maven++;
        if (jar.hasSource()) {
          mavenSource++;
        }
      } else {
        project++;;
        if (jar.hasSource()) {
          projectSource++;
        }
      }
    }
    
    logger.info("Writing jar statistics to: " + JAR_STATS_FILE.asOutput().getValue().getAbsolutePath());
      
    TablePrettyPrinter printer = null;
    try {
      printer = TablePrettyPrinter.getTablePrettyPrinter(JAR_STATS_FILE);
      printer.beginTable(2);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Jars from Projects");
      printer.addCell(project);
      printer.beginRow();
      printer.addCell("Jars from Projects with Source");
      printer.addCell(projectSource);
      printer.beginRow();
      printer.addCell("Maven Jars");
      printer.addCell(maven);
      printer.beginRow();
      printer.addCell("Maven Jars with Source");
      printer.addCell(mavenSource);
    } finally {
      printer.close();
    }
  }
  
  public static final IOFileArgumentFactory PROJECT_SIZES_FILE = new IOFileArgumentFactory("project-sizes-file", "project-sizes.txt", "File containing project size information");
  public static void printProjectSizes(Repository repo) {
    BufferedWriter bw = null;
    try {
      logger.info("Writing project sizes to: " + PROJECT_SIZES_FILE.asOutput().getValue().getAbsolutePath());
      bw = FileUtils.getBufferedWriter(PROJECT_SIZES_FILE);
      bw.write("project-path total-file-count filtered-file-count jar-count");
      bw.newLine();
      for (RepoProject project : repo.getProjects()) {
        IFileSet files = project.getFileSet();
        bw.write(project.getProjectRoot().getRelativePath() + " " + files.getJavaFileCount() + " " + files.getFilteredJavaFileCount() + " " + files.getJarFileCount());
        bw.newLine();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing to file", e);
    } finally { 
      FileUtils.close(bw);
    }
  }
  
  public static final IOFileArgumentFactory PROJECT_NAMES_FILE = new IOFileArgumentFactory("project-names-file", "project-names.txt", "File containing the names of the projects in the repository.");
  public static void printProjectNames(Repository repo) {
    BufferedWriter bw = null;
    try {
      logger.info("Writing project names to: " + PROJECT_NAMES_FILE.asOutput().getValue().getAbsolutePath());
      bw = FileUtils.getBufferedWriter(PROJECT_NAMES_FILE);
      bw.write("project-path project-name");
      bw.newLine();
      for (RepoProject project : repo.getProjects()) {
        bw.write(project.getProjectRoot().getRelativePath() + " " + project.loadProperties().getName());
        bw.newLine();
      }
      logger.info("Done!");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing to file", e);
    } finally { 
      FileUtils.close(bw);
    }
  }
  
  public static void printProjectNames(ExtractedRepository repo) {
    BufferedWriter bw = null;
    try {
      logger.info("Writing project names to: " + PROJECT_NAMES_FILE.asOutput().getValue().getAbsolutePath());
      bw = FileUtils.getBufferedWriter(PROJECT_NAMES_FILE);
      bw.write("project-path project-name");
      bw.newLine();
      for (ExtractedProject project : repo.getProjects()) {
        bw.write(project.getRelativePath() + " " + project.getName());
        bw.newLine();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing to file", e);
    } finally { 
      FileUtils.close(bw);
    }
  }
}
