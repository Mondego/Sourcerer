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
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.clusterer.stats.EasyFilter;
import edu.uci.ics.sourcerer.clusterer.stats.FileCluster;
import edu.uci.ics.sourcerer.clusterer.stats.Filter;
import edu.uci.ics.sourcerer.clusterer.stats.Matching;
import edu.uci.ics.sourcerer.repo.base.IDirectory;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.Counter;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.properties.IntegerProperty;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class DirectoryClusterer {
  public static final Property<String> DIRECTORY_LISTING = new StringProperty("directory-listing", "dir-listing.txt", "List of all the directories in the repository.");

  public static final Property<String> MATCHED_DIRECTORIES = new StringProperty("dir-matched-dirs", "dir-matched-dirs.txt", "The results of matching the directories.");
  public static final Property<String> MATCHED_FILES = new StringProperty("dir-matched-files", "dir-matched-files.txt", "The results of matching the files.");
  public static final Property<String> FILTERED_MATCHED_FILES = new StringProperty("filtered-dir-matched-files", "filtered-dir-matched-files.txt", "The matched files filtered by the intersection list.");
  
  public static final Property<Integer> MINIMUM_MATCH_SIZE = new IntegerProperty("minimum-match-size", 5, "Minimum number of files that must match per directory.");
  
  public static final Property<String> POPULAR_NAMES = new StringProperty("popular-names", "popular-names.txt", "Filenames sorted by popularity.");
  public static final Property<Integer> POPULAR_DISCARD = new IntegerProperty("popular-discard", 500, "Discard the filenames that occur too often.");  
  
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
            builder.append(" " + file.getFile().getName().replace(' ', '*'));
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
  
  public static Iterable<String> loadFileListing() {
    return new Iterable<String>() {
      @Override
      public Iterator<String> iterator() {
        return new Iterator<String>() {
          private BufferedReader br = null;
          private Deque<String> next = Helper.newStack();
          
          {
            try {
              br = FileUtils.getBufferedReader(DIRECTORY_LISTING);
            } catch (IOException e) {
              logger.log(Level.SEVERE, "Error loading directory listing", e);
            }
          }
          
          @Override
          public boolean hasNext() {
            if (next.isEmpty()) {
              if (br == null) {
                return false;
              } else {
                while (next.isEmpty() && br != null) {
                  String line = null;
                  try {
                    line = br.readLine();
                  } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error reading directory listing", e);
                  }
                  
                  if (line == null) {
                    FileUtils.close(br);
                    br = null;
                  } else {
                    String[] parts = line.split(" ");
                    if (parts.length < 2) {
                      logger.log(Level.SEVERE, "Invalid directory line: " + line);
                    } else {
                      String basePath = parts[0] + ":";
                      if (parts[1].length() > 0) {
                        basePath += "/" + parts[1] + "/";
                      }
                      for (int i = 2; i < parts.length; i++) {
                        next.add(basePath + parts[i]);
                      }
                    }
                  }
                }
                return !next.isEmpty();
              }
            } else {
              return true;
            }
          }
          
          @Override
          public String next() {
            if (hasNext()) {
              return next.pop();
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
  
  private static ArrayList<Directory> loadDirectoryListing() {
    ArrayList<Directory> dirs = Helper.newArrayList();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(new File(INPUT.getValue(), DIRECTORY_LISTING.getValue())));
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        Arrays.sort(parts, 2, parts.length);
        JavaFile[] files = new JavaFile[parts.length - 2];
        for (int i = 2; i < parts.length; i++) {
          files[i - 2] = new JavaFile(parts[i]);
        }
        dirs.add(new Directory(parts[0], parts[1], files));
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error loading directory listing", e);
    } finally {
      FileUtils.close(br);
    }
    return dirs;
  }
  
  private static Collection<Counter<String>> computeNamePopularity(Iterable<Directory> dirs) {
    Map<String, Counter<String>> names = Helper.newHashMap();
    for (Directory dir : dirs) {
      for (JavaFile file : dir.getFiles()) {
        Counter<String> counter = names.get(file.getName());
        if (counter == null) {
          counter = new Counter<String>(file.getName());
          names.put(file.getName(), counter);
        }
        counter.increment();
      }
    }
    TreeSet<Counter<String>> retval = Helper.newTreeSet(Counter.<String>getReverseComparator()); 
    retval.addAll(names.values());
    return retval;
  }
  
  public static void generateComparisonFiles() {
    logger.info("Loading directory listing...");
    ArrayList<Directory> dirs = loadDirectoryListing();
    
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(POPULAR_NAMES);
    logger.info("Calculating name popularity");
    Collection<Counter<String>> names = computeNamePopularity(dirs);
    printer.addHeader("Filenames sorted by popularity");
    printer.beginTable(2);
    printer.addDividerRow();
    printer.addRow("Filename", "Count");
    printer.addDividerRow();
    for (Counter<String> name : names) {
      printer.beginRow();
      printer.addCell(name.getObject());
      printer.addCell(name.getCount());
    }
    printer.addDividerRow();
    printer.endTable();
    printer.close();
    
    // Build the set of filenames to ignore
    Set<String> ignore = Helper.newHashSet();
    for (Counter<String> name : names) {
      if (name.getCount() >= POPULAR_DISCARD.getValue()) {
        ignore.add(name.getObject());
      } else {
        break;
      }
    }
    names.clear();
    
    BufferedWriter matchedDirs = null;
    BufferedWriter matchedFiles = null;
    try {
      matchedDirs = new BufferedWriter(new FileWriter(new File(OUTPUT.getValue(), MATCHED_DIRECTORIES.getValue())));
      matchedFiles = new BufferedWriter(new FileWriter(new File(OUTPUT.getValue(), MATCHED_FILES.getValue())));
      logger.info("Performing pairwise comparison...");
      for (int i = 0; i < dirs.size(); i++) {
        Directory dir = dirs.get(i);
        logger.info("  Comparing dir from project " + dir + " (" + (i + 1) + " of " + dirs.size() + ")");
        for (int j = i + 1; j < dirs.size(); j++) {
          dir.compare(dirs.get(j), ignore);
        }
        // write out the dir info
        matchedDirs.write(dir.getProject() + " " + dir.getPath() + " ");
        matchedDirs.write(dir.getMatches80().size() + " " + dir.getMatches50().size() + " " + dir.getMatches30().size());
        for (String match : dir.getMatches80()) {
          matchedDirs.write(" " + match);
        }
        for (String match : dir.getMatches50()) {
          matchedDirs.write(" " + match);
        }
        for (String match : dir.getMatches30()) {
          matchedDirs.write(" " + match);
        }
        matchedDirs.write("\n");
        
        // write out the file
        for (JavaFile file : dir.getFiles()) {
          matchedFiles.write(dir.getProject() + " " + dir.getPath() + " " + file.getName() + " ");
          matchedFiles.write(file.getMatches80().size() + " " + file.getMatches50().size() + " " + file.getMatches30().size());
          for (String match : file.getMatches80()) {
            matchedFiles.write(" " + match);
          }
          for (String match : file.getMatches50()) {
            matchedFiles.write(" " + match);
          }
          for (String match : file.getMatches30()) {
            matchedFiles.write(" " + match);
          }
          matchedFiles.write("\n");
        }
        dirs.set(i, null);
      }
      logger.info("Done!");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Exception!", e);
    } finally {
      FileUtils.close(matchedDirs);
      FileUtils.close(matchedFiles);
    }
  }
  
  public static void generateFilteredListing(Filter filter) {
    logger.info("Processing dir file listing...");
    
    BufferedReader br = null;
    BufferedWriter bw = null;
    try {
      br = FileUtils.getBufferedReader(MATCHED_FILES);
      bw = FileUtils.getBufferedWriter(FILTERED_MATCHED_FILES);
      
      Set<String> dupDetector = Helper.newHashSet();
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        if (parts.length < 6) {
          logger.log(Level.SEVERE, "Invalid line: " + line);
        } else {
          
          int count80 = Integer.parseInt(parts[3]);
          int count50 = Integer.parseInt(parts[4]);
          int count30 = Integer.parseInt(parts[5]);
          String name = "/" + parts[2];
          if (filter.pass(parts[0], parts[1] + name) && !dupDetector.contains(parts[0] + parts[1] + name)) {
            dupDetector.add(parts[0] + parts[1] + name);
            StringBuilder first = new StringBuilder();
            first.append(parts[0]).append(" /").append(parts[1]).append(" ").append(parts[2]);
            
            StringBuilder second = new StringBuilder();
            int newCount80 = 0;
            for (int i = 6; i < 6 + count80; i++) {
              int colon = parts[i].indexOf(':');
              String project = parts[i].substring(0, colon);
              String path = parts[i].substring(colon + 1) + name;
              if (filter.pass(project, path)) {
                newCount80++;
                second.append(" ").append(project + ":/" + path);
              }
            }
            int newCount50 = 0;
            for (int i = 6 + count80; i < 6 + count80 + count50; i++) {
              int colon = parts[i].indexOf(':');
              String project = parts[i].substring(0, colon);
              String path = parts[i].substring(colon + 1) + name;
              if (filter.pass(project, path)) {
                newCount50++;
                second.append(" ").append(project + ":/" + path);
              }
            }
            int newCount30 = 0;
            for (int i = 6 + count80 + count50; i < 6 + count80 + count50 + count30; i++) {
              int colon = parts[i].indexOf(':');
              String project = parts[i].substring(0, colon);
              String path = parts[i].substring(colon + 1) + name;
              if (filter.pass(project, path)) {
                newCount30++;
                second.append(" ").append(project + ":/" + path);
              }
            }
            first.append(" ").append(newCount80).append(" ").append(newCount50).append(" ").append(newCount30).append(second).append("\n");
            bw.write(first.toString());
          }
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to generate filtered listing.", e);
    } finally {
      FileUtils.close(br);
      FileUtils.close(bw);
    }
  }
  
  public static Matching getMatching80() {
    return getMatching80(MATCHED_FILES);
  }
  
  public static Matching getFilteredMatching80() {
    return getMatching80(FILTERED_MATCHED_FILES);
  }
  
  private static Matching getMatching80(Property<String> property) {
    logger.info("Processing dir file listing...");
    
    BufferedReader br = null;
    try {
      br = FileUtils.getBufferedReader(property);
      
      Matching matching = new Matching();
      Map<String, FileCluster> files = Helper.newHashMap();
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        if (parts.length < 6) {
          logger.log(Level.SEVERE, "Invalid line: " + line);
        } else {
          int count = Integer.parseInt(parts[3]);
          String name = "/" + parts[2];
          if (count > 0) {
            String key = parts[0] + ":" + parts[1] + name;
            FileCluster cluster = files.get(key);
            for (int i = 6; cluster == null && i < 6 + count; i++) {
              cluster = files.get(parts[i] + name);
            }
            if (cluster == null) {
              cluster = new FileCluster();
            }
              
            if (!files.containsKey(key)) {
              files.put(key, cluster);
              cluster.addFile(parts[0], parts[1] + name);
            }
              
            for (int i = 6; i < 6 + count; i++) {
              int colon = parts[i].indexOf(':');
              String project = parts[i].substring(0, colon);
              String path = parts[i].substring(colon + 1) + name;
              key = project + ":" + path;
              FileCluster otherCluster = files.get(key);
              if (otherCluster == null) {
                files.put(key, cluster);
                cluster.addFile(project, path);
              } else if (cluster != otherCluster) {
                // Merge the two clusters
                // Update the values
                for (String otherPath : otherCluster.getPaths()) {
                  files.put(otherPath, cluster);
                  colon = otherPath.indexOf(':');
                  cluster.addFile(otherPath.substring(0, colon), otherPath.substring(colon + 1));
                }
              }
            }
          } else {
            FileCluster cluster = new FileCluster();
            cluster.addFile(parts[0], parts[1] + name);
            matching.addCluster(cluster);
          }
        }
      }
      
      for (FileCluster cluster : files.values()) {
        matching.addCluster(cluster);
      }
      return matching;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading file listing.", e);
      return null;
    } finally {
      FileUtils.close(br);
    }
  }
  
  
  public static Matching getMatching50() {
    return getMatching50(MATCHED_FILES);
  }
  
  public static Matching getFilteredMatching50() {
    return getMatching50(FILTERED_MATCHED_FILES);
  }
  
  private static Matching getMatching50(Property<String> property) {
    logger.info("Processing dir file listing...");
    
    BufferedReader br = null;
    try {
      br = FileUtils.getBufferedReader(property);
      
      Matching matching = new Matching();
      Map<String, FileCluster> files = Helper.newHashMap();
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        if (parts.length < 6) {
          logger.log(Level.SEVERE, "Invalid line: " + line);
        } else {
          int count = Integer.parseInt(parts[3]) + Integer.parseInt(parts[4]);
          String name = "/" + parts[2];
          if (count > 0) {
            String key = null;
            if (parts[1].startsWith("/")) {
              key = parts[0] + ":" + parts[1] + name;
            } else {
              key = parts[0] + ":/" + parts[1] + name;
            }
            FileCluster cluster = files.get(key);
            for (int i = 6; cluster == null && i < 6 + count; i++) {
              cluster = files.get(parts[i] + name);
            }
            if (cluster == null) {
              cluster = new FileCluster();
            }
              
            if (!files.containsKey(key)) {
              files.put(key, cluster);
              cluster.addFile(parts[0], parts[1] + name);
            }
              
            for (int i = 6; i < 6 + count; i++) {
              int colon = parts[i].indexOf(':');
              String project = parts[i].substring(0, colon);
              String path = parts[i].substring(colon + 1) + name;
              if (path.startsWith("/")) {
                key = project + ":" + path;
              } else {
                key = project + ":/" + path;
              }
              FileCluster otherCluster = files.get(key);
              if (otherCluster == null) {
                files.put(key, cluster);
                cluster.addFile(project, path);
              } else if (cluster != otherCluster) {
                // Merge the two clusters
                // Update the values
                for (String otherPath : otherCluster.getPaths()) {
                  files.put(otherPath, cluster);
                  colon = otherPath.indexOf(':');
                  cluster.addFile(otherPath.substring(0, colon), otherPath.substring(colon + 1));
                }
              }
            }
          } else {
            FileCluster cluster = new FileCluster();
            cluster.addFile(parts[0], parts[1] + name);
            matching.addCluster(cluster);
          }
        }
      }
      
      for (FileCluster cluster : files.values()) {
        matching.addCluster(cluster);
      }
      return matching;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading file listing.", e);
      return null;
    } finally {
      FileUtils.close(br);
    }
  }
  
  public static Matching getFilteredMatching50(Filter filter) {
    logger.info("Processing dir file listing...");
    
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(new File(INPUT.getValue(), MATCHED_FILES.getValue())));
      
      Matching matching = new Matching();
      Map<String, FileCluster> files = Helper.newHashMap();
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        if (parts.length < 6) {
          logger.log(Level.SEVERE, "Invalid line: " + line);
        } else {
          int count = Integer.parseInt(parts[3]) + Integer.parseInt(parts[4]);
          String name = "/" + parts[2];
          if (filter.pass(parts[0], parts[1] + name)) {
            if (count > 0) {
              String key = parts[0] + ":" + parts[1] + name;
              FileCluster cluster = files.get(key);
              for (int i = 6; cluster == null && i < 6 + count; i++) {
                cluster = files.get(parts[i] + name);
              }
              if (cluster == null) {
                cluster = new FileCluster();
              }
              
              if (!files.containsKey(key)) {
                files.put(key, cluster);
                cluster.addFile(parts[0], parts[1] + name);
              }
              
              for (int i = 6; i < 6 + count; i++) {
                key = parts[i] + name;
                FileCluster otherCluster = files.get(key);
                if (otherCluster == null) {
                  files.put(key, cluster);
                  int colon = key.indexOf(':');
                  cluster.addFile(key.substring(0, colon), key.substring(colon + 1));
                } else if (cluster != otherCluster) {
                  // Merge the two clusters
                  // Update the values
                  for (String path : otherCluster.getPaths()) {
                    files.put(path, cluster);
                    int colon = path.indexOf(':');
                    cluster.addFile(path.substring(0, colon), path.substring(colon + 1));
                  }
                }
              }
            } else {
              FileCluster cluster = new FileCluster();
              cluster.addFile(parts[0], parts[1] + name);
              matching.addCluster(cluster);
            }
          }
        }
      }
      
      
      for (FileCluster cluster : files.values()) {
        matching.addCluster(cluster);
      }
      return matching;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading file listing.", e);
      return null;
    } finally {
      FileUtils.close(br);
    }
  }
  
  public static Matching getMatching30() {
    return getFilteredMatching30(new EasyFilter());
  }
  
  public static Matching getFilteredMatching30(Filter filter) {
    logger.info("Processing dir file listing...");
    
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(new File(INPUT.getValue(), MATCHED_FILES.getValue())));
      
      Matching matching = new Matching();
      Map<String, FileCluster> files = Helper.newHashMap();
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        if (parts.length < 6) {
          logger.log(Level.SEVERE, "Invalid line: " + line);
        } else {
          int count = Integer.parseInt(parts[3]) + Integer.parseInt(parts[4]) + Integer.parseInt(parts[5]);
          String name = "/" + parts[2];
          if (filter.pass(parts[0], parts[1] + name)) {
            if (count > 0) {
              String key = parts[0] + ":" + parts[1] + name;
              FileCluster cluster = files.get(key);
              for (int i = 6; cluster == null && i < 6 + count; i++) {
                cluster = files.get(parts[i] + name);
              }
              if (cluster == null) {
                cluster = new FileCluster();
              }
              
              if (!files.containsKey(key)) {
                files.put(key, cluster);
                cluster.addFile(parts[0], parts[1] + name);
              }
              
              for (int i = 6; i < 6 + count; i++) {
                key = parts[i] + name;
                FileCluster otherCluster = files.get(key);
                if (otherCluster == null) {
                  files.put(key, cluster);
                  int colon = key.indexOf(':');
                  cluster.addFile(key.substring(0, colon), key.substring(colon + 1));
                } else if (cluster != otherCluster) {
                  // Merge the two clusters
                  // Update the values
                  for (String path : otherCluster.getPaths()) {
                    files.put(path, cluster);
                    int colon = path.indexOf(':');
                    cluster.addFile(path.substring(0, colon), path.substring(colon + 1));
                  }
                }
              }
            } else {
              FileCluster cluster = new FileCluster();
              cluster.addFile(parts[0], parts[1] + name);
              matching.addCluster(cluster);
            }
          }
        }
      }
      
      
      for (FileCluster cluster : files.values()) {
        matching.addCluster(cluster);
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
