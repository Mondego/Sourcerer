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
package edu.uci.ics.sourcerer.clusterer.file;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.util.io.Properties.OUTPUT;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileClusterer {
  public static final Property<String> FILE_LISTING = new StringProperty("file-listing", "file-listing.txt", "List of all the files in the repository.");
  
  public static void generateFileListing() {
    logger.info("Loading repository...");
    Repository repo = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
    
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(new File(OUTPUT.getValue(), FILE_LISTING.getValue())));
      
      Collection<RepoProject> projects = repo.getProjects();
      int count = 0;
      for (RepoProject project : projects) {
        logger.info("Processing " + project + " (" + ++count + " of " + projects.size() + ")");
        Deque<IDirectory> stack = Helper.newStack();
        for (IDirectory dir : project.getFileSet().getRootDirectories()) {
          stack.add(dir);
        }
        int fileCount = 0;
        
        while (!stack.isEmpty()) {
          IDirectory dir = stack.pop();
          for (IDirectory subDir : dir.getSubdirectories()) {
            stack.add(subDir);
          }
          for (IJavaFile file : dir.getJavaFiles()) {
            if (++fileCount % 1000 == 0) {
              logger.info("  " + fileCount + " files processed...");
            }
            // Calculate the hash
            String hash = FileUtils.computeHash(file.getFile().toFile());
            
            StringBuilder builder = new StringBuilder();
            builder.append(project.getProjectRoot().getRelativePath());
            builder.append(" ").append(file.getFile().getRelativePath());
            builder.append(" ").append(hash).append("\n");
            bw.write(builder.toString());
          }
        }
      }
      logger.info("Done!");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in writing file listing.", e);
    } finally {
      FileUtils.close(bw);
    }
  }
}
