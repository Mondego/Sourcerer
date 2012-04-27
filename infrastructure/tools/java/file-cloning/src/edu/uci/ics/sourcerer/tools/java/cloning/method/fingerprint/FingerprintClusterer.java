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
package edu.uci.ics.sourcerer.tools.java.cloning.method.fingerprint;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.cloning.method.ProjectMap;
import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.util.io.EntryWriter;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.SimpleDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;
import edu.uci.ics.sourcerer.util.io.arguments.DoubleArgument;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.arguments.IntegerArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FingerprintClusterer {
  public static final DualFileArgument FINGERPRINT_FILE_LISTING = new DualFileArgument("fingerprint-file-listing", "fingerprint-file-list.txt", "List of all the files (and the types inside the files) in the repository.");
  public static final DualFileArgument EXCLUDED_NAMES_FILE = new DualFileArgument("excluded-names-file", "excluded-names.txt", "List of excluded names.");
  
  public static final Argument<Boolean> REQUIRE_FINGERPRINT_NAME_MATCH = new BooleanArgument("require-fingerprint-name-match", true, "Give special priority to class names.");
  public static final Argument<Double> MINIMUM_JACCARD_INDEX = new DoubleArgument("minimum-jaccard-index", .75, "Minimum jaccard index to count as a match.");
  public static final Argument<Integer> MINIMUM_FINGERPRINT_SIZE = new IntegerArgument("minimum-fingerprint-size", 5, "Minimum number of fingerprint names.");
  
  public static void generateFileListing() {
    new DatabaseRunnable() {
      @Override
      protected void action() {
        TaskProgressLogger task = TaskProgressLogger.get();
        try (SimpleSerializer out = IOUtils.makeSimpleSerializer(FINGERPRINT_FILE_LISTING);
             EntryWriter<FingerprintFile> ew = out.getEntryWriter(FingerprintFile.class)) {
          
          task.start("Loading project listing");
          Collection<Integer> projects = null;
          try (SelectQuery query = exec.createSelectQuery(ProjectsTable.TABLE)) {
            query.addSelect(ProjectsTable.PROJECT_ID);
            query.andWhere(ProjectsTable.PROJECT_TYPE.compareEquals(Project.CRAWLED));
            projects = query.select().toCollection(ProjectsTable.PROJECT_ID);
          }
          task.finish();

          task.start("Processing projects", "projects processed", 500);
          FingerprintFile fingerprintFile = new FingerprintFile();
          Collection<String> fields = new ArrayList<>();
          Collection<String> constructors = new ArrayList<>();
          Collection<String> methods = new ArrayList<>();
          try (SelectQuery filesQuery = exec.createSelectQuery(FilesTable.TABLE)) {
            filesQuery.addSelect(FilesTable.FILE_ID);
            ConstantCondition<Integer> compProjectID = FilesTable.PROJECT_ID.compareEquals();
            filesQuery.andWhere(compProjectID.and(FilesTable.FILE_TYPE.compareEquals(File.SOURCE)));
            
            for (Integer projectID : projects) {
              compProjectID.setValue(projectID);
              for (Integer fileID : filesQuery.select().toCollection(FilesTable.FILE_ID)) {
                //TODO look up the primary top-level type for this file and fingerprint it
//                if (file.getType() == edu.uci.ics.sourcerer.model.File.SOURCE) {
//                  MediumEntityDB shortest = null;
//                  for (MediumEntityDB entity : accessor.getMediumTopLevelByFileID(file.getFileID())) {
//                    if (shortest == null) {
//                      shortest = entity;
//                    } else {
//                      if (entity.getFqn().length() < shortest.getFqn().length() ) {
//                        shortest = entity;
//                      }
//                    }
//                  }
//                  if (shortest == null) {
//                    logger.log(Level.SEVERE, "Unable to find entities for file: " + file);
//                  } else {
//                    for (MediumEntityDB contained : accessor.getContainedEntities(shortest.getEntityID())) {
//                      switch (contained.getType()) {
//                        case FIELD:
//                        case ENUM_CONSTANT:
//                          fields.add(contained.getFqn());
//                          break;
//                        case CONSTRUCTOR:
//                          constructors.add(contained.getFqn());
//                          break;
//                        case METHOD:
//                        case ANNOTATION_ELEMENT:
//                          methods.add(contained.getFqn());
//                          break;
//                        default:
//                      }
//                    }
//                    String name = shortest.getFqn().substring(shortest.getFqn().lastIndexOf('.') + 1);
//                    fingerprintFile.set(project.getPath(), file.getPath(), name, fields.toArray(new String[fields.size()]), constructors.toArray(new String[constructors.size()]), methods.toArray(new String[methods.size()]));
//                    ew.write(fingerprintFile);
//                    fields.clear();
//                    constructors.clear();
//                    methods.clear();
//                  }
//                }
              }
              task.progress();
            }
          }
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Error writing/reading to/from file", e);
        }
        task.finish();
      }
    }.run();
  }
 
  public static void loadFileListing(ProjectMap projects) {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Loading fingerprint file listing", "files loaded", 500);
    try (SimpleDeserializer deserializer = IOUtils.makeSimpleDeserializer(FINGERPRINT_FILE_LISTING)) {
      for (FingerprintFile fingerprintFile : deserializer.deserializeToIterable(FingerprintFile.class)) {
        projects.addFile(fingerprintFile);
        task.progress();
      }
      projects.getFingerprintFactory().clearPopularNames();
      task.finish();
    } catch (IOException e) {
      task.exception(e);
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
