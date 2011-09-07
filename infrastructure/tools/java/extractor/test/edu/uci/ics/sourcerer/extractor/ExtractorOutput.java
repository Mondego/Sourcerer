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
//package edu.uci.ics.sourcerer.extractor;
//
//import java.util.Collection;
//
//import edu.uci.ics.sourcerer.model.extracted.EntityEX;
//import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
//import edu.uci.ics.sourcerer.model.extracted.RelationEX;
//import edu.uci.ics.sourcerer.util.Helper;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class ExtractorOutput {
//  private Collection<EntityEX> entities;
//  private Collection<RelationEX> relations;
//  private Collection<LocalVariableEX> localVariables;
//  
//  protected ExtractorOutput() {
//    entities = Helper.newLinkedList();
//    relations = Helper.newLinkedList();
//    localVariables = Helper.newLinkedList();
//  }
//  
//  public void add(EntityEX entity) {
//    entities.add(entity);
//  }
//  
//  public void add(RelationEX relation) {
//    relations.add(relation);
//  }
//  
//  public void add(LocalVariableEX localVariable) {
//    localVariables.add(localVariable);
//  }
//  
//  public Iterable<EntityEX> getEntities() {
//    return entities;
//  }
//  
//  public Iterable<RelationEX> getRelations() {
//    return relations;
//  }
//  
//  public Iterable<LocalVariableEX> getLocalVariables() {
//    return localVariables;
//  }
//}
