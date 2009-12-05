/*
 * Sourcerer: An infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package edu.uci.ics.sourcerer.db.schema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import edu.uci.ics.sourcerer.db.util.InsertBatcher;
import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Nov 29, 2009
 *
 */
public class EntitySimilarityTanimotoCoefficientTable {
	
  private EntitySimilarityTanimotoCoefficientTable() {}
  
  public static final String TABLE = "similarity_tanimoto";
  
  //---- LOCK ----
  public static String getReadLock() {
    return SchemaUtils.getReadLock(TABLE);
  }
  
  public static String getWriteLock() {
    return SchemaUtils.getWriteLock(TABLE);
  }
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
    		"lhs_eid BIGINT UNSIGNED NOT NULL",
            "rhs_eid BIGINT UNSIGNED NOT NULL",
            "similarity DOUBLE NOT NULL",
            "INDEX(lhs_eid)",
            "INDEX(rhs_eid)");
  }
  
  // ---- INSERT ----
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
	    return executor.getInsertBatcher(TABLE);
	  }
  
  private static String getInsertValue(String lhsEid, String rhsEid, String simValue) {
    return SchemaUtils.getInsertValue(
        SchemaUtils.convertNotNullNumber(lhsEid),
        SchemaUtils.convertNotNullNumber(rhsEid),
        SchemaUtils.convertNotNullNumber(simValue));
  }
  
  public static void insert(InsertBatcher batcher, String lhsEid, String rhsEid, String similarity) {
	    batcher.addValue(getInsertValue(lhsEid, rhsEid, similarity));
	  }
  
//  // ---- DELETE ----
//  public static void deleteByProjectID(QueryExecutor executor, String projectID) {
//    executor.delete(TABLE, "project_id=" + projectID);
//  }
  
  
//---- SELECT ----
  public static final ResultTranslator<Long> TRANSLATOR_RHS_EID = new ResultTranslator<Long>() {
    @Override
    public Long translate(ResultSet result) throws SQLException {
      return (result.getLong(2));
    }
  };
  
  public static Collection<Long> getSimilarEntityIds(QueryExecutor executor, long lhsEid){
	  return executor.select(TABLE, "rhs_eid", "lhs_eid=" + lhsEid, TRANSLATOR_RHS_EID);
  }
  
  public static Collection<Long> getTopKSimilarEntityIds(QueryExecutor executor, long lhsEid, int k){
	  return executor.select(TABLE, "rhs_eid", "lhs_eid=" + lhsEid 
			  + " ORDER BY similarity desc LIMIT " + k, 
			  TRANSLATOR_RHS_EID);
  }

}
