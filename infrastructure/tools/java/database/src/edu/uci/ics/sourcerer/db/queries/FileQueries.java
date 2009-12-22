package edu.uci.ics.sourcerer.db.queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;
import edu.uci.ics.sourcerer.model.db.LocationDB;

public class FileQueries {
  public static final ResultTranslator<LocationDB> LOCATION_RESULT_TRANSLATOR = new ResultTranslator<LocationDB>() {
    @Override
    public LocationDB translate(ResultSet result) throws SQLException {
      return new LocationDB(result.getString(1), result.getInt(2), result.getInt(3));
    }
    
    @Override
    public String getSelect() {
      return "path,offset,length";
    }
  };
  
  public static String getFilePathByEntityID(QueryExecutor executor, String entityID) {
    return executor.executeSingle("SELECT path FROM entities INNER JOIN files ON entities.file_id=files.file_id WHERE entity_id=" + entityID + ";", ResultTranslator.SIMPLE_RESULT_TRANSLATOR);
  }
  
  public static LocationDB getLocationByEntityID(QueryExecutor executor, String entityID) {
    return executor.selectSingle("entities INNER JOIN files ON entities.file_id=files.file_id", LOCATION_RESULT_TRANSLATOR.getSelect(), "entity_id=" + entityID, LOCATION_RESULT_TRANSLATOR);
  }
  
  public static LocationDB getLocationByRelationID(QueryExecutor executor, String relationID) {
    return executor.selectSingle("relations INNER JOIN files ON relations.file_id=files.file_id", LOCATION_RESULT_TRANSLATOR.getSelect(), "relation_id=" + relationID, LOCATION_RESULT_TRANSLATOR);
//    return executor.executeSingle("SELECT path FROM relations INNER JOIN files ON relations.file_id=files.file_id WHERE relation_id=" + relationID + ";", ResultTranslator.SIMPLE_RESULT_TRANSLATOR);
  }
  
  public static LocationDB getLocationByCommentID(QueryExecutor executor, String commentID) {
    return executor.selectSingle("comments INNER JOIN files ON comments.file_id=files.file_id", LOCATION_RESULT_TRANSLATOR.getSelect(), "comment_id=" + commentID, LOCATION_RESULT_TRANSLATOR);
  }
}
