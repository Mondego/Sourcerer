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
//<<<<<<< HEAD
//import static edu.uci.ics.sourcerer.db.schema.ImportsTable.*;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.Collection;
//
//import edu.uci.ics.sourcerer.db.util.QueryExecutor;
//import edu.uci.ics.sourcerer.db.util.ResultTranslator;
//import edu.uci.ics.sourcerer.model.db.ImportDB;
//=======
//import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
//>>>>>>> flossmole
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public final class ImportQueries extends Queries {
//  public static final ResultTranslator<ImportDB> IMPORT_TRANSLATOR = new ResultTranslator<ImportDB>(TABLE, STATIC, ON_DEMAND, EID, PROJECT_ID, FILE_ID, OFFSET, LENGTH) {
//    @Override
//    public ImportDB translate(ResultSet result) throws SQLException {
//      return new ImportDB(
//          STATIC.convertFromDB(result.getString(1)),
//          ON_DEMAND.convertFromDB(result.getString(2)),
//          EID.convertFromDB(result.getString(3)),
//          PROJECT_ID.convertFromDB(result.getString(4)),
//          FILE_ID.convertFromDB(result.getString(5)),
//          OFFSET.convertFromDB(result.getString(6)),
//          LENGTH.convertFromDB(result.getString(7)));
//    }
//  };
//  
//  protected ImportQueries(QueryExecutor executor) {
//    super(executor);
//  }
//  
//  public Collection<ImportDB> getImportsByFileID(Integer fileID) {
//    return executor.select(FILE_ID.getEquals(fileID), IMPORT_TRANSLATOR);
//  }
//}
