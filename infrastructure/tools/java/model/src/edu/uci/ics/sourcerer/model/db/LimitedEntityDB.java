package edu.uci.ics.sourcerer.model.db;

import edu.uci.ics.sourcerer.model.Entity;

public class LimitedEntityDB {
  private String projectID;
  private String entityID;
  private Entity type;
  public LimitedEntityDB(String projectID, String entityID, Entity type) {
    this.projectID = projectID;
    this.entityID = entityID;
    this.type = type;
  }
  
  public String getProjectID() {
    return projectID;
  }
  
  public String getEntityID() {
    return entityID;
  }
  
  public Boolean isInternal(String projectID) {
    if (type.isSyntheticTypeEntity()) {
      return null;
    } else {
      return projectID.equals(this.projectID);
    }
  }
}
