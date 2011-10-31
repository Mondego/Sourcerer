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
//import edu.uci.ics.sourcerer.util.db.DatabaseConnection;
//
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public abstract class InlineDatabaseAccessor extends DatabaseAccessor {
//  protected InlineDatabaseAccessor() {
//    super();
//  }
//  
//  public abstract void action();
//  
//  public void execute() {
//    DatabaseConnection conn = new DatabaseConnection();
//    if (conn.open()) {
//      init(conn);
//      action();
//      close();
//    }
//  }
//}
