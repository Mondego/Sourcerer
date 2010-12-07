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
package edu.uci.ics.sourcerer.clusterer.hash;

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
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.base.IDirectory;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.FileUtils;
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
      bw = new BufferedWriter(new FileWriter(new File(OUTPUT.getValue(), HASH_FILE_LISTING.getValue())));
      
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
            File f = file.getFile().toFile();
            Pair<String, String> hashes = FileUtils.computeHashes(f);
            
            if (hashes != null) {
              StringBuilder builder = new StringBuilder();
              builder.append(project.getProjectRoot().getRelativePath());
              builder.append(" ").append(file.getFile().getRelativePath());
              builder.append(" ").append(hashes.getFirst());
              builder.append(" ").append(hashes.getSecond());
              builder.append(" ").append(f.length()).append("\n");
              bw.write(builder.toString());
            }
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
                      next = parts[0] + "/" + parts[1];
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
  
  public static void compileMatchingStatistics() {
    match();
  }
  
  public static void match() {
    logger.info("Processing file listing...");
    
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(new File(INPUT.getValue(), HASH_FILE_LISTING.getValue())));
      
      final Map<HashingMatcher, HashingMatcher> files = Helper.newHashMap();
      HashingMatcher nextItem = new HashingMatcher();
      int count = 0;
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        if (parts.length == 5) {
          if (++count % 1000 == 0) {
            logger.info("Processed " + count + " lines...");
          }
          nextItem.setValues(parts[2], parts[3], Long.parseLong(parts[4]));
          
          if (nextItem.getLength() > 0) {
            HashingMatcher matcher = files.get(nextItem);
            if (matcher == null) {
              matcher = nextItem.copy();
              files.put(matcher, matcher);
            }
            matcher.add(parts[0], parts[1]);
          }
          
        } else {
          logger.log(Level.SEVERE, "Invalid line: " + line);
        }
      }
      logger.info("Finished processing " + count + " lines.");
      
      int totalNoProjectDups = 0; 
      int copiedCount = 0;
      for (HashingMatcher matcher : files.values()) {
        if (matcher.getProjectCount() > 1) {
          copiedCount++;
        }
        totalNoProjectDups += matcher.getProjectCount();
      }
      
      Set<HashingMatcher> orderedSet = Helper.newTreeSet(new Comparator<HashingMatcher>() {
        @Override
        public int compare(HashingMatcher o1, HashingMatcher o2) {
          int a = files.get(o1).getProjectCount();
          int b = files.get(o2).getProjectCount();
          if (a == b) {
            return o1.getMD5().compareTo(o2.getMD5());
          } else {
            return b - a;
          }
        }});
      
      orderedSet.addAll(files.values());
      
      for (HashingMatcher matcher : orderedSet) {
        if (matcher.getProjectCount() <= 1) {
          break;
        }
        logger.info(matcher.toString());
      }
      
      logger.info(count + " total files.");
      logger.info(totalNoProjectDups + " total files (eliminating within-project duplicates).");
      logger.info(files.size() + " unique files.");
      logger.info(copiedCount + " duplicated files.");
      
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading file listing.", e);
    } finally {
      FileUtils.close(br);
    }
  }
}
