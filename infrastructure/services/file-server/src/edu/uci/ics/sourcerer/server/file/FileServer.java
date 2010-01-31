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
import edu.uci.ics.sourcerer.model.db.JarClassFileDB;
import edu.uci.ics.sourcerer.model.db.JarDB;
import edu.uci.ics.sourcerer.model.db.JarLocationDB;
import edu.uci.ics.sourcerer.model.db.LocationDB;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
import edu.uci.ics.sourcerer.repo.general.IndexedJar;
import edu.uci.ics.sourcerer.repo.general.JarIndex;
import edu.uci.ics.sourcerer.util.TimeoutManager;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.server.ServletUtils;

@SuppressWarnings("serial")
public class FileServer extends HttpServlet {
  private TimeoutManager<DatabaseConnection> connectionManager;
  private Repository repo;
  
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
    
    // Lookup by fileID
    {
      String fileID = request.getParameter("fileID");
      if (fileID != null) {
        String filePath = db.getFilePathByFileID(fileID);
        if (filePath == null) {
          ServletUtils.writeErrorMsg(response, "Unable to find path for file " + fileID);
        } else {
          IJavaFile file = repo.getFile(filePath);
          if (file == null) {
            ServletUtils.writeErrorMsg(response, "Unable to find " + filePath + " for file " + fileID);
          } else {
            String name = null;
            if (download) {
              name = filePath.substring(filePath.lastIndexOf('/') + 1);
              name = name.substring(0, name.indexOf(".java")) + "-f" + fileID + ".java";
            }
            ServletUtils.writeFile(response, name, file.getFile());
          }
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
          IJavaFile file = repo.getFile(loc.getPath());
          if (file == null) {
            ServletUtils.writeErrorMsg(response, "Unable to find " + loc.getPath() + " for entity " + entityID);
          } else {
            String name = null;
            if (download) {
              name = loc.getPath().substring(loc.getPath().lastIndexOf('/') + 1);
              name = name.substring(0, name.indexOf(".java")) + "-e" + entityID + ".java";
            }
            ServletUtils.writeFileFragment(response, name, file.getFile(), loc.getOffset(), loc.getLength());
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
          IJavaFile file = repo.getFile(loc.getPath());
          if (file == null) {
            ServletUtils.writeErrorMsg(response, "Unable to find " + loc.getPath() + " for relation " + relationID);
          } else {
            String name = null;
            if (download) {
              name = loc.getPath().substring(loc.getPath().lastIndexOf('/') + 1);
              name = name.substring(0, name.indexOf(".java")) + "-r" + relationID + ".java";
            }
            ServletUtils.writeFileFragment(response, name, file.getFile(), loc.getOffset(), loc.getLength());
          }
        }
        // Cleanup
        db.close();
        return;
      }
      
      // Lookup by commentID
      {
        String commentID = request.getParameter("commentID");
        if (commentID != null) {
          LocationDB loc = db.getLocationByCommentID(commentID);
          if (loc == null) {
            ServletUtils.writeErrorMsg(response, "Comment " + commentID + " has no associated file");
          } else {
            IJavaFile file = repo.getFile(loc.getPath());
            if (file == null) {
              ServletUtils.writeErrorMsg(response, "Unable to find " + loc.getPath() + " for comment " + commentID);
            } else {
              String name = null;
              if (download) {
                name = loc.getPath().substring(loc.getPath().lastIndexOf('/') + 1);
                name = name.substring(0, name.indexOf(".java")) + "-c" + commentID + ".java";
              }
              ServletUtils.writeFileFragment(response, name, file.getFile(), loc.getOffset(), loc.getLength());
            }
          }
          // Cleanup
          db.close();
          return;
        }
      }
      
      // Lookup by jarID
      {
        String jarID = request.getParameter("jarID");
        if (jarID != null) {
          JarDB jar = db.getJarByJarID(jarID);
          if (jar == null) {
            ServletUtils.writeErrorMsg(response, "Unable to find jar " + jarID);
          } else {
            JarIndex index = repo.getJarIndex();
            IndexedJar indexed = index.getIndexedJar(jar.getHash());
            if (indexed == null) {
              ServletUtils.writeErrorMsg(response, "Unable to find jar " + jarID + " with hash " + jar.getHash());
            } else {
              ServletUtils.writeFile(response, jar.getName(), indexed.getJarFile());
            }
          }
          // Cleanup
          db.close();
          return;
        }
      }
      
      // Lookup by jar class file
      {
        String jarClassFileID = request.getParameter("jarClassFileID");
        if (jarClassFileID != null) {
          JarClassFileDB classFile = db.getJarClassFileByFileID(jarClassFileID);
          if (classFile == null) {
            ServletUtils.writeErrorMsg(response, "Unable to find jar class file " + jarClassFileID);
          } else {
            // Get the jar
            JarIndex index = repo.getJarIndex();
            IndexedJar indexed = index.getIndexedJar(classFile.getHash());
            if (indexed == null) {
              ServletUtils.writeErrorMsg(response, "Unable to find jar " + classFile.getJarID() + " with hash " + classFile.getHash());
            } else {
              File sourceFile = indexed.getSourceFile();
              if (sourceFile == null) {
                IndexedJar source = index.getPossibleSourceMatch(indexed);
                if (source == null) {
                  sourceFile = indexed.getJarFile();
                } else {
                  sourceFile = source.getJarFile();
                }
              }
              try {
                ZipFile zip = new ZipFile(sourceFile);
                String minusClass = classFile.getPath().substring(0, classFile.getPath().lastIndexOf('.'));
                String entryName = minusClass.replace('.', '/') + ".java";
                ZipEntry entry = zip.getEntry(entryName);
                if (entry == null) {
                  ServletUtils.writeErrorMsg(response, "Unable to find entry " + entryName + " in " + sourceFile.getName());
                } else {
                  String name = null;
                  if (download) {
                    name = classFile.getPath().substring(minusClass.lastIndexOf('.') + 1) + "-jf" + jarClassFileID + ".java";
                  }
                  ServletUtils.writeInputStream(response, name, zip.getInputStream(entry));
                }
              } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable to read jar file", e);
              }
            }
          }
          return;
        }
      }
      
      // Lookup by jar entity id
      {
        String jarEntityID = request.getParameter("jarEntityID");
        if (jarEntityID != null) {
          JarLocationDB loc = db.getJarLocationByJarEntityID(jarEntityID);
          if (loc == null) {
            ServletUtils.writeErrorMsg(response, "Unable to find location for jar entity " + jarEntityID);
          } else {
            // Get the jar
            JarIndex index = repo.getJarIndex();
            IndexedJar indexed = index.getIndexedJar(loc.getHash());
            if (indexed == null) {
              ServletUtils.writeErrorMsg(response, "Unable to find jar " + loc.getJarID() + " with hash " + loc.getHash());
            } else {
              File sourceFile = indexed.getSourceFile();
              if (sourceFile == null) {
                IndexedJar source = index.getPossibleSourceMatch(indexed);
                if (source == null) {
                  sourceFile = indexed.getJarFile();
                } else {
                  sourceFile = source.getJarFile();
                }
              }
              try {
                ZipFile zip = new ZipFile(sourceFile);
                String minusClass = loc.getPath().substring(0, loc.getPath().lastIndexOf('.'));
                String entryName = minusClass.replace('.', '/') + ".java";
                ZipEntry entry = zip.getEntry(entryName);
                if (entry == null) {
                  entry = zip.getEntry("src/" + entryName);
                }
                if (entry == null) {
                  ServletUtils.writeErrorMsg(response, "Unable to find entry " + entryName + " in " + sourceFile.getName());
                } else {
                  String name = null;
                  if (download) {
                    name = loc.getPath().substring(minusClass.lastIndexOf('.') + 1) + "-je" + jarEntityID + ".java";
                  }
                  ServletUtils.writeInputStreamFragment(response, name, zip.getInputStream(entry), loc.getOffset(), loc.getLength());
                }
              } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable to read jar file", e);
              }
            }
          }
          return;
        }
      }
      
