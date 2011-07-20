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
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.uci.ics.sourcerer.db.queries.DatabaseAccessor;
import edu.uci.ics.sourcerer.model.File;
import edu.uci.ics.sourcerer.model.Project;
import edu.uci.ics.sourcerer.model.db.FileDB;
import edu.uci.ics.sourcerer.model.db.LocationDB;
import edu.uci.ics.sourcerer.model.db.LargeProjectDB;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
import edu.uci.ics.sourcerer.repo.general.IndexedJar;
import edu.uci.ics.sourcerer.repo.general.JarIndex;
import edu.uci.ics.sourcerer.util.TimeoutManager;
import edu.uci.ics.sourcerer.util.db.DatabaseConnection;
import edu.uci.ics.sourcerer.util.io.internal.FileUtils;

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
  
  private static byte[] convertResult(Result result) {
    if (result.success()) {
      return result.getResult();
    } else {
      logger.log(Level.SEVERE, result.getErrorMessage());
      return null;
    }
  }
  public static byte[] lookupByProjectID(Integer projectID) {
    return convertResult(lookupResultByProjectID(projectID));
  }
  
  public static Result lookupResultByProjectID(Integer projectID) {
    FileDatabaseAccessor db = accessorManager.get();
    LargeProjectDB project = db.getProjectByProjectID(projectID);
    if (project == null) {
      return new Result("Unable to find project: " + projectID);
    } else {
      if (project.getType() == Project.SYSTEM) {
        return new Result(project + " is a SYSTEM project");
      } else if (project.getType() == Project.JAR || project.getType() == Project.MAVEN) {
        JarIndex index = repo.getJarIndex();
        IndexedJar indexed = index.getIndexedJar(project.getHash());
        if (indexed == null) {
          return new Result("Unable to find " + project + " with hash " + project.getHash());
        } else {
          return new Result(indexed.getName(), FileUtils.getFileAsByteArray(indexed.getJarFile()));
        }
      } else if (project.getType() == Project.JAVA_LIBRARY) {
        return new Result(project.getName(), FileUtils.getFileAsByteArray(extracted.getJavaLibrary(project.getPath())));
      } else if (project.getType() == Project.CRAWLED) {
        return new Result("Crawled projects not supported: " + project);
      } else {
        return new Result("Unknown project type: " + project.getType());
      }
    }
  }
  
  public static byte[] lookupByFileID(Integer fileID) {
    return convertResult(lookupResultByFileID(fileID));
  }
  
  public static Result lookupResultByFileID(Integer fileID) { 
    FileDatabaseAccessor db = accessorManager.get();
    FileDB file = db.getFileByFileID(fileID);
    if (file == null) {
      return new Result("Unable to find file: " + fileID);
    } else {
      LargeProjectDB project = db.getProjectByProjectID(file.getProjectID());
      return getFile(project, file, null);
    }
  }
  
  public static byte[] lookupByEntityID(Integer entityID) {
    return convertResult(lookupResultByEntityID(entityID));
  }
  
  public static Result lookupResultByEntityID(Integer entityID) {
    FileDatabaseAccessor db = accessorManager.get();
    LocationDB loc = db.getLocationByEntityID(entityID);
    if (loc == null) {
      return new Result("Entity " + entityID + " has no associated file");
    } else {
      FileDB file = db.getFileByFileID(loc.getFileID());
      if (file == null) {
        return new Result("Unable to find file: " + loc.getFileID());
      } else {
        LargeProjectDB project = db.getProjectByProjectID(file.getProjectID());
        return getFile(project, file, loc);
      }
    }
  }
  
  public static byte[] lookupByRelationID(Integer relationID) {
    return convertResult(lookupResultByRelationID(relationID));
  }
  
  public static Result lookupResultByRelationID(Integer relationID) {
    FileDatabaseAccessor db = accessorManager.get();
    LocationDB loc = db.getLocationByRelationID(relationID);
    if (loc == null) {
      return new Result("Relation " + relationID + " has no associated file");
    } else {
      FileDB file = db.getFileByFileID(loc.getFileID());
      if (file == null) {
        return new Result("Unable to find file: " + loc.getFileID());
      } else {
        LargeProjectDB project = db.getProjectByProjectID(file.getProjectID());
        return getFile(project, file, loc);
      }
    }
  }
  
  public static byte[] lookupByCommentID(Integer commentID) {
    return convertResult(lookupResultByCommentID(commentID));
  }
  
  public static Result lookupResultByCommentID(Integer commentID) {
    FileDatabaseAccessor db = accessorManager.get();
    LocationDB loc = db.getLocationByCommentID(commentID);
    if (loc == null) {
      return new Result("Comment " + commentID + " has no associated file");
    } else {
      FileDB file = db.getFileByFileID(loc.getFileID());
      if (file == null) {
        return new Result("Unable to find file: " + loc.getFileID());
      } else {
        LargeProjectDB project = db.getProjectByProjectID(file.getProjectID());
        return getFile(project, file, loc);
      }
    }
  }
  
  private static Result getFile(LargeProjectDB project, FileDB file, LocationDB location) {
    if (file.getType() == File.JAR) {
      JarIndex index = repo.getJarIndex();
      IndexedJar indexed = index.getIndexedJar(file.getHash());
      if (indexed == null) {
        return new Result("Unable to find " + file + " with hash " + file.getHash());
      } else {
        if (location == null) {
          return new Result(indexed.getName(), FileUtils.getFileAsByteArray(indexed.getJarFile()));
        } else {
          return new Result("Cannot get a fragment of a jar file");
        }
      }
    } else if (file.getType() == File.SOURCE) {
      if (project.getType() == Project.CRAWLED) {
        byte[] javaFile = repo.getFile(project.getPath(), file.getPath());
        if (javaFile == null) {
          return new Result("Unable to find " + file.getPath() + " for " + file);
        } else {
          if (location == null) {
            return new Result(file.getName(), javaFile);
          } else {
            String name = file.getName();
            name = name.substring(0, name.indexOf('.')) + "-" + location.getOffset() + "-" + location.getLength() + ".java";
            byte[] fragment = new byte[location.getLength()];
            System.arraycopy(javaFile, location.getOffset(), fragment, 0, location.getLength());
            return new Result(name, fragment);
          }
        }
      } else {
        java.io.File sourceFile = null;
        if (project.getType() == Project.JAR || project.getType() == Project.MAVEN) {
          JarIndex index = repo.getJarIndex();
          IndexedJar indexed = index.getIndexedJar(project.getHash());
          if (indexed == null) {
            return new Result("Unable to find " + project + " for class " + file + " with hash " + project.getHash());
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
          return new Result(project + " has improper type " + project.getType() + " for looking up source files");
        }
        if (sourceFile == null) {
          return new Result("Null source file for " + file + " in " + project);
        } else if (!sourceFile.exists()) {
          return new Result("Missing source file for " + file + " in " + project);
        } else {
          ZipFile zip = null;
          try {
            zip = new ZipFile(sourceFile);
            String minusClass = file.getPath().substring(0, file.getPath().lastIndexOf('.'));
            String entryName = minusClass.replace('.', '/') + ".java";
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
              Enumeration<? extends ZipEntry> entries = zip.entries();
              for (ZipEntry possibleEntry = entries.nextElement(); entries.hasMoreElements(); possibleEntry = entries.nextElement()) {
                if (possibleEntry.getName().endsWith(entryName)) {
                  entry = possibleEntry;
                  break;
                }
              }
            }
            if (entry == null) {
              return new Result("Unable to find entry " + entryName + " in " + sourceFile.getName() + " for " + file + " and " + project);
            } else {
              if (location == null) {
                return new Result(entry.getName(), FileUtils.getInputStreamAsByteArray(zip.getInputStream(entry), (int)entry.getSize()));
              } else {
                String name = entry.getName();
                name = name.substring(0, name.lastIndexOf('.')) + "-" + location.getOffset() + "-" + location.getLength() + ".java";
                return new Result(name, FileUtils.getInputStreamFragmentAsByteArray(zip.getInputStream(entry), location.getOffset(), location.getLength()));
              }
            }
          } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to read jar file", e);
            return new Result("Unable to read jar file");
          } finally {
            FileUtils.close(zip);
          }
        }
      }
    } else {
      return new Result(file + " from " + project + " is a class file with no corresponding source");
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
          Integer id = null;
          while (id == null) {
            try {
              id = Integer.valueOf(reader.readLine());
            } catch (NumberFormatException e) {
              System.out.println("Please enter the id number");
              System.out.print(":>");
            }
          }
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
  
  public static class Result {
    private String name;
    private byte[] result;
    private String errorMessage;
    
    private Result(String name, byte[] result) {
      this.name = name;
      this.result = result;
    }
    
    private Result(String errorMessage) {
      this.errorMessage = errorMessage;
    }
    
    public boolean success() {
      return errorMessage == null;
    }
    
    public String getName() {
      return name;
    }
    
    public byte[] getResult() {
      return result;
    }
    
    public String getErrorMessage() {
      return errorMessage;
    }
  }
  
  private static class FileDatabaseAccessor extends DatabaseAccessor {
    protected FileDatabaseAccessor(DatabaseConnection connection) {
      super(connection);
    }
    
    public synchronized LocationDB getLocationByEntityID(Integer entityID) {
      return entityQueries.getLocationByEntityID(entityID);
    }
    
    public synchronized LocationDB getLocationByRelationID(Integer relationID) {
      return relationQueries.getLocationByRelationID(relationID);
    }
    
    public synchronized LocationDB getLocationByCommentID(Integer commentID) {
      return commentQueries.getLocationByCommentID(commentID);
    }
    
    public synchronized FileDB getFileByFileID(Integer fileID) {
      return fileQueries.getByFileID(fileID);
    }
    
    public synchronized LargeProjectDB getProjectByProjectID(Integer projectID) {
      return projectQueries.getLargeByProjectID(projectID);
    }
  }
}
