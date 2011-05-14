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

import static edu.uci.ics.sourcerer.db.schema.EntitiesTable.ENTITY_ID;
import static edu.uci.ics.sourcerer.db.schema.EntitiesTable.ENTITY_TYPE;
import static edu.uci.ics.sourcerer.db.schema.EntitiesTable.FILE_ID;
import static edu.uci.ics.sourcerer.db.schema.EntitiesTable.FQN;
import static edu.uci.ics.sourcerer.db.schema.EntitiesTable.LENGTH;
import static edu.uci.ics.sourcerer.db.schema.EntitiesTable.OFFSET;
import static edu.uci.ics.sourcerer.db.schema.EntitiesTable.PROJECT_ID;
import static edu.uci.ics.sourcerer.db.schema.EntitiesTable.TABLE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.db.LocationDB;
import edu.uci.ics.sourcerer.model.db.MediumEntityDB;
import edu.uci.ics.sourcerer.model.db.SmallEntityDB;
import edu.uci.ics.sourcerer.util.db.QueryExecutor;
import edu.uci.ics.sourcerer.util.db.ResultTranslator;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class EntityQueries extends Queries {
  private static final ResultTranslator<LocationDB> LOCATION_TRANSLATOR = new ResultTranslator<LocationDB>(TABLE, FILE_ID, OFFSET, LENGTH) {
    @Override
    public LocationDB translate(ResultSet result) throws SQLException {
      return new LocationDB(FILE_ID.convertFromDB(result.getString(1)), OFFSET.convertFromDB(result.getString(2)), LENGTH.convertFromDB(result.getString(3)));
    }
  };
  
  private static final ResultTranslator<SmallEntityDB> SMALL_ENTITY_TRANSLATOR = new ResultTranslator<SmallEntityDB>(TABLE, PROJECT_ID, ENTITY_ID, ENTITY_TYPE) {
    @Override
    public SmallEntityDB translate(ResultSet result) throws SQLException {
      return new SmallEntityDB(ENTITY_ID.convertFromDB(result.getString(2)), ENTITY_TYPE.convertFromDB(result.getString(3)), PROJECT_ID.convertFromDB(result.getString(1)));
    }
  };
  
  private static final ResultTranslator<MediumEntityDB> MEDIUM_ENTITY_TRANSLATOR = new ResultTranslator<MediumEntityDB>(TABLE, FQN, PROJECT_ID, ENTITY_ID, ENTITY_TYPE) {
    @Override
    public MediumEntityDB translate(ResultSet result) throws SQLException {
      return new MediumEntityDB(ENTITY_ID.convertFromDB(result.getString(3)), ENTITY_TYPE.convertFromDB(result.getString(4)), FQN.convertFromDB(result.getString(1)), PROJECT_ID.convertFromDB(result.getString(2)));
    }
  };
  
  protected EntityQueries(QueryExecutor executor) {
    super(executor);
  }

  public LocationDB getLocationByEntityID(Integer entityID) {
    return executor.selectSingle(ENTITY_ID.getEquals(entityID), LOCATION_TRANSLATOR); 
  }
  
  public Collection<SmallEntityDB> getSmallByFqn(String fqn, String inClause) {
    return executor.select(and(FQN.getEquals(fqn), PROJECT_ID.getInFromClause(inClause)), SMALL_ENTITY_TRANSLATOR);
  }
  
  public Iterable<MediumEntityDB> getMediumReferenceableByFqnPrefix(String prefix) {
    return executor.selectStreamed(and(FQN.getLike(prefix + "%"), ENTITY_TYPE.getNin(Entity.PARAMETER, Entity.LOCAL_VARIABLE)), MEDIUM_ENTITY_TRANSLATOR);
  }
  
  public Iterable<MediumEntityDB> getMediumExternalByProjectID(Integer projectID) {
    return executor.selectStreamed(and(PROJECT_ID.getEquals(projectID), ENTITY_TYPE.getNin(Entity.PARAMETER, Entity.LOCAL_VARIABLE)), MEDIUM_ENTITY_TRANSLATOR);
  }
  
  public Iterable<MediumEntityDB> getMediumLocalByProjectID(Integer projectID) {
    return executor.selectStreamed(and(PROJECT_ID.getEquals(projectID), ENTITY_TYPE.getIn(Entity.PARAMETER, Entity.LOCAL_VARIABLE)) + " ORDER BY " + ENTITY_ID.getName() + " ASC", MEDIUM_ENTITY_TRANSLATOR);
  }
  
  public Iterable<MediumEntityDB> getMediumSyntheticByProjectID(Integer projectID) {
    return executor.selectStreamed(and(PROJECT_ID.getEquals(projectID), ENTITY_TYPE.getIn(Entity.ARRAY, Entity.WILDCARD, Entity.TYPE_VARIABLE, Entity.PARAMETERIZED_TYPE, Entity.DUPLICATE)), MEDIUM_ENTITY_TRANSLATOR);
  }
  
  public Collection<MediumEntityDB> getMediumTopLevelByFileID(Integer fileID) {
    return executor.select(and(FILE_ID.getEquals(fileID), ENTITY_TYPE.getIn(Entity.CLASS, Entity.INTERFACE, Entity.ANNOTATION, Entity.ENUM)), MEDIUM_ENTITY_TRANSLATOR);
  }
  
  public Iterable<MediumEntityDB> getMediumByProjectID(Integer projectID, Entity type) {
    return executor.selectStreamed(and(PROJECT_ID.getEquals(projectID), ENTITY_TYPE.getEquals(type)), MEDIUM_ENTITY_TRANSLATOR);
  }
}
