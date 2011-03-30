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
package edu.uci.ics.sourcerer.clusterer.cloning.method.fingerprint;

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
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.LineFileWriter;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;
import edu.uci.ics.sourcerer.util.io.properties.DoubleProperty;
import edu.uci.ics.sourcerer.util.io.properties.IntegerProperty;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FingerprintClusterer {
  public static final Property<String> FINGERPRINT_FILE_LISTING = new StringProperty("fingerprint-file-listing", "fingerprint-file-list.txt", "List of all the files (and the types inside the files) in the repository.");
  public static final Property<String> EXCLUDED_NAMES_FILE = new StringProperty("excluded-names-file", "excluded-names.txt", "List of excluded names.");
  
  public static final Property<Boolean> REQUIRE_FINGERPRINT_NAME_MATCH = new BooleanProperty("require-fingerprint-name-match", true, "Give special priority to class names.");
  public static final Property<Double> MINIMUM_JACCARD_INDEX = new DoubleProperty("minimum-jaccard-index", .75, "Minimum jaccard index to count as a match.");
  public static final Property<Integer> MINIMUM_FINGERPRINT_SIZE = new IntegerProperty("minimum-fingerprint-size", 5, "Minimum number of fingerprint names.");
  
  private static class FingerprintDatabaseAccessor extends DatabaseAccessor {
    public FingerprintDatabaseAccessor(DatabaseConnection connection) {
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
    
    public Collection<MediumEntityDB> getContainedEntities(Integer entityID) {
      return joinQueries.getContainedEntities(entityID);
    }
  }
  
  public static void generateFileListing() {
    DatabaseConnection conn = new DatabaseConnection();
    if (conn.open()) {
      LineFileWriter writer = null;
      LineFileWriter.EntryWriter<FingerprintFile> ew = null;
      FingerprintDatabaseAccessor accessor = null;
      
      try {
        writer = FileUtils.getLineFileWriter(FINGERPRINT_FILE_LISTING);
        ew = writer.getEntryWriter(FingerprintFile.class);
        accessor = new FingerprintDatabaseAccessor(conn);
        
        Collection<SmallProjectDB> projects = accessor.getProjectIDs();
        int count = 0;
        FingerprintFile fingerprintFile = new FingerprintFile();
        Collection<String> fields = Helper.newArrayList();
        Collection<String> constructors = Helper.newArrayList();
        Collection<String> methods = Helper.newArrayList();
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
                for (MediumEntityDB contained : accessor.getContainedEntities(shortest.getEntityID())) {
                  switch (contained.getType()) {
                    case FIELD:
                    case ENUM_CONSTANT:
                      fields.add(contained.getFqn());
                      break;
                    case CONSTRUCTOR:
                      constructors.add(contained.getFqn());
                      break;
                    case METHOD:
                    case ANNOTATION_ELEMENT:
                      methods.add(contained.getFqn());
                      break;
                    default:
                  }
                }
                String name = shortest.getFqn().substring(shortest.getFqn().lastIndexOf('.') + 1);
                fingerprintFile.set(project.getPath(), file.getPath(), name, fields.toArray(new String[fields.size()]), constructors.toArray(new String[constructors.size()]), methods.toArray(new String[methods.size()]));
                ew.write(fingerprintFile);
                fields.clear();
                constructors.clear();
                methods.clear();
              }
            }
          }
        }
        logger.info("Done!");
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing/reading to/from file", e);
      } finally {
        FileUtils.close(writer);
        FileUtils.close(ew);
        FileUtils.close(accessor);
      }
    }
  }
 
  public static void loadFileListing(ProjectMap projects) {
    try {
      logger.info("Loading fingerprint file listing...");
      int count = 0;
      for (FingerprintFile fingerprintFile : FileUtils.readLineFile(FingerprintFile.class, FINGERPRINT_FILE_LISTING)) {
        count++;
        projects.addFile(fingerprintFile);
      }
      logger.info("  " + count + " files loaded");
      projects.getFingerprintFactory().clearPopularNames();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading fingerprint file listing.", e);
    }
  }
  
  public static void computeConfidence(ProjectMap projects) {
    logger.info("Computing fingerprint confidence...");
    
  }
  
