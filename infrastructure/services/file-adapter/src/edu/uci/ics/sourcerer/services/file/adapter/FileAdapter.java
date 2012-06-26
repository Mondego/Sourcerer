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
package edu.uci.ics.sourcerer.services.file.adapter;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.uci.ics.sourcerer.tools.core.repo.model.ContentFile;
import edu.uci.ics.sourcerer.tools.core.repo.model.RepoFile;
import edu.uci.ics.sourcerer.tools.java.db.schema.CommentsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFileSet;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.TimeoutManager;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnection;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileAdapter {
  private static TimeoutManager<FileDatabaseAccessor> accessorManager = 
      new TimeoutManager<FileDatabaseAccessor>(new TimeoutManager.Instantiator<FileDatabaseAccessor>() {
        @Override
        public FileDatabaseAccessor create() {
          return new FileDatabaseAccessor();
        }
      }, 10 * 60 * 1000);
  
  private static class FileDatabaseAccessor implements Closeable {
    private DatabaseConnection conn;
    
    private FileDatabaseAccessor() {
      conn = DatabaseConnectionFactory.INSTANCE.create();
      conn.open();
    }
    
    private SelectQuery selectByProjectID = null;
    private ConstantCondition<Integer> equalsProjectID = null;
    public TypedQueryResult selectByProjectID(Integer projectID) {
      if (selectByProjectID == null) {
        selectByProjectID = conn.getExecutor().createSelectQuery(ProjectsTable.TABLE);
        selectByProjectID.addSelect(ProjectsTable.PROJECT_TYPE, ProjectsTable.HASH, ProjectsTable.PATH);
        equalsProjectID = ProjectsTable.PROJECT_ID.compareEquals();
        selectByProjectID.andWhere(equalsProjectID);
      }
      equalsProjectID.setValue(projectID);
      return selectByProjectID.select();
    }
    
    private SelectQuery selectByFileID = null;
    private ConstantCondition<Integer> equalsFileID = null;
    public TypedQueryResult selectByFileID(Integer fileID) {
      if (selectByFileID == null) {
        selectByFileID = conn.getExecutor().createSelectQuery(FilesTable.TABLE);
        selectByFileID.addSelect(FilesTable.FILE_TYPE, FilesTable.HASH, FilesTable.PATH, FilesTable.PROJECT_ID);
        equalsFileID = FilesTable.FILE_ID.compareEquals();
        selectByFileID.andWhere(equalsFileID);
      }
      equalsFileID.setValue(fileID);
      return selectByFileID.select();
    }
    
    private SelectQuery selectByEntityID = null;
    private ConstantCondition<Integer> equalsEntityID = null;
    public TypedQueryResult selectByEntityID(Integer entityID) {
      if (selectByEntityID == null) {
        selectByEntityID = conn.getExecutor().createSelectQuery(EntitiesTable.TABLE);
        selectByEntityID.addSelect(EntitiesTable.PROJECT_ID, EntitiesTable.FILE_ID, EntitiesTable.OFFSET, EntitiesTable.LENGTH);
        equalsEntityID = EntitiesTable.ENTITY_ID.compareEquals();
        selectByEntityID.andWhere(equalsEntityID);
      }
      equalsEntityID.setValue(entityID);
      return selectByEntityID.select();
    }
    
    private SelectQuery selectByRelationID = null;
    private ConstantCondition<Integer> equalsRelationID = null;
    public TypedQueryResult selectByRelationID(Integer relationID) {
      if (selectByRelationID == null) {
        selectByRelationID = conn.getExecutor().createSelectQuery(RelationsTable.TABLE);
        selectByRelationID.addSelect(RelationsTable.PROJECT_ID, RelationsTable.FILE_ID, RelationsTable.OFFSET, RelationsTable.LENGTH);
        equalsRelationID = RelationsTable.RELATION_ID.compareEquals();
        selectByRelationID.andWhere(equalsRelationID);
      }
      equalsRelationID.setValue(relationID);
      return selectByRelationID.select();
    }
    
    private SelectQuery selectByCommentID = null;
    private ConstantCondition<Integer> equalsCommentID = null;
    public TypedQueryResult selectByCommentID(Integer commentID) {
      if (selectByCommentID == null) {
        selectByCommentID = conn.getExecutor().createSelectQuery(CommentsTable.TABLE);
        selectByCommentID.addSelect(CommentsTable.PROJECT_ID, CommentsTable.FILE_ID, CommentsTable.OFFSET, CommentsTable.LENGTH);
        equalsCommentID = CommentsTable.COMMENT_ID.compareEquals();
        selectByCommentID.andWhere(equalsRelationID);
      }
      equalsCommentID.setValue(commentID);
      return selectByCommentID.select();
    }
    
    private SelectQuery selectImportLinks = null;
    private ConstantCondition<Integer> importFileID = null;
    public TypedQueryResult selectImportLinks(Integer fileID) {
      if (selectImportLinks == null) {
        selectImportLinks = conn.getExecutor().createSelectQuery(ImportsTable.TABLE);
        selectImportLinks.addSelect(ImportsTable.EID, ImportsTable.OFFSET, ImportsTable.LENGTH);
        importFileID = ImportsTable.FILE_ID.compareEquals();
        selectImportLinks.andWhere(importFileID);
      }
      importFileID.setValue(fileID);
      return selectImportLinks.select();
    }
    
    private SelectQuery selectFields = null;
    private ConstantCondition<Integer> fieldFileID = null;
    public TypedQueryResult selectFields(Integer fileID) {
      if (selectFields == null) {
        selectFields = conn.getExecutor().createSelectQuery(EntitiesTable.TABLE);
        selectFields.addSelect(EntitiesTable.OFFSET, EntitiesTable.LENGTH);
        fieldFileID = EntitiesTable.FILE_ID.compareEquals();
        selectFields.andWhere(fieldFileID.and(EntitiesTable.ENTITY_TYPE.compareEquals(Entity.FIELD)));
      }
      fieldFileID.setValue(fileID);
      return selectFields.select();
    }
    
    private SelectQuery selectRelationLinks = null;
    private ConstantCondition<Integer> relationFileID = null;
    public TypedQueryResult selectRelationLinks(Integer fileID) {
      if (selectRelationLinks == null) {
        selectRelationLinks = conn.getExecutor().createSelectQuery(RelationsTable.RHS_EID.compareEquals(EntitiesTable.ENTITY_ID));
        selectRelationLinks.addSelect(RelationsTable.RELATION_TYPE, RelationsTable.OFFSET, RelationsTable.LENGTH, EntitiesTable.ENTITY_ID, EntitiesTable.FQN);
        relationFileID = RelationsTable.FILE_ID.compareEquals();
        selectRelationLinks.andWhere(relationFileID.and(RelationsTable.RELATION_TYPE.compareIn(EnumSet.of(Relation.USES, Relation.READS, Relation.WRITES, Relation.CALLS))));
      }
      relationFileID.setValue(fileID);
      return selectRelationLinks.select();
    }
    
    public void close() {
      IOUtils.close(conn);
    }
  }
  
  private static JavaRepository repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JavaRepositoryFactory.INPUT_REPO);
  
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

    TypedQueryResult result = db.selectByProjectID(projectID);
    if (result.next()) {
      Project type = result.getResult(ProjectsTable.PROJECT_TYPE);
      if (type == Project.SYSTEM) {
        return new Result(projectID + " is a SYSTEM project");
      } else if (type == Project.JAR || type == Project.MAVEN || type == Project.JAVA_LIBRARY) {
        return getJarFile(projectID, null, result.getResult(ProjectsTable.HASH));
      } else if (type == Project.CRAWLED) {
        return new Result("Crawled projects not supported: " + projectID);
      } else {
        return new Result("Unknown project type: " + type + " for " + projectID);
      }
    } else {
      return new Result("Unable to find project: " + projectID);
    }
  }
  
  public static byte[] lookupByFileID(Integer fileID) {
    return convertResult(lookupResultByFileID(fileID));
  }
  
  public static Result lookupResultByFileID(Integer fileID) { 
    FileDatabaseAccessor db = accessorManager.get();
    
    TypedQueryResult result = db.selectByFileID(fileID);
    if (result.next()) {
      File type = result.getResult(FilesTable.FILE_TYPE);
      if (type == File.JAR) {
        return getJarFile(null, fileID, result.getResult(FilesTable.HASH));
      } else if (type == File.SOURCE) {
        return getSourceFile(db, result.getResult(FilesTable.PROJECT_ID), fileID, result.getResult(FilesTable.PATH), null, null);
      } else {
        return new Result("file " + fileID + " is a class file with no corresponding source");
      }
    } else {
      return new Result("Unable to find file: " + fileID);
    }
  }
  
  public static byte[] lookupByEntityID(Integer entityID) {
    return convertResult(lookupResultByEntityID(entityID));
  }
  
  public static Result lookupResultByEntityID(Integer entityID) {
    FileDatabaseAccessor db = accessorManager.get();
    
    TypedQueryResult result = db.selectByEntityID(entityID);
    if (result.next()) {
      Integer fileID = result.getResult(EntitiesTable.FILE_ID);
      if (fileID == null) {
        return new Result("Entity " + entityID + " has no associated file"); 
      } else {
        TypedQueryResult fileInfo = db.selectByFileID(fileID);
        if (fileInfo.next()) {
          return getSourceFile(db, result.getResult(EntitiesTable.PROJECT_ID), fileID, fileInfo.getResult(FilesTable.PATH), result.getResult(EntitiesTable.OFFSET), result.getResult(EntitiesTable.LENGTH));
        } else {
          return new Result("File " + fileID + " does not exist for entity " + entityID);
        }
      }
    } else {
      return new Result("Entity " + entityID + " does not exist");
    }
  }
  
  public static byte[] lookupByRelationID(Integer relationID) {
    return convertResult(lookupResultByRelationID(relationID));
  }
  
  public static Result lookupResultByRelationID(Integer relationID) {
    FileDatabaseAccessor db = accessorManager.get();
    
    TypedQueryResult result = db.selectByRelationID(relationID);
    if (result.next()) {
      Integer fileID = result.getResult(RelationsTable.FILE_ID);
      if (fileID == null) {
        return new Result("Relation " + relationID + " has no associated file"); 
      } else {
        TypedQueryResult fileInfo = db.selectByFileID(fileID);
        return getSourceFile(db, result.getResult(RelationsTable.PROJECT_ID), fileID, fileInfo.getResult(FilesTable.PATH), result.getResult(RelationsTable.OFFSET), result.getResult(RelationsTable.LENGTH));
      }
    } else {
      return new Result("Relation " + relationID + " does not exist");
    }
  }
  
  public static byte[] lookupByCommentID(Integer commentID) {
    return convertResult(lookupResultByCommentID(commentID));
  }
  
  public static Result lookupResultByCommentID(Integer commentID) {
    FileDatabaseAccessor db = accessorManager.get();
    
    TypedQueryResult result = db.selectByCommentID(commentID);
    if (result.next()) {
      Integer fileID = result.getResult(RelationsTable.FILE_ID);
      if (fileID == null) {
        return new Result("Comment " + commentID + " has no associated file"); 
      } else {
        TypedQueryResult fileInfo = db.selectByFileID(fileID);
        return getSourceFile(db, result.getResult(CommentsTable.PROJECT_ID), fileID, fileInfo.getResult(FilesTable.PATH), result.getResult(CommentsTable.OFFSET), result.getResult(CommentsTable.LENGTH));
      }
    } else {
      return new Result("Comment " + commentID + " does not exist");
    }
  }
  
  private static Result getJarFile(Integer projectID, Integer fileID, String hash) {
    JarFile jar = repo.getJarFile(hash);
    if (jar == null) {
      if (fileID == null) {
        return new Result("Unable to find project " + projectID + " with hash " + hash);
      } else {
        return new Result("Unable to find file " + projectID + " with hash " + hash);
      }
    } else {
      return new Result(jar.getProperties().NAME.getValue(), fileID, FileUtils.getFileAsByteArray(jar.getFile().toFile()));
    }
  }
  
  private static Result getSourceFile(FileDatabaseAccessor db, Integer projectID, Integer fileID, String path, Integer offset, Integer length) {
    TypedQueryResult projectInfo = db.selectByProjectID(projectID);
    if (projectInfo.next()) {
      Project type = projectInfo.getResult(ProjectsTable.PROJECT_TYPE);
      if (type == Project.CRAWLED) {
        String projectPath = projectInfo.getResult(ProjectsTable.PATH);
        JavaProject project = repo.getProject(projectPath);
        if (project == null) {
          return new Result("Unable to find project path " + projectPath + " for project " + projectID);
        } else {
          JavaFileSet files = project.getContent();
          ContentFile file = files.getFile(path);
          byte[] contents = FileUtils.getFileAsByteArray(file.getFile().toFile());
          if (contents == null) {
              return new Result("Unable to find " + path + " for " + fileID);
          } else {
            if (offset == null) {
              return new Result(file.getFile().getName(), fileID, contents);
            } else {
              String name = file.getFile().getName();
              name = name.substring(0, name.indexOf('.')) + "-" + offset + "-" + length + ".java";
              return new Result(name, fileID, contents, offset, length);
            }
          }
        }
      } else if (type == Project.JAR || type == Project.JAVA_LIBRARY || type == Project.MAVEN) {
        String hash = projectInfo.getResult(ProjectsTable.HASH);
        JarFile jar = repo.getJarFile(hash);
        if (jar == null) {
          return new Result("Unable to find project " + projectID + " for class file " + fileID + " with hash " + hash);
        } else {
          RepoFile file = null;
          if (jar.getSourceFile().exists()) {
            file = jar.getSourceFile();
          } else {
            file = jar.getFile();
          }
          try (ZipFile zip = new ZipFile(file.toFile())) {
            String minusClass = path.substring(0, path.lastIndexOf('.'));
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
              return new Result("Unable to find entry " + entryName + " in " + jar + " for file " + fileID + " and project " + projectID);
            } else {
              if (offset == null) {
                return new Result(entry.getName(), fileID, IOUtils.getInputStreamAsByteArray(zip.getInputStream(entry), (int)entry.getSize()));
              } else {
                String name = entry.getName();
                name = name.substring(0, name.lastIndexOf('.')) + "-" + offset + "-" + length + ".java";
                return new Result(name, fileID, IOUtils.getInputStreamAsByteArray(zip.getInputStream(entry), (int)entry.getSize()), offset, length);
              }
            }
          } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to read jar file", e);
            return new Result("Unable to read jar file");
          }
        }
      } else {
        return new Result("Invalid project type: " + type + " for project " + projectID + " and file " + fileID);
      }
    } else {
      return new Result("No match for project " + projectID + " and file " + fileID);
    }
  }
  
  public static Collection<Link> getImportsByFileID(Integer fileID) {
    FileDatabaseAccessor db = accessorManager.get();
    
    ArrayList<Link> links = new ArrayList<>();
    TypedQueryResult result = db.selectImportLinks(fileID);
    while (result.next()) {
      links.add(new Link(result.getResult(ImportsTable.EID), result.getResult(ImportsTable.OFFSET), result.getResult(ImportsTable.LENGTH)));
    }
    return links;
  }
  
  public static Collection<Link> getFieldsByFileID(Integer fileID) {
    FileDatabaseAccessor db = accessorManager.get();
    
    ArrayList<Link> links = new ArrayList<>();
    TypedQueryResult result = db.selectFields(fileID);
    while (result.next()) {
      links.add(new Link(result.getResult(EntitiesTable.OFFSET), result.getResult(EntitiesTable.LENGTH)));
    }
    return links;
  }
  
  public static Collection<Link> getRelationLinksByFileID(Integer fileID) {
    FileDatabaseAccessor db = accessorManager.get();
    
    ArrayList<Link> links = new ArrayList<>();
    TypedQueryResult result = db.selectRelationLinks(fileID);
    while (result.next()) {
      links.add(new Link(result.getResult(EntitiesTable.ENTITY_ID), result.getResult(EntitiesTable.FQN), result.getResult(RelationsTable.OFFSET), result.getResult(RelationsTable.LENGTH), result.getResult(RelationsTable.RELATION_TYPE)));
    }
    return links;
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
    private byte[] fullResult;
    private int offset;
    private int length;
    private Integer fileID;
    private String name;
    private byte[] result;
    private String errorMessage;
    
    private Result(String name, Integer fileID, byte[] fullResult) {
      this(name, fileID, fullResult, -1, 0);
    }
    
    private Result(String name, Integer fileID, byte[] fullResult, int offset, int length) {
      this.name = name;
      this.fileID = fileID;
      this.fullResult = fullResult;
      this.offset = offset;
      this.length = length;
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
    
    public Integer getFileID() {
      return fileID;
    }
    
    public byte[] getResult() {
      if (offset == -1) {
        return fullResult;
      } else if (result == null) {
        result = new byte[length];
        System.arraycopy(fullResult, offset, result, 0, length);
      }
      return result;
    }
    
    public byte[] getFullResult() {
      return fullResult;
    }
    
    public Integer getOffset() {
      return offset;
    }
    
    public int getLength() {
      return length;
    }
    
    public String getErrorMessage() {
      return errorMessage;
    }
  }
  
  public static class Link {
    private Integer entityID;
    private String fqn;
    private Integer offset;
    private Integer length;
    private Relation type;
    
    private Link(Integer offset, Integer length) {
      this.offset = offset;
      this.length = length;
    }
    
    private Link(Integer entityID, Integer offset, Integer length) {
      this.entityID = entityID;
      this.offset = offset;
      this.length = length;
    }
    
    private Link(Integer entityID, String fqn, Integer offset, Integer length, Relation type) {
      this.entityID = entityID;
      this.fqn = fqn;
      this.offset = offset;
      this.length = length;
      this.type = type;
    }
    
    public Integer getEntityID() {
      return entityID;
    }
    
    public String getFqn() {
      return fqn;
    }
    
    public Integer getOffset() {
      return offset;
    }
    
    public Integer getLength() {
      return length;
    }
    
    public Relation getType() {
      return type;
    }
  }
}
