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
//package edu.uci.ics.sourcerer.db.tools;
//
//import java.util.Collection;
//
//import edu.uci.ics.sourcerer.model.Entity;
//import edu.uci.ics.sourcerer.model.RelationClass;
//import edu.uci.ics.sourcerer.model.db.SmallEntityDB;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class RelationClassifier {
//  private Collection<Integer> libraryProjects;
//  
//  public RelationClassifier(Collection<Integer> libraryProjects) {
//    this.libraryProjects = libraryProjects;
//  }
//  
//  public RelationClass getRelationClass(SmallEntityDB lhs, SmallEntityDB rhs) {
//    if (lhs.getProjectID().equals(rhs.getProjectID())) {
//      return RelationClass.INTERNAL;
//    } else if (libraryProjects.contains(rhs.getProjectID())) {
//      return RelationClass.JAVA_LIBRARY;
//    } else if (rhs.getType() == Entity.ARRAY ||
//        rhs.getType() == Entity.DUPLICATE ||
//        rhs.getType() == Entity.PARAMETERIZED_TYPE ||
//        rhs.getType() == Entity.WILDCARD){
//      return RelationClass.NOT_APPLICABLE;
//    } else if (rhs.getType() == Entity.UNKNOWN) {
//      return RelationClass.UNKNOWN;
//    } else {
//      return RelationClass.EXTERNAL;
//    }
//  }
//}
