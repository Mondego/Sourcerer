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
package edu.uci.ics.sourcerer.db.queries;

import static edu.uci.ics.sourcerer.db.schema.RelationsTable.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.db.LocationDB;
import edu.uci.ics.sourcerer.model.db.RelationDB;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RelationQueries extends Queries {
  private static final ResultTranslator<LocationDB> LOCATION_TRANSLATOR = new ResultTranslator<LocationDB>(TABLE, FILE_ID, OFFSET, LENGTH) {
    @Override
    public LocationDB translate(ResultSet result) throws SQLException {
      return new LocationDB(
          FILE_ID.convertFromDB(result.getString(1)),
          OFFSET.convertFromDB(result.getString(2)),
          LENGTH.convertFromDB(result.getString(3)));
    }
  };
  
  public static final ResultTranslator<RelationDB> RELATION_TRANSLATOR = new ResultTranslator<RelationDB>(TABLE, RELATION_ID, RELATION_TYPE, RELATION_CLASS, LHS_EID, RHS_EID, PROJECT_ID, FILE_ID, OFFSET, LENGTH) {
    @Override
    public RelationDB translate(ResultSet result) throws SQLException {
      return new RelationDB(
          RELATION_ID.convertFromDB(result.getString(1)),
          RELATION_TYPE.convertFromDB(result.getString(2)),
          RELATION_CLASS.convertFromDB(result.getString(3)),
          LHS_EID.convertFromDB(result.getString(4)),
          RHS_EID.convertFromDB(result.getString(5)),
          PROJECT_ID.convertFromDB(result.getString(6)),
          FILE_ID.convertFromDB(result.getString(7)),
          OFFSET.convertFromDB(result.getString(8)),
          LENGTH.convertFromDB(result.getString(9)));
    }
  };
  
  protected RelationQueries(QueryExecutor executor) {
    super(executor);
  }
  
  public LocationDB getLocationByRelationID(Integer relationID) {
    return executor.selectSingle(RELATION_ID.getEquals(relationID), LOCATION_TRANSLATOR); 
  }
  
  public int getRelationCountBy(int targetID, Relation ... relations) {
    return executor.selectSingleInt(TABLE, "count(*)", and(RHS_EID.getEquals(targetID), RELATION_TYPE.getIn(relations)));
  }
  
  public Collection<Integer> getRelationProjectCountBy(int targetID, Relation ... relations) {
    return executor.select(TABLE, "project_id", and(RHS_EID.getEquals(targetID), RELATION_TYPE.getIn(relations)), ResultTranslator.SIMPLE_INT_TRANSLATOR);
  }
  
  public Collection<RelationDB> getRelationsByProject(Integer projectID, Relation ... relations) {
    return executor.select(and(PROJECT_ID.getEquals(projectID), RELATION_TYPE.getIn(relations)), RELATION_TRANSLATOR);
  }
  
  public Collection<RelationDB> getRelationsByFileID(Integer fileID, Relation ... relations) {
    return executor.select(and(FILE_ID.getEquals(fileID), RELATION_TYPE.getIn(relations)), RELATION_TRANSLATOR);
  }
}
