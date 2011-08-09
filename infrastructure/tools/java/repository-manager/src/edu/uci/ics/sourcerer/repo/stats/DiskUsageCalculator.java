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

import java.io.File;
import java.util.Deque;

import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.general.IndexedJar;
import edu.uci.ics.sourcerer.repo.general.JarIndex;
import edu.uci.ics.sourcerer.tools.core.repo.base.Repository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.internal.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class DiskUsageCalculator {
  public static final DualFileArgument REPO_DISK_USAGE_FILE = new DualFileArgument("repo-disk-usage-file", "repo-disk-usage.txt", "File containing repository disk usage information.");
  
  public static void printRepositoryDiskUsage(Repository repo) {
    long totalSize = 0;
    long filteredSize = 0;
    long projectJarsSize = 0;
    long mavenJarsSize = 0;
    
    logger.info("Loading projects...");
    int count = 0;
    int max = repo.getProjectCount();
    for (RepoProject project : repo.getProjects()) {
      logger.info("Processing " + project.toString() + " (" + ++count + " of " + max + ")");

      // Compute the full size
      totalSize += computeDirectorySize(project.getContent().toFile());
      
      // Compute the filtered size
      IFileSet files = project.getFileSet();
      // Start with the unique files
      for (IJavaFile file : files.getUniqueJavaFiles()) {
        filteredSize += file.getFile().toFile().length();
      }
      // Then do the chosen duplicates
      for (IJavaFile file : files.getBestDuplicateJavaFiles()) {
        filteredSize += file.getFile().toFile().length();
      }
    }
    
    FileUtils.deleteTempDir();
    
    logger.info("Loading jar index...");
    JarIndex index = repo.getJarIndex();
    for (IndexedJar jar : index.getJars()) {
      if (jar.isMavenJar()) {
        mavenJarsSize += jar.getJarFile().length();
        if (jar.hasSource()) {
          mavenJarsSize += jar.getSourceFile().length();
        }
      } else {
        projectJarsSize += jar.getJarFile().length();
        if (jar.hasSource()) {
          projectJarsSize += jar.getSourceFile().length();
        }
      }
    }
    
    long gigabyteInBytes = 1024l * 1024l * 1024l;
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(REPO_DISK_USAGE_FILE);
    printer.setFractionDigits(2);
    printer.beginTable(3);
    printer.addDividerRow();
    printer.addRow("Item", "Size (bytes)", "Size (gb)");
    printer.addDividerRow();
    printer.beginRow();
    printer.addCell("Total Size");
    printer.addCell(totalSize);
    printer.addCell((double) totalSize / gigabyteInBytes);
    printer.beginRow();
    printer.addCell("Filtered Size");
    printer.addCell(filteredSize);
    printer.addCell((double) filteredSize / gigabyteInBytes);
    printer.beginRow();
    printer.addCell("Project Jars Size");
    printer.addCell(projectJarsSize);
    printer.addCell((double) projectJarsSize / gigabyteInBytes);
    printer.beginRow();
    printer.addCell("Maven Jars Size");
    printer.addCell(mavenJarsSize);
    printer.addCell((double) mavenJarsSize / gigabyteInBytes);
    printer.addDividerRow();
    printer.endTable();
    printer.close();
  }
  
  private static long computeDirectorySize(File dir) {
    if (dir.isFile()) {
      return dir.length();
    } else {
      long size = 0;
      Deque<File> stack = Helper.newStack();
      stack.add(dir);
      while (!stack.isEmpty()) {
        dir = stack.pop();
        for (File file : dir.listFiles()) {
          if (file.isDirectory()) {
            stack.push(file);
          } else {
            size += file.length();
          }
        }
      }
      return size;
    }
  }
}