//  public static void generateFilteredList(Filter filter) {
//    BufferedReader br = null;
//    BufferedWriter bw = null;
//    try {
//       br = FileUtils.getBufferedReader(FINGERPRINT_FILE_LISTING);
//       bw = FileUtils.getBufferedWriter(FILTERED_FINGERPRINT_FILE_LISTING);
//       
//       for (String line = br.readLine(); line != null; line = br.readLine()) {
//         String[] parts = line.split(" ");
//         if (parts.length >= 6) {
//           if (filter.singlePass(parts[0], parts[2])) {
//             bw.write(line + "\n");
//           }
//         } else {
//           logger.log(Level.SEVERE, "Invalid line: " + line);
//         }
//       }
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error generating filtered list.", e);
//    } finally {
//      FileUtils.close(br);
//      FileUtils.close(bw);
//    }
//  }
//  
//  public static Matching getMatching() {
//    return getMatching(FINGERPRINT_FILE_LISTING);
//  }
//  
//  public static Matching getFilteredMatching() {
//    return getMatching(FILTERED_FINGERPRINT_FILE_LISTING);
//  }
//  
//  private static Matching getMatching(Property<String> property) {
//    logger.info("Processing fingerprint file listing...");
//    
//    BufferedReader br = null;
//    try {
//      br = FileUtils.getBufferedReader(property);
//      
//      Matching matching = new Matching();
//      Map<FingerprintMatcher, FileCluster> files = Helper.newHashMap();
//      for (String line = br.readLine(); line != null; line = br.readLine()) {
//        try {
//          String[] parts = line.split(" ");
//          if (parts.length >= 6) {
//            String[] names = new String[parts.length - 5];
//            int startIdx = parts[4].lastIndexOf('.') + 1;
//            String prefix = parts[4].substring(0, startIdx);
//            names[0] = parts[4].substring(startIdx);
//            for (int i = 6; i < parts.length; i++) {
//              if (parts[i].startsWith(prefix)) {
//                names[i - 5] = parts[i].substring(startIdx);
//              } else {
//                names[i - 5] = parts[i];
//              }
//            }
//            Arrays.sort(names, 1, names.length);
//            FingerprintMatcher matcher = new FingerprintMatcher(names);
//            FileCluster cluster = files.get(matcher);
//            if (cluster == null) {
//              cluster = new FileCluster();
//              files.put(matcher, cluster);
//              matching.addCluster(cluster);
//            }
//            cluster.addProjectUniqueFile(parts[0], parts[2]);
//          } else {
//            logger.log(Level.SEVERE, "Invalid line: " + line);
//          }
//        } catch (IndexOutOfBoundsException e) {
//          logger.log(Level.SEVERE, "Invalid line (substring problem): " + line, e);
//        }
//      }
//      return matching;
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error in reading file listing.", e);
//      return null;
//    } finally {
//      FileUtils.close(br);
//    }
//  }
  
//  public static Matching getFilteredMatching(Filter filter) {
//    logger.info("Processing fingerprint file listing...");
//    
//    BufferedReader br = null;
//    try {
//      br = new BufferedReader(new FileReader(new File(Properties.INPUT.getValue(), FINGERPRINT_FILE_LISTING.getValue())));
//      
//      Matching matching = new Matching();
//      Map<FingerprintMatcher, FileCluster> files = Helper.newHashMap();
//      for (String line = br.readLine(); line != null; line = br.readLine()) {
//        try {
//          String[] parts = line.split(" ");
//          if (parts.length >= 6) {
//            if (filter.pass(parts[0], parts[2])) {
//              String[] names = new String[parts.length - 5];
//              int startIdx = parts[4].lastIndexOf('.') + 1;
//              String prefix = parts[4].substring(0, startIdx);
//              names[0] = parts[4].substring(startIdx);
//              for (int i = 6; i < parts.length; i++) {
//                if (parts[i].startsWith(prefix)) {
//                  names[i - 5] = parts[i].substring(startIdx);
//                } else {
//                  names[i - 5] = parts[i];
//                }
//              }
//              Arrays.sort(names, 1, names.length);
//              FingerprintMatcher matcher = new FingerprintMatcher(names);
//              FileCluster cluster = files.get(matcher);
//              if (cluster == null) {
//                cluster = new FileCluster();
//                files.put(matcher, cluster);
//                matching.addCluster(cluster);
//              }
//              cluster.addProjectUniqueFile(parts[0], parts[2]);
//            }
//          } else {
//            logger.log(Level.SEVERE, "Invalid line: " + line);
//          }
//        } catch (IndexOutOfBoundsException e) {
//          logger.log(Level.SEVERE, "Invalid line (substring problem): " + line, e);
//        }
//      }
//      return matching;
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error in reading file listing.", e);
//      return null;
//    } finally {
//      FileUtils.close(br);
//    }
//  }
}
