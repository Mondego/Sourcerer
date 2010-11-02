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
package edu.uci.ics.sourcerer.clusterer.dir;

import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;
import static edu.uci.ics.sourcerer.util.io.Properties.INPUT;
import static edu.uci.ics.sourcerer.util.io.Properties.OUTPUT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.base.IDirectory;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 *
 */
public class DirectoryClusterer {
  public static final Property<String> DIRECTORY_LISTING = new StringProperty("directory-listing", "dir-listing.txt", "List of all the directories in the repository.");
  public static final Property<String> COMPARISON_FILE = new StringProperty("comparison-file", "comp-file.txt", "File containing results of comparison.");
  
  public static void generateDirectoryListing() {
    logger.info("Loading repository...");
    Repository repo = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
    
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(new File(OUTPUT.getValue(), DIRECTORY_LISTING.getValue())));
      
      Collection<RepoProject> projects = repo.getProjects();
      int count = 0;
      for (RepoProject project : projects) {
        logger.info("Processing " + project + " (" + ++count + " of " + projects.size() + ")");
        Deque<IDirectory> stack = Helper.newStack(); 
        for (IDirectory dir : project.getFileSet().getRootDirectories()) {
          stack.add(dir);
        }
        while (!stack.isEmpty()) {
          IDirectory dir = stack.pop();
          for (IDirectory subDir : dir.getSubdirectories()) {
            stack.add(subDir);
          }
          
          StringBuilder builder = new StringBuilder();
          for (IJavaFile file : dir.getJavaFiles()) {
            builder.append(" " + file.getFile().getName());
          }
          
          if (builder.length() > 0) {
            bw.write(project.getProjectRoot().getRelativePath());
            bw.write(" " + dir.toString());
            bw.write(builder.append('\n').toString());
          }
        }
      }
      logger.info("Done!");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in writing directory listing.", e);
    } finally {
      FileUtils.close(bw);
    }
  }
  
  private static ArrayList<Directory> loadDirectoryListing() {
    ArrayList<Directory> dirs = Helper.newArrayList();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(new File(INPUT.getValue(), DIRECTORY_LISTING.getValue())));
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        String[] files = new String[parts.length - 2];
        System.arraycopy(parts, 2, files, 0, parts.length - 2);
        Arrays.sort(files);
        dirs.add(new Directory(parts[0], parts[1], files));
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error loading directory listing", e);
    } finally {
      FileUtils.close(br);
    }
    return dirs;
  }
  
  public static void performComparison() {
    logger.info("Loading directory listing...");
    ArrayList<Directory> dirs = loadDirectoryListing();

    logger.info("Performing pairwise comparison...");
    for (int i = 0; i < dirs.size(); i++) {
      Directory dir = dirs.get(i);
      logger.info("  Comparing dir from project " + dir + " (" + (i + 1) + " of " + dirs.size() + ")");
      for (int j = i + 1; j < dirs.size(); j++) {
        dir.compare(dirs.get(j));
      }
    }
    
    int matching30 = 0;
    int matching50 = 0;
    int matching80 = 0;
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(COMPARISON_FILE);
    printer.beginTable(6);
    printer.addDividerRow();
    printer.addRow("Project", "Path", "Files", "Count 30%", "Count 50%", "Count 80%");
    printer.addDividerRow();
    for (Directory dir : dirs) {
      if (dir.matches30()) {
        matching30++;
      }
      if (dir.matches50()) {
        matching50++;
      }
      if (dir.matches80()) {
        matching80++;
      }
      dir.writeRow(printer);
    }
    printer.addDividerRow();
    printer.close();
    logger.info("Directories that matches 30% of at least one other directory: " + matching30);
    logger.info("Directories that matches 50% of at least one other directory: " + matching50);
    logger.info("Directories that matches 80% of at least one other directory: " + matching80);
    
  }
}
