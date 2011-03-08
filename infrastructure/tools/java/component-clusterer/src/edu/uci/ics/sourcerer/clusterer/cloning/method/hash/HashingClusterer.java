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
package edu.uci.ics.sourcerer.clusterer.cloning.method.hash;

import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;
import static edu.uci.ics.sourcerer.util.io.Properties.INPUT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.clusterer.cloning.stats.FileCluster;
import edu.uci.ics.sourcerer.clusterer.cloning.stats.Matching;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.LineBuilder;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class HashingClusterer {
  public static final Property<String> HASH_FILE_LISTING = new StringProperty("hash-file-listing", "hash-file-listing.txt", "List of all the files (and their hashes) in the repository.");
  
  public static void generateFileListing() {
    logger.info("Loading repository...");
    Repository repo = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
    
    BufferedWriter bw = null;
    try {
      bw = FileUtils.getBufferedWriter(HASH_FILE_LISTING);
      
      Collection<RepoProject> projects = repo.getProjects();
      int count = 0;
      for (RepoProject project : projects) {
        logger.info("Processing " + project + " (" + ++count + " of " + projects.size() + ")");
        LineBuilder builder = new LineBuilder();
        for (IJavaFile file : project.getFileSet().getFilteredJavaFiles()) {
          File f = file.getFile().toFile();
          Pair<String, String> hashes = FileUtils.computeHashes(f);
        
          if (hashes != null) {
            builder.addItem(project.getProjectRoot().getRelativePath());
            builder.addItem(file.getFile().getRelativePath());
            builder.addItem(hashes.getFirst());
            builder.addItem(hashes.getSecond());
            builder.addItem(Long.toString(f.length()));
            bw.write(builder.toLine());
            bw.newLine();
          }
        }
        FileUtils.resetTempDir();
      }
      logger.info("Done!");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in writing file listing.", e);
    } finally {
      FileUtils.close(bw);
    }
  }
  
  public static Iterable<String> loadFileListing() {
    return new Iterable<String>() {
      @Override
      public Iterator<String> iterator() {
        return new Iterator<String>() {
          private BufferedReader br = null;
          private String next = null;
          
          {
            try {
              br = new BufferedReader(new FileReader(new File(INPUT.getValue(), HASH_FILE_LISTING.getValue())));
            } catch (IOException e) {
              logger.log(Level.SEVERE, "Error in reading file listing.", e);
            }
          }
          
          @Override
          public boolean hasNext() {
            if (next == null) {
              if (br == null) {
                return false;
              } else {
                while (next == null && br != null) {
                  String line = null;
                  try {
                    line = br.readLine();
                  } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error in reading file listing.", e);
                  }
                  if (line == null) {
                    FileUtils.close(br);
                    br = null;
                  } else {
                    String[] parts = line.split(" ");
                    if (parts.length == 5) {
                      next = parts[0] + ":/" + parts[1];
                    } else {
                      logger.log(Level.SEVERE, "Invalid file line: " + line);
                    }
                  }
                }
                return next != null;
              }
            } else {
              return true;
            }
          }
          
          @Override
          public String next() {
            if (hasNext()) {
              String retval = next;
              next = null;
              return retval;
            } else {
              throw new NoSuchElementException();
            }
          }
          
          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
  
  public static Matching getMatching() {
    logger.info("Processing hash file listing...");
    
    BufferedReader br = null;
    try {
      br = FileUtils.getBufferedReader(HASH_FILE_LISTING);
      
      Matching matching = new Matching();
      
      HashingMatcher nextItem = new HashingMatcher();
      Map<HashingMatcher, FileCluster> files = Helper.newHashMap();
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        if (parts.length == 5) {
          nextItem.setValues(parts[2], parts[3], Long.parseLong(parts[4]));
          
          if (nextItem.getLength() > 0) {
            FileCluster cluster = files.get(nextItem);
            if (cluster == null) {
              cluster = new FileCluster();
              files.put(nextItem.copy(), cluster);
              matching.addCluster(cluster);
            }
            cluster.addFile(parts[0], parts[1]);
          }
        } else {
          logger.log(Level.SEVERE, "Invalid line: " + line);
        }
      }
      
      return matching;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading file listing.", e);
      return null;
    } finally {
      FileUtils.close(br);
    }
  }
}
