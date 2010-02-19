package edu.uci.ics.sourcerer.server.file;

import edu.uci.ics.sourcerer.db.schema.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.model.db.FileDB;
import edu.uci.ics.sourcerer.model.db.LocationDB;
import edu.uci.ics.sourcerer.model.db.ProjectDB;

public class FileServerDatabaseAccessor extends DatabaseAccessor {
  protected FileServerDatabaseAccessor(DatabaseConnection connection) {
    super(connection);
  }
  
  public LocationDB getLocationByEntityID(String entityID) {
    return entitiesTable.getLocationByEntityID(entityID);
  }
  
  public LocationDB getLocationByRelationID(String relationID) {
    return relationsTable.getLocationByRelationID(relationID);
  }
  
  public LocationDB getLocationByCommentID(String commentID) {
    return commentsTable.getLocationByCommentID(commentID);
  }
  
  public FileDB getFileByFileID(String fileID) {
    return filesTable.getFileByFileID(fileID);
  }
  
  public ProjectDB getProjectByProjectID(String projectID) {
    return projectsTable.getProjectByProjectID(projectID);
  }
  
  public void close() {
    super.close();
  }
}
