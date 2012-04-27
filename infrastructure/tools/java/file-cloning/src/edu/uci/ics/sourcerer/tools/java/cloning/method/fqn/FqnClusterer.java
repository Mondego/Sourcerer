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
package edu.uci.ics.sourcerer.tools.java.cloning.method.fqn;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.cloning.method.ProjectMap;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.arguments.IntegerArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnClusterer {
  public static final DualFileArgument FQN_FILE_LISTING = new DualFileArgument("fqn-file-listing", "fqn-file-listing.txt", "List of all the files (and their FQNs) in the repository.");
  public static final Argument<Integer> MINIMUM_FQN_DOTS = new IntegerArgument("minimum-fqn-dots", 3, "Minimum number of dots for an fqn to be given high confidence.");

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
}
 