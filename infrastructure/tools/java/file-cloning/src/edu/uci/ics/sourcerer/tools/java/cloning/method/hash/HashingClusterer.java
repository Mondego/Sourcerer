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
package edu.uci.ics.sourcerer.tools.java.cloning.method.hash;

import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.tools.java.cloning.method.ProjectMap;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.LineFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.arguments.IOFileArgumentFactory;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class HashingClusterer {
  public static final DualFileArgument HASH_FILE_LISTING = new DualFileArgument("hash-file-listing", "hash-file-listing.txt", "List of all the files (and their hashes) in the repository.");
  
  public static void generateFileListing() {
    logger.info("Loading repository...");
    Repository repo = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
    
    LineFileWriter writer = null;
    LineFileWriter.EntryWriter<HashedFile> ew = null;
    try {
      writer = FileUtils.getLineFileWriter(HASH_FILE_LISTING);
      ew = writer.getEntryWriter(HashedFile.class);
      
      Collection<RepoProject> projects = repo.getProjects();
      int count = 0;
      HashedFile hashedFile = new HashedFile();
      for (RepoProject project : projects) {
        logger.info("Processing " + project + " (" + ++count + " of " + projects.size() + ")");
        for (IJavaFile file : project.getFileSet().getFilteredJavaFiles()) {
          java.io.File f = file.getFile().toFile();
          Pair<String, String> hashes = FileUtils.computeHashes(f);
        
          if (hashes != null) {
            hashedFile.set(
                project.getProjectRoot().getRelativePath(), 
                file.getFile().getRelativePath(),
                hashes.getFirst(),
                hashes.getSecond(),
                f.length());
            ew.write(hashedFile);
          }
        }
        FileUtils.cleanTempDir();
      }
      logger.info("Done!");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in writing file listing.", e);
    } finally {
      FileUtils.close(ew);
      FileUtils.close(writer);
    }
  }
  
  public static void loadFileListing(ProjectMap projects) {
    try {
      logger.info("Loading hash file listing...");
      int count = 0;
      for (HashedFile hashedFile : FileUtils.readLineFile(HashedFile.class, HASH_FILE_LISTING, "project", "path", "md5", "length")) {
        if (hashedFile.getLength() > 0) {
          count++;
          projects.addFile(hashedFile);
        }
      }
      logger.info("  " + count + " files loaded");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading hash file listing.", e);
    }
  }
  
//  public static void printProjectMatchingRates() {
//    logger.info("Loading file listing...");
//    ProjectMap projects = new ProjectMap();
//    loadFileListing(projects);
//    CounterSet<Project> counters = new CounterSet<Project>();
//    for (Project project : projects.getProjects()) {
//      // For each project, collect all the projects that it matches
//      for (File file : project.getFiles()) {
//        for (File otherFile : file.getHashKey().getFiles()) {
//          if (otherFile.getProject() != project) {
//            counters.increment(otherFile.getProject());
//          }
//        }
//      }
//      if (counters.getCounters().size() > 0) {
//        logger.info("Project " + project + " matches the follow projects: ");
//        for (Counter<Project> counter : counters.getCounters()) {
//          logger.info("  " + counter.getObject() + " " + counter.getCount());
//        }
//        counters.clear();
//      }
//    }
//    logger.info("Done!");
//  }
  
//  public static Matching getMatching() {
//    logger.info("Processing hash file listing...");
//    
//    BufferedReader br = null;
//    try {
//      br = FileUtils.getBufferedReader(HASH_FILE_LISTING);
//      
//      Matching matching = new Matching();
//      
//      HashingMatcher nextItem = new HashingMatcher();
//      Map<HashingMatcher, FileCluster> files = Helper.newHashMap();
//      for (String line = br.readLine(); line != null; line = br.readLine()) {
//        String[] parts = line.split(" ");
//        if (parts.length == 5) {
//          nextItem.setValues(parts[2], parts[3], Long.parseLong(parts[4]));
//          
//          if (nextItem.getLength() > 0) {
//            FileCluster cluster = files.get(nextItem);
//            if (cluster == null) {
//              cluster = new FileCluster();
//              files.put(nextItem.copy(), cluster);
//              matching.addCluster(cluster);
//            }
//            cluster.addFile(parts[0], parts[1]);
//          }
//        } else {
//          logger.log(Level.SEVERE, "Invalid line: " + line);
//        }
//      }
//      
//      return matching;
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error in reading file listing.", e);
//      return null;
//    } finally {
//      FileUtils.close(br);
//    }
//  }
}
