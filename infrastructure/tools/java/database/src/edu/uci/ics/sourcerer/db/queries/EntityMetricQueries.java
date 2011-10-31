///* 
// * Sourcerer: an infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
//
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.uci.ics.sourcerer.db.queries;
//
//import static edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable.*;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.Collection;
//
//import edu.uci.ics.sourcerer.db.util.QueryExecutor;
//import edu.uci.ics.sourcerer.db.util.ResultTranslator;
//import edu.uci.ics.sourcerer.model.db.EntityMetricDB;
//import edu.uci.ics.sourcerer.model.metrics.Metric;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class EntityMetricQueries extends Queries {
//  private ResultTranslator<EntityMetricDB> ENTITY_METRIC_TRANSLATOR = new ResultTranslator<EntityMetricDB>(TABLE, PROJECT_ID, FILE_ID, ENTITY_ID, METRIC_TYPE, VALUE) {
//    @Override
//    public EntityMetricDB translate(ResultSet result) throws SQLException {
//      return new EntityMetricDB(
//          PROJECT_ID.convertFromDB(result.getString(1)),
//          FILE_ID.convertFromDB(result.getString(2)),
//          ENTITY_ID.convertFromDB(result.getString(3)),
//          METRIC_TYPE.convertFromDB(result.getString(4)), 
//          VALUE.convertFromDB(result.getString(5)));
//    }
//  };
//  
//  protected EntityMetricQueries(QueryExecutor executor) {
//    super(executor);
//  }
//  
//  public Collection<EntityMetricDB> getMetricsByProjectID(Integer projectID, Metric ... metrics) {
//    if (metrics.length == 0) {
//      return executor.select(PROJECT_ID.getEquals(projectID), ENTITY_METRIC_TRANSLATOR);
//    } else {
//      return executor.select(and(PROJECT_ID.getEquals(projectID), METRIC_TYPE.getIn(metrics)), ENTITY_METRIC_TRANSLATOR);
//    }
//  }
//}
