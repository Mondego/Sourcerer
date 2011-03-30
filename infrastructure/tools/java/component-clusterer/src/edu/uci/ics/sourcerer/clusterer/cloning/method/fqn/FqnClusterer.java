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
package edu.uci.ics.sourcerer.clusterer.cloning.method.fqn;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.clusterer.cloning.basic.ProjectMap;
import edu.uci.ics.sourcerer.db.queries.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.model.db.FileDB;
import edu.uci.ics.sourcerer.model.db.MediumEntityDB;
import edu.uci.ics.sourcerer.model.db.SmallProjectDB;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.LineFileWriter;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.IntegerProperty;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnClusterer {
  public static final Property<String> FQN_FILE_LISTING = new StringProperty("fqn-file-listing", "fqn-file-listing.txt", "List of all the files (and their FQNs) in the repository.");
  public static final Property<Integer> MINIMUM_FQN_DOTS = new IntegerProperty("minimum-fqn-dots", 3, "Minimum number of dots for an fqn to be given high confidence.");

  private static class FqnDatabaseAccessor extends DatabaseAccessor {
    public FqnDatabaseAccessor(DatabaseConnection connection) {
      super(connection);
    }
    
    public Collection<SmallProjectDB> getProjectIDs() {
      return projectQueries.getSmallByType(edu.uci.ics.sourcerer.model.Project.CRAWLED);
    }
    
    public Collection<FileDB> getFilesByProjectID(Integer projectID) {
      return fileQueries.getFilesByProjectID(projectID);
    }
    
    public Collection<MediumEntityDB> getMediumTopLevelByFileID(Integer fileID) {
      return entityQueries.getMediumTopLevelByFileID(fileID);
    }
  }
  
  public static void generateFileListing() {
    DatabaseConnection conn = new DatabaseConnection();
    if (conn.open()) {
      LineFileWriter writer = null;
      LineFileWriter.EntryWriter<FqnFile> ew = null;
      FqnDatabaseAccessor accessor = null;
      try {
        writer = FileUtils.getLineFileWriter(FQN_FILE_LISTING);
        ew = writer.getEntryWriter(FqnFile.class);
        accessor = new FqnDatabaseAccessor(conn);
        
        Collection<SmallProjectDB> projects = accessor.getProjectIDs();
        int count = 0;
        FqnFile fqnFile = new FqnFile();
        for (SmallProjectDB project : projects) {
          logger.info("Processing " + project + " (" + ++count + " of " + projects.size() + ")");
          
          for (FileDB file : accessor.getFilesByProjectID(project.getProjectID())) {
            if (file.getType() == edu.uci.ics.sourcerer.model.File.SOURCE) {
              MediumEntityDB shortest = null;
              for (MediumEntityDB entity : accessor.getMediumTopLevelByFileID(file.getFileID())) {
                if (shortest == null) {
                  shortest = entity;
                } else {
                  if (entity.getFqn().length() < shortest.getFqn().length() ) {
                    shortest = entity;
                  }
                }
              }
              if (shortest == null) {
                logger.log(Level.SEVERE, "Unable to find entities for file: " + file);
              } else {
                fqnFile.set(project.getPath(), file.getPath(), shortest.getFqn());
                ew.write(fqnFile);
              }
            }
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error in writing file listing.", e);
      } finally {
        FileUtils.close(writer);
        FileUtils.close(ew);
        FileUtils.close(accessor);
      }
    }
  }
  
  public static void loadFileListing(ProjectMap projects) {
    try {
      logger.info("Loading fqn file listing...");
      int count = 0;
      for (FqnFile fqnFile : FileUtils.readLineFile(FqnFile.class, FQN_FILE_LISTING)) {
        count++;
        projects.addFile(fqnFile);
      }
      logger.info("  " + count + " files loaded");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading fqn file listing.", e);
    }
  }
  
//  public static void computeConfidence(ProjectMap projects) {
//    logger.info("Computing fqn confidence...");
//    ProjectMatchSet matches = projects.getProjectMatchSet();
//    
//    int setToLow = 0;
//    int setToMedium = 0;
//    
//    for (SimpleKey<String> key : projects.getKeyFactory().getFqnKeys()) {
//      String fqn = key.getKey();
//      if (fqn.startsWith("default.")) {
//        key.setConfidence(Confidence.LOW);
//        setToLow++;
//      } else {
//        int totalCount = 0;
//        int lowCount = 0;
//        ArrayList<File> files = key.getFiles();
//        for (int i = 0, length = files.size(); i < length; i++) {
//          for (int j = i + 1; j < length; j++) {
//            totalCount++;
//            FileMatch match = matches.getFileMatch(files.get(i).getProject(), files.get(j).getProject());
//            if (match.getSharedCount() == 0 && match.getUniqueFqnCount() < 5) {
//              lowCount++;
//            }
//          }
//        }
//        if (lowCount > 0) {
//            key.setConfidence(Confidence.MEDIUM);
//            setToMedium++;
//        }
//      }
//    }
//    logger.info("  Of " + projects.getKeyFactory().getFqnKeys().size() + " total keys, " + setToLow + " set to low and " + setToMedium + " set to medium confidence.");
//  }
//  private static class FqnDatabaseAccessor extends DatabaseAccessor {
//    public FqnDatabaseAccessor(DatabaseConnection connection) {
//      super(connection);
//    }
//    
//    
//    public Iterable<FileFqn> getFileFqns() {
//      return joinQueries.getFileFqns();
//    }
//  }
//  
//  public static void generateFileListing() {
//    DatabaseConnection conn = new DatabaseConnection();
//    if (conn.open()) {
//      FqnDatabaseAccessor accessor = null;
//      BufferedWriter bw = null;
//      try {
//        accessor = new FqnDatabaseAccessor(conn);
//        bw = new BufferedWriter(new FileWriter(new File(Properties.OUTPUT.getValue(), FQN_FILE_LISTING.getValue())));
//        logger.info("Issuing database query...");
//        int count = 0;
//        for (FileFqn file : accessor.getFileFqns()) {
//          if (++count % 10000 == 0) {
//            logger.info(count + " rows written");
//          }
//          bw.write(file.getProject() + " " + file.getProjectID() + " " + file.getPath() + " " + file.getFileID() + " " + file.getFqn() + " " + file.getEntityID() + "\n");
//        }
//        logger.info("Done!");
//      } catch (IOException e) {
//        logger.log(Level.SEVERE, "Error writing to file", e);
//      } finally {
//        FileUtils.close(accessor);
//        FileUtils.close(bw);
//      }
//    }
//  }
//  
//  public static Iterable<String> loadFileListing() {
//    return new Iterable<String>() {
//      @Override
//      public Iterator<String> iterator() {
//        return new Iterator<String>() {
//          private BufferedReader br = null;
//          private String next = null;
//          
//          {
//            try {
//              br = FileUtils.getBufferedReader(FQN_FILE_LISTING);
//            } catch (IOException e) {
//              logger.log(Level.SEVERE, "Error in reading file listing.", e);
//            }
//          }
//          
//          @Override
//          public boolean hasNext() {
//            if (next == null) {
//              if (br == null) {
//                return false;
//              } else {
//                while (next == null && br != null) {
//                  String line = null;
//                  try {
//                    line = br.readLine();
//                  } catch (IOException e) {
//                    logger.log(Level.SEVERE, "Error in reading file listing.", e);
//                  }
//                  if (line == null) {
//                    FileUtils.close(br);
//                    br = null;
//                  } else {
//                    String[] parts = line.split(" ");
//                    if (parts.length == 6) {
//                      next = parts[0] + ":" + parts[2];
//                    } else {
//                      logger.log(Level.SEVERE, "Invalid file line: " + line);
//                    }
//                  }
//                }
//                return next != null;
//              }
//            } else {
//              return true;
//            }
//          }
//          
//          @Override
//          public String next() {
//            if (hasNext()) {
//              String retval = next;
//              next = null;
//              return retval;
//            } else {
//              throw new NoSuchElementException();
//            }
//          }
//          
//          @Override
//          public void remove() {
//            throw new UnsupportedOperationException();
//          }
//        };
//      }
//    };
//  }
//  
//  public static void generateFilteredList(Filter filter) {
//    BufferedReader br = null;
//    BufferedWriter bw = null;
//    try {
//      br = FileUtils.getBufferedReader(FQN_FILE_LISTING);
//      bw = FileUtils.getBufferedWriter(FILTERED_FQN_FILE_LISTING);
//      
//      for (String line = br.readLine(); line != null; line = br.readLine()) {
//        String[] parts = line.split(" ");
//        if (parts.length == 6) {
//          if (filter.singlePass(parts[0], parts[2])) {
//            bw.write(line + "\n");
//          }
//        } else {
//          logger.log(Level.SEVERE, "Invalid line: " + line);
//        }
//      }
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error generating filtered list.", e);
//    } finally {
//      FileUtils.close(br);
//      FileUtils.close(bw);
//    }
//  }
//  
//  public static Matching getMatching() {
//    return getMatching(FQN_FILE_LISTING);
//  }
//  
//  public static Matching getFilteredMatching() {
//    return getMatching(FILTERED_FQN_FILE_LISTING);
//  }
//  
//  private static Matching getMatching(Property<String> property) {
//    logger.info("Processing fqn file listing...");
//    
//    BufferedReader br = null;
//    try {
//      br = FileUtils.getBufferedReader(property);
//      
//      Matching matching = new Matching();
//      
//      Map<String, FileCluster> files = Helper.newHashMap();
//      for (String line = br.readLine(); line != null; line = br.readLine()) {
//        String[] parts = line.split(" ");
//        if (parts.length == 6) {
//          FileCluster cluster = files.get(parts[4]);
//          if (cluster == null) {
//            cluster = new FileCluster();
//            files.put(parts[4], cluster);
//            matching.addCluster(cluster);
//          }
//          cluster.addProjectUniqueFile(parts[0], parts[2]);
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
//  
////  public static Matching getFilteredMatching(Filter filter) {
////    logger.info("Processing fqn file listing...");
////    
////    BufferedReader br = null;
////    try {
////      br = new BufferedReader(new FileReader(new File(Properties.INPUT.getValue(), FQN_FILE_LISTING.getValue())));
////      
////      Matching matching = new Matching();
////      
////      Map<String, FileCluster> files = Helper.newHashMap();
////      for (String line = br.readLine(); line != null; line = br.readLine()) {
////        String[] parts = line.split(" ");
////        if (parts.length == 6) {
////          if (filter.pass(parts[0], parts[2])) {
////            FileCluster cluster = files.get(parts[4]);
////            if (cluster == null) {
////              cluster = new FileCluster();
////              files.put(parts[4], cluster);
////              matching.addCluster(cluster);
////            }
////            cluster.addProjectUniqueFile(parts[0], parts[2]);
////          }
////        } else {
////          logger.log(Level.SEVERE, "Invalid line: " + line);
////        }
////      }
////      
////      return matching;
////    } catch (IOException e) {
////      logger.log(Level.SEVERE, "Error in reading file listing.", e);
////      return null;
////    } finally {
////      FileUtils.close(br);
////    }
////  }
}
 