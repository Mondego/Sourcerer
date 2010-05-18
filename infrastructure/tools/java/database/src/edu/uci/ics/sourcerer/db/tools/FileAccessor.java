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
package edu.uci.ics.sourcerer.db.tools;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.uci.ics.sourcerer.db.schema.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.model.File;
import edu.uci.ics.sourcerer.model.Project;
import edu.uci.ics.sourcerer.model.db.FileDB;
import edu.uci.ics.sourcerer.model.db.LocationDB;
import edu.uci.ics.sourcerer.model.db.ProjectDB;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
import edu.uci.ics.sourcerer.repo.general.IndexedJar;
import edu.uci.ics.sourcerer.repo.general.JarIndex;
import edu.uci.ics.sourcerer.util.TimeoutManager;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileAccessor {
  private static TimeoutManager<FileDatabaseAccessor> accessorManager = 
      new TimeoutManager<FileDatabaseAccessor>(new TimeoutManager.Instantiator<FileDatabaseAccessor>() {
        @Override
        public FileDatabaseAccessor create() {
          DatabaseConnection conn = new DatabaseConnection();
          conn.open();
          return new FileDatabaseAccessor(conn);
        }
      }, 10 * 60 * 1000);
  
  private static Repository repo = Repository.getRepository(AbstractRepository.INPUT_REPO.getValue());
  private static ExtractedRepository extracted = ExtractedRepository.getRepository(AbstractRepository.OUTPUT_REPO.getValue());
  
  public static byte[] lookupByProjectID(String projectID) {
    FileDatabaseAccessor db = accessorManager.get();
    ProjectDB project = db.getProjectByProjectID(projectID);
    if (project == null) {
      logger.log(Level.SEVERE, "Unable to find project: " + projectID);
      return null;
    } else {
      if (project.getType() == Project.SYSTEM) {
        logger.log(Level.SEVERE, project + " is a SYSTEM project");
        return null;
      } else if (project.getType() == Project.JAR || project.getType() == Project.MAVEN) {
        JarIndex index = repo.getJarIndex();
        IndexedJar indexed = index.getIndexedJar(project.getHash());
        if (indexed == null) {
          logger.log(Level.SEVERE, "Unable to find " + project + " with hash " + project.getHash());
          return null;
        } else {
          return FileUtils.getFileAsByteArray(indexed.getJarFile());
        }
      } else if (project.getType() == Project.JAVA_LIBRARY) {
        return FileUtils.getFileAsByteArray(extracted.getJavaLibrary(project.getPath()));
      } else if (project.getType() == Project.CRAWLED) {
        logger.log(Level.SEVERE, "Crawled projects not supported: " + project);
        return null;
      } else {
        return null;
      }
    }
  }
  
  public static byte[] lookupByFileID(String fileID) {
    FileDatabaseAccessor db = accessorManager.get();
    FileDB file = db.getFileByFileID(fileID);
    if (file == null) {
      logger.log(Level.SEVERE, "Unable to find file: " + fileID);
      return null;
    } else {
      ProjectDB project = db.getProjectByProjectID(file.getProjectID());
      return getFile(project, file, null);
    }
  }
  
  public static byte[] lookupByEntityID(String entityID) {
    FileDatabaseAccessor db = accessorManager.get();
    LocationDB loc = db.getLocationByEntityID(entityID);
    if (loc == null) {
      logger.log(Level.SEVERE, "Entity " + entityID + " has no associated file");
      return null;
    } else {
      FileDB file = db.getFileByFileID(loc.getFileID());
      if (file == null) {
        logger.log(Level.SEVERE, "Unable to find file: " + loc.getFileID());
        return null;
      } else {
        ProjectDB project = db.getProjectByProjectID(file.getProjectID());
        return getFile(project, file, loc);
      }
    }
  }
  
  public static byte[] lookupByRelationID(String relationID) {
    FileDatabaseAccessor db = accessorManager.get();
    LocationDB loc = db.getLocationByRelationID(relationID);
    if (loc == null) {
      logger.log(Level.SEVERE, "Relation " + relationID + " has no associated file");
      return null;
    } else {
      FileDB file = db.getFileByFileID(loc.getFileID());
      if (file == null) {
        logger.log(Level.SEVERE, "Unable to find file: " + loc.getFileID());
        return null;
      } else {
        ProjectDB project = db.getProjectByProjectID(file.getProjectID());
        return getFile(project, file, loc);
      }
    }
  }
  
  public static byte[] lookupByCommentID(String commentID) {
    FileDatabaseAccessor db = accessorManager.get();
    LocationDB loc = db.getLocationByCommentID(commentID);
    if (loc == null) {
      logger.log(Level.SEVERE, "Comment " + commentID + " has no associated file");
      return null;
    } else {
      FileDB file = db.getFileByFileID(loc.getFileID());
      if (file == null) {
        logger.log(Level.SEVERE, "Unable to find file: " + loc.getFileID());
        return null;
      } else {
        ProjectDB project = db.getProjectByProjectID(file.getProjectID());
        return getFile(project, file, loc);
      }
    }
  }
  
  private static byte[] getFile(ProjectDB project, FileDB file, LocationDB location) {
    if (file.getType() == File.JAR) {
      JarIndex index = repo.getJarIndex();
      IndexedJar indexed = index.getIndexedJar(file.getHash());
      if (indexed == null) {
        logger.log(Level.SEVERE, "Unable to find " + file + " with hash " + file.getHash());
        return null;
      } else {
        if (location == null) {
          return FileUtils.getFileAsByteArray(indexed.getJarFile());
        } else {
          logger.log(Level.SEVERE, "Cannot get a fragment of a jar file");
          return null;
        }
      }
    } else if (file.getType() == File.SOURCE) {
      if (project.getType() == Project.CRAWLED) {
        IJavaFile javaFile = repo.getFile(file.getPath());
        if (javaFile == null) {
          logger.log(Level.SEVERE, "Unable to find " + file.getPath() + " for " + file);
          return null;
        } else {
          if (location == null) {
            return FileUtils.getFileAsByteArray(javaFile.getFile());
          } else {
            return FileUtils.getFileFragmentAsByteArray(javaFile.getFile(), location.getOffset(), location.getLength());
          }
        }
      } else {
        java.io.File sourceFile = null;
        if (project.getType() == Project.JAR || project.getType() == Project.MAVEN) {
          JarIndex index = repo.getJarIndex();
          IndexedJar indexed = index.getIndexedJar(project.getHash());
          if (indexed == null) {
            logger.log(Level.SEVERE, "Unable to find " + project + " for class " + file + " with hash " + project.getHash());
            return null;
          } else {
            sourceFile = indexed.getSourceFile();
            if (sourceFile == null) {
              IndexedJar source = index.getPossibleSourceMatch(indexed);
              if (source == null) {
                sourceFile = indexed.getJarFile();
              } else {
                sourceFile = source.getJarFile();
              }
            }
          }
        } else if (project.getType() == Project.JAVA_LIBRARY) {
          sourceFile = extracted.getJavaLibrarySource(project.getPath());
        } else {
          logger.log(Level.SEVERE, project + " has improper type " + project.getType() + " for looking up source files");
          return null;
        }
        if (sourceFile == null) {
          logger.log(Level.SEVERE, "Null source file for " + file + " in " + project);
          return null;
        } else if (!sourceFile.exists()) {
          logger.log(Level.SEVERE, "Missing source file for " + file + " in " + project);
          return null;
        } else {
          ZipFile zip = null;
          try {
            zip = new ZipFile(sourceFile);
            String minusClass = file.getPath().substring(0, file.getPath().lastIndexOf('.'));
            String entryName = minusClass.replace('.', '/') + ".java";
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
              logger.log(Level.SEVERE, "Unable to find entry " + entryName + " in " + sourceFile.getName() + " for " + file + " and " + project);
              return null;
            } else {
              if (location == null) {
                return FileUtils.getInputStreamAsByteArray(zip.getInputStream(entry), (int)entry.getSize());
              } else {
                return FileUtils.getInputStreamFragmentAsByteArray(zip.getInputStream(entry), location.getOffset(), location.getLength());
              }
            }
          } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to read jar file", e);
            return null;
          } finally {
            FileUtils.close(zip);
          }
        }
      }
    } else {
      logger.log(Level.SEVERE, file + " from " + project + " is a class file with no corresponding source");
      return null;
    }
  }
  
  public static void testConsole() {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      while (true) {
        System.out.println(
            "\nPlease enter type of item to lookup\n" +
        		"Project  (p)\n" +
        		"File     (f)\n" +
        		"Entity   (e)\n" +
        		"Relation (r)\n" +
        		"Comment  (c)");
        System.out.print(":>");
        String input = reader.readLine();
        if (input.equals("p") || input.equals("f") || input.equals("e") || input.equals("r") || input.equals("c")) {
          System.out.println("Please enter the id number");
          System.out.print(":>");
          String id = reader.readLine();
          if (input.equals("p")) {
            byte[] result = lookupByProjectID(id);
            if (result == null) {
              System.out.println("Unable to find project");
            } else {
              System.out.println("Found project with " + result.length + " bytes");
            }
          } else if (input.equals("f")) {
            byte[] result = lookupByFileID(id);
            if (result == null) {
              System.out.println("Unable to find file");
            } else {
              System.out.println(new String(result));
              System.out.println("Found file with " + result.length + " bytes");
            }
          } else if (input.equals("e")) {
            byte[] result = lookupByEntityID(id);
            if (result == null) {
              System.out.println("Unable to find entity");
            } else {
              System.out.println(new String(result));
              System.out.println("Found entity with " + result.length + " bytes");
            }
          } else if (input.equals("r")) {
            byte[] result = lookupByRelationID(id);
            if (result == null) {
              System.out.println("Unable to find relation");
            } else {
              System.out.println(new String(result));
              System.out.println("Found relation with " + result.length + " bytes");
            }
          } else if (input.equals("c")) {
            byte[] result = lookupByCommentID(id);
            if (result == null) {
              System.out.println("Unable to find comment");
            } else {
              System.out.println(new String(result));
              System.out.println("Found comment with " + result.length + " bytes");
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private static class FileDatabaseAccessor extends DatabaseAccessor {
    protected FileDatabaseAccessor(DatabaseConnection connection) {
      super(connection);
    }
    
    public synchronized LocationDB getLocationByEntityID(String entityID) {
      return entitiesTable.getLocationByEntityID(entityID);
    }
    
    public synchronized LocationDB getLocationByRelationID(String relationID) {
      return relationsTable.getLocationByRelationID(relationID);
    }
    
    public synchronized LocationDB getLocationByCommentID(String commentID) {
      return commentsTable.getLocationByCommentID(commentID);
    }
    
    public synchronized FileDB getFileByFileID(String fileID) {
      return filesTable.getFileByFileID(fileID);
    }
    
    public synchronized ProjectDB getProjectByProjectID(String projectID) {
      return projectsTable.getProjectByProjectID(projectID);
    }
  }
}
