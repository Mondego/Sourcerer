package edu.uci.ics.sourcerer.server.file;

import edu.uci.ics.sourcerer.db.queries.FileQueries;
import edu.uci.ics.sourcerer.db.queries.JarFileQueries;
import edu.uci.ics.sourcerer.db.schema.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.schema.FilesTable;
import edu.uci.ics.sourcerer.db.schema.JarClassFilesTable;
import edu.uci.ics.sourcerer.db.schema.JarsTable;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.model.db.JarClassFileDB;
import edu.uci.ics.sourcerer.model.db.JarDB;
import edu.uci.ics.sourcerer.model.db.JarLocationDB;
import edu.uci.ics.sourcerer.model.db.LocationDB;

public class FileServerDatabaseAccessor extends DatabaseAccessor {
  protected FileServerDatabaseAccessor(DatabaseConnection connection) {
    super(connection);
  }
  
  public LocationDB getLocationByEntityID(String entityID) {
    return FileQueries.getLocationByEntityID(executor, entityID);
  }
  
  public LocationDB getLocationByRelationID(String relationID) {
    return FileQueries.getLocationByRelationID(executor, relationID);
  }
  
  public LocationDB getLocationByCommentID(String commentID) {
    return FileQueries.getLocationByCommentID(executor, commentID);
  }
  
  public String getFilePathByFileID(String fileID) {
    return FilesTable.getFilePathByFileID(executor, fileID);
  }
  
  public JarDB getJarByJarID(String jarID) {
    return JarsTable.getJarByJarID(executor, jarID);
  }
  
  public JarClassFileDB getJarClassFileByFileID(String fileID) {
    return JarClassFilesTable.getJarClassFileByFileID(executor, fileID);
  }
  
  public JarLocationDB getJarLocationByJarEntityID(String jarEntityID) {
    return JarFileQueries.getLocationByJarEntityID(executor, jarEntityID);
  }
  
  public JarLocationDB getJarLocationByJarRelationID(String jarRelationID) {
    return JarFileQueries.getLocationByJarRelationID(executor, jarRelationID);
  }
  
  public JarLocationDB getJarLocationByJarCommentID(String jarCommentID) {
    return JarFileQueries.getLocationByJarCommentID(executor, jarCommentID);
  }
}
