package edu.uci.ics.sourcerer.db.queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;
import edu.uci.ics.sourcerer.model.db.JarLocationDB;

public class JarFileQueries {
  public static final ResultTranslator<JarLocationDB> RESULT_TRANSLATOR = new ResultTranslator<JarLocationDB>() {
    @Override
    public JarLocationDB translate(ResultSet result) throws SQLException {
      String jarID = result.getString(1);
      String hash = result.getString(2);
      String path = result.getString(3);
      int offset = result.getInt(4);
      int length = result.getInt(5);
      return new JarLocationDB(jarID, hash, path, offset, length);
    }
    
    @Override
    public String getSelect() {
      return "jars.jar_id,jars.hash,jar_class_files.path,offset,length";
    }
  };
  
  public static JarLocationDB getLocationByJarEntityID(QueryExecutor executor, String jarEntityID) {
    return executor.selectSingle("jar_entities INNER JOIN jar_class_files INNER JOIN jars ON jar_entities.jclass_fid=jar_class_files.file_id AND jar_class_files.jar_id=jars.jar_id", RESULT_TRANSLATOR.getSelect(), "entity_id=" + jarEntityID, RESULT_TRANSLATOR);
  }
  
  public static JarLocationDB getLocationByJarRelationID(QueryExecutor executor, String jarEntityID) {
    return executor.selectSingle("jar_relations INNER JOIN jar_class_files INNER JOIN jars ON jar_relations.jclass_fid=jar_class_files.file_id AND jar_class_files.jar_id=jars.jar_id", RESULT_TRANSLATOR.getSelect(), "relation_id=" + jarEntityID, RESULT_TRANSLATOR);
  }
  
  public static JarLocationDB getLocationByJarCommentID(QueryExecutor executor, String jarEntityID) {
    return executor.selectSingle("jar_comments INNER JOIN jar_class_files INNER JOIN jars ON jar_comments.jclass_fid=jar_class_files.file_id AND jar_class_files.jar_id=jars.jar_id", RESULT_TRANSLATOR.getSelect(), "comment_id=" + jarEntityID, RESULT_TRANSLATOR);
  }
}