      // Lookup by jar relation id
      {
        String jarRelationID = request.getParameter("jarRelationID");
        if (jarRelationID != null) {
          JarLocationDB loc = db.getJarLocationByJarRelationID(jarRelationID);
          if (loc == null) {
            ServletUtils.writeErrorMsg(response, "Unable to find location for jar relation " + jarRelationID);
          } else {
            // Get the jar
            JarIndex index = repo.getJarIndex();
            IndexedJar indexed = index.getIndexedJar(loc.getHash());
            if (indexed == null) {
              ServletUtils.writeErrorMsg(response, "Unable to find jar " + loc.getJarID() + " with hash " + loc.getHash());
            } else {
              File sourceFile = indexed.getSourceFile();
              if (sourceFile == null) {
                IndexedJar source = index.getPossibleSourceMatch(indexed);
                if (source == null) {
                  sourceFile = indexed.getJarFile();
                } else {
                  sourceFile = source.getJarFile();
                }
              }
              try {
                ZipFile zip = new ZipFile(sourceFile);
                String minusClass = loc.getPath().substring(0, loc.getPath().lastIndexOf('.'));
                String entryName = minusClass.replace('.', '/') + ".java";
                ZipEntry entry = zip.getEntry(entryName);
                if (entry == null) {
                  ServletUtils.writeErrorMsg(response, "Unable to find entry " + entryName + " in " + sourceFile.getName());
                } else {
                  String name = null;
                  if (download) {
                    name = loc.getPath().substring(minusClass.lastIndexOf('.') + 1) + "-jr" + jarRelationID + ".java";
                  }
                  ServletUtils.writeInputStreamFragment(response, name, zip.getInputStream(entry), loc.getOffset(), loc.getLength());
                }
              } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable to read jar file", e);
              }
            }
          }
          return;
        }
      }
      
      // Lookup by jar comment id
      {
        String jarCommentID = request.getParameter("jarCommentID");
        if (jarCommentID != null) {
          JarLocationDB loc = db.getJarLocationByJarCommentID(jarCommentID);
          if (loc == null) {
            ServletUtils.writeErrorMsg(response, "Unable to find location for jar comment " + jarCommentID);
          } else {
            // Get the jar
            JarIndex index = repo.getJarIndex();
            IndexedJar indexed = index.getIndexedJar(loc.getHash());
            if (indexed == null) {
              ServletUtils.writeErrorMsg(response, "Unable to find jar " + loc.getJarID() + " with hash " + loc.getHash());
            } else {
              File sourceFile = indexed.getSourceFile();
              if (sourceFile == null) {
                IndexedJar source = index.getPossibleSourceMatch(indexed);
                if (source == null) {
                  sourceFile = indexed.getJarFile();
                } else {
                  sourceFile = source.getJarFile();
                }
              }
              try {
                ZipFile zip = new ZipFile(sourceFile);
                String minusClass = loc.getPath().substring(0, loc.getPath().lastIndexOf('.'));
                String entryName = minusClass.replace('.', '/') + ".java";
                ZipEntry entry = zip.getEntry(entryName);
                if (entry == null) {
                  ServletUtils.writeErrorMsg(response, "Unable to find entry " + entryName + " in " + sourceFile.getName());
                } else {
                  String name = null;
                  if (download) {
                    name = loc.getPath().substring(minusClass.lastIndexOf('.') + 1) + "-jr" + jarCommentID + ".java";
                  }
                  ServletUtils.writeInputStreamFragment(response, name, zip.getInputStream(entry), loc.getOffset(), loc.getLength());
                }
              } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable to read jar file", e);
              }
            }
          }
          return;
        }
      }
      
      ServletUtils.writeErrorMsg(response, "Invalid action");
    }
  }
}
