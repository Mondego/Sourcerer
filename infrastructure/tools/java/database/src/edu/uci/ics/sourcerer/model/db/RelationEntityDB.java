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
//package edu.uci.ics.sourcerer.model.db;
//
//import edu.uci.ics.sourcerer.model.Entity;
//import edu.uci.ics.sourcerer.model.Relation;
//import edu.uci.ics.sourcerer.model.RelationClass;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class RelationEntityDB {
//  private RelationDB relation;
//  private EntityDB entity;
//  
//  public RelationEntityDB(Integer relationID, Relation relationType, RelationClass relationClass, Integer lhsEid, Integer rhsEid, Integer relProjectID, Integer relFileID, Integer relOffset, Integer relLength, Integer entityID, Entity type, String fqn, Integer modifiers, Integer multi, Integer projectID, Integer fileID, Integer offset, Integer length) {
//    relation = new RelationDB(relationID, relationType, relationClass, lhsEid, rhsEid, relProjectID, relFileID, relOffset, relLength);
//    entity = new EntityDB(entityID, type, fqn, modifiers, multi, projectID, fileID, offset, length);
//  }
//  
//  public RelationDB getRelation() {
//    return relation;
//  }
//  
//  public EntityDB getEntity() {
//    return entity;
//  }
//}
