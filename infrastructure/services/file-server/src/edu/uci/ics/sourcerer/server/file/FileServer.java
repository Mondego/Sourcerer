package edu.uci.ics.sourcerer.server.file;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
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
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.server.ServletUtils;

@SuppressWarnings("serial")
public class FileServer extends HttpServlet {
  private TimeoutManager<DatabaseConnection> connectionManager;
  private Repository repo;
  private ExtractedRepository extracted;
  
  @Override
  public void init() throws ServletException {
    PropertyManager.PROPERTIES_STREAM.setValue(getServletContext().getResourceAsStream("/WEB-INF/lib/file-server.properties"));
    PropertyManager.REQUIRE_REGISTERED.setValue(false);
    PropertyManager.initializeProperties();
    
    if (!PropertyManager.registerAndVerify(DatabaseConnection.DATABASE_URL, DatabaseConnection.DATABASE_USER, DatabaseConnection.DATABASE_PASSWORD, AbstractRepository.INPUT_REPO)) {
      logger.log(Level.INFO, "Initializing connection manager");
      connectionManager = new TimeoutManager<DatabaseConnection>(new TimeoutManager.Instantiator<DatabaseConnection>() {
        @Override
        public DatabaseConnection create() {
          DatabaseConnection conn = new DatabaseConnection();
          conn.open();
          return conn;
        }
      }, 10 * 60 * 1000);
      
      repo = Repository.getRepository(AbstractRepository.INPUT_REPO.getValue());
      extracted = ExtractedRepository.getRepository(AbstractRepository.OUTPUT_REPO.getValue());
    } else {
      logger.log(Level.SEVERE, "Unable to initialize connection");
      connectionManager = null;
    }
  }
  
  @Override
  public void destroy() {
    logger.log(Level.INFO, "Destroying");
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (connectionManager == null) {
      ServletUtils.writeErrorMsg(response, "Database connection not initialized properly");
      return;
    }
    
    FileServerDatabaseAccessor db = new FileServerDatabaseAccessor(connectionManager.get());
    
    // Should the files download or be shown in browser?
    boolean download = "t".equals(request.getParameter("dl"));
    
    // Lookup by projectID
    {
      String projectID = request.getParameter("projectID");
      if (projectID != null) {
        ProjectDB project = db.getProjectByProjectID(projectID);
        if (project == null) {
          ServletUtils.writeErrorMsg(response, "Unable to find project " + projectID);
        } else {
          if (project.getType() == Project.SYSTEM) {
            ServletUtils.writeErrorMsg(response, projectID + " is a SYSTEM project");
          } else if (project.getType() == Project.JAR || project.getType() == Project.MAVEN) {
            JarIndex index = repo.getJarIndex();
            IndexedJar indexed = index.getIndexedJar(project.getHash());
            if (indexed == null) {
              ServletUtils.writeErrorMsg(response, "Unable to find project " + projectID + " with hash " + project.getHash());
            } else {
              ServletUtils.writeFile(response, project.getName(), indexed.getJarFile());
            }
          } else if (project.getType() == Project.JAVA_LIBRARY) {
            ServletUtils.writeFile(response, project.getName(), extracted.getJavaLibrary(project.getPath()));
          } else if (project.getType() == Project.CRAWLED) {
            ServletUtils.writeErrorMsg(response, "Source project exploration not yet supported");
          }
        }
        // Cleanup
        db.close();
        return;
      }
    }
    
    // Lookup by fileID
    {
      String fileID = request.getParameter("fileID");
      if (fileID != null) {
        FileDB file = db.getFileByFileID(fileID);
        if (file == null) {
          ServletUtils.writeErrorMsg(response, "Unable to find file " + fileID);
        } else {
          ProjectDB project = null;
//          if (file.getType() != edu.uci.ics.sourcerer.model.File.SOURCE) {
            project = db.getProjectByProjectID(file.getProjectID());
//          }
          writeFile(response, project, file, null, download);
        }
        // Cleanup
        db.close();
        return;
      }
    }
    
    // Lookup by entityID
    {
      String entityID = request.getParameter("entityID");
      if (entityID != null) {
        LocationDB loc = db.getLocationByEntityID(entityID);
        if (loc == null) {
          ServletUtils.writeErrorMsg(response, "Entity " + entityID + " has no associated file");
        } else {
          FileDB file = db.getFileByFileID(loc.getFileID());
          if (file == null) {
            ServletUtils.writeErrorMsg(response, "Unable to find file " + loc.getFileID());
          } else {
            ProjectDB project = null;
//            if (file.getType() != edu.uci.ics.sourcerer.model.File.SOURCE) {
              project = db.getProjectByProjectID(file.getProjectID());
//            }
            writeFile(response, project, file, loc, download);
          }
        }
        // Cleanup
        db.close();
        return;
      }
    }
    
    // Lookup by relationID
    {
      String relationID = request.getParameter("relationID");
      if (relationID != null) {
        LocationDB loc = db.getLocationByRelationID(relationID);
        if (loc == null) {
          ServletUtils.writeErrorMsg(response, "Relation " + relationID + " has no associated file");
        } else {
          FileDB file = db.getFileByFileID(loc.getFileID());
          if (file == null) {
            ServletUtils.writeErrorMsg(response, "Unable to find file " + loc.getFileID());
          } else {
            ProjectDB project = null;
//            if (file.getType() != edu.uci.ics.sourcerer.model.File.SOURCE) {
              project = db.getProjectByProjectID(file.getProjectID());
//            }
            writeFile(response, project, file, loc, download);
          }
        }
        // Cleanup
        db.close();
        return;
      }
    }
    
    {
      // Lookup by commentID
      {
        String commentID = request.getParameter("commentID");
        if (commentID != null) {
          LocationDB loc = db.getLocationByCommentID(commentID);
          if (loc == null) {
            ServletUtils.writeErrorMsg(response, "Comment " + commentID + " has no associated file");
          } else {
            FileDB file = db.getFileByFileID(loc.getFileID());
            if (file == null) {
              ServletUtils.writeErrorMsg(response, "Unable to find file " + loc.getFileID());
            } else {
              ProjectDB project = null;
//              if (file.getType() != edu.uci.ics.sourcerer.model.File.SOURCE) {
                project = db.getProjectByProjectID(file.getProjectID());
//              }
              writeFile(response, project, file, loc, download);
            }
          }
          // Cleanup
          db.close();
          return;
        }
      }
    }
      
    ServletUtils.writeErrorMsg(response, "Invalid action");
  }
  
