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
//package edu.uci.ics.sourcerer.tools.java.extractor;
//
//import java.util.Collection;
//import java.util.Collections;
//
//import edu.uci.ics.sourcerer.tools.java.model.extracted.EntityEX;
//import edu.uci.ics.sourcerer.tools.java.model.extracted.LocalVariableEX;
//import edu.uci.ics.sourcerer.tools.java.model.extracted.RelationEX;
//import edu.uci.ics.sourcerer.util.CachedReference;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//class ExtractedFile {
//  private final ExtractedProject project;
//  private final String classFile;
//  
//  private CachedReference<Collection<EntityEX>> entities = new CachedReference<Collection<EntityEX>>() {
//    @Override
//    protected Collection<EntityEX> create() {
//      return Collections.unmodifiableCollection(project.loadEntities(ExtractedFile.this));
//    }
//  };
//  private CachedReference<EntitySet> entitySet = new CachedReference<EntitySet>() {
//    @Override
//    protected EntitySet create() {
//      return EntitySet.make(entities.get());
//    }
//  };
//  private CachedReference<Collection<RelationEX>> relations = new CachedReference<Collection<RelationEX>>() {
//    @Override
//    protected Collection<RelationEX> create() {
//      return Collections.unmodifiableCollection(project.loadRelations(ExtractedFile.this));
//    }
//  };
//  private CachedReference<RelationSet> relationSet = new CachedReference<RelationSet>() {
//    @Override
//    protected RelationSet create() {
//      return RelationSet.make(relations.get(), entitySet.get());
//    }
//  };
//  private CachedReference<Collection<LocalVariableEX>> localVariables = new CachedReference<Collection<LocalVariableEX>>() {
//    @Override
//    protected Collection<LocalVariableEX> create() {
//      return Collections.unmodifiableCollection(project.loadLocalVariables(ExtractedFile.this));
//    }
//  }; 
//  private CachedReference<LocalVariableSet> localVariableSet = new CachedReference<LocalVariableSet>() {
//    @Override
//    protected LocalVariableSet create() {
//      return LocalVariableSet.make(localVariables.get());
//    }
//  };
//  
//  ExtractedFile(ExtractedProject project, String classFile) {
//    this.project = project;
//    this.classFile = classFile;
//  }
//  
//  public Collection<EntityEX> getEntities() {
//    return entities.get();
//  }
//  
//  public EntitySet getEntitySet() {
//    return entitySet.get();
//  }
//  
//  public Collection<RelationEX> getRelations() {
//    return relations.get();
//  }
//  
//  public RelationSet getRelationSet() {
//    return relationSet.get();
//  }
//  
//  public Collection<LocalVariableEX> getLocalVariables() {
//    return localVariables.get();
//  }
//  
//  public LocalVariableSet getLocalVariableSet() {
//    return localVariableSet.get();
//  }
//   
//  public String getClassFile() {
//    return classFile;
//  }
//  
//  @Override
//  public String toString() {
//    return classFile;
//  }
//}