  private void writeFile(HttpServletResponse response, ProjectDB project, FileDB file, LocationDB location, boolean download) throws IOException {
    if (project.getType() == Project.CRAWLED) {
      IJavaFile javaFile = repo.getFile(file.getPath());
      if (javaFile == null) {
        ServletUtils.writeErrorMsg(response, "Unable to find " + file.getPath() + " for file " + file.getFileID());
      } else {
        String name = null;
        if (download) {
          name = file.getName();
        }
        if (location == null) {
          ServletUtils.writeFile(response, name, javaFile.getFile());
        } else {
          ServletUtils.writeFileFragment(response, name, javaFile.getFile(), location.getOffset(), location.getLength());
        }
      }
    } else if (file.getType() == edu.uci.ics.sourcerer.model.File.JAR && project.getType() == Project.JAR) {
      JarIndex index = repo.getJarIndex();
      IndexedJar indexed = index.getIndexedJar(file.getHash());
      if (indexed == null) {
        ServletUtils.writeErrorMsg(response, "Unable to find file " + file.getFileID() + " with hash " + file.getHash());
      } else {
        if (location == null) {
          ServletUtils.writeFile(response, file.getName(), indexed.getJarFile());
        } else {
          ServletUtils.writeErrorMsg(response, "Cannot write a fragment of a jar file");
        }
      }
    } else if (file.getType() == edu.uci.ics.sourcerer.model.File.SOURCE && (project.getType() == Project.JAR || project.getType() == Project.MAVEN || project.getType() == Project.JAVA_LIBRARY)) {
      File sourceFile = null;
      if (project.getType() == Project.JAR) {
        JarIndex index = repo.getJarIndex();
        IndexedJar indexed = index.getIndexedJar(project.getHash());
        if (indexed == null) {
          ServletUtils.writeErrorMsg(response, "Unable to find project " + project.getProjectID() + " for class file " + file.getFileID() + " with hash " + project.getHash());
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
      }
      if (sourceFile == null) {
        ServletUtils.writeErrorMsg(response, "Null source file " + file.getFileID() + "(" + file.getType() +") in project " + project.getProjectID() + "(" + project.getType() + ")");
      } else if (!sourceFile.exists()) {
        ServletUtils.writeErrorMsg(response, "Missing source file " + file.getFileID() + "(" + file.getType() +") in project " + project.getProjectID() + "(" + project.getType() + ")");
      } else {
        ZipFile zip = null;
        try {
          zip = new ZipFile(sourceFile);
          String minusClass = file.getPath().substring(0, file.getPath().lastIndexOf('.'));
          String entryName = minusClass.replace('.', '/') + ".java";
          ZipEntry entry = zip.getEntry(entryName);
          if (entry == null) {
            ServletUtils.writeErrorMsg(response, "Unable to find entry " + entryName + " in " + sourceFile.getName());
          } else {
            String name = null;
            if (download) {
              name = file.getName();
            }
            if (location == null) {
              ServletUtils.writeInputStream(response, name, zip.getInputStream(entry));
            } else {
              ServletUtils.writeInputStreamFragment(response, name, zip.getInputStream(entry), location.getOffset(), location.getLength());
            }
          }
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Unable to read jar file", e);
        } finally {
          FileUtils.close(zip);
        }
      }
    } else {
      ServletUtils.writeErrorMsg(response, "File " + file.getFileID() + "(" + file.getType() +") has an unexpected file type or project type " + project.getProjectID() + "(" + project.getType() + ")");
    }
  }
}
