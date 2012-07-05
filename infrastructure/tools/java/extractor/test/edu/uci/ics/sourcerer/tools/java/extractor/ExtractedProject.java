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
//import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Map;
//import java.util.TreeMap;
//
//import edu.uci.ics.sourcerer.tools.java.model.extracted.EntityEX;
//import edu.uci.ics.sourcerer.tools.java.model.extracted.LocalVariableEX;
//import edu.uci.ics.sourcerer.tools.java.model.extracted.RelationEX;
//import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
//import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
//import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProject;
//import edu.uci.ics.sourcerer.util.TimeCounter;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//class ExtractedProject {
//  private ReaderBundle readers;
//  private Map<String, ExtractedFile> files;
//  
//  private ExtractedProject(File file) {
//    this.readers = new ReaderBundle(file);
//  }
//  
//  public static ExtractedProject make(ExtractedJavaProject project) {
//    return new ExtractedProject(project.getExtractionDir().toFile());
//  }
//  
//  public static ExtractedProject make(ExtractedJarFile jar) {
//    return new ExtractedProject(jar.getExtractionDir().toFile());
//  }
//  
//  public ExtractedFile getMatchingFile(ExtractedFile file) {
//    return files.get(file.getClassFile());
//  }
//  
//  public Collection<ExtractedFile> getFiles() {
//    if (files == null) {
//      loadFiles();
//    }
//    return Collections.unmodifiableCollection(files.values());
//  }
//  
//  private void loadFiles() {
//    logger.info("      Loading class files...");
//    TimeCounter counter = new TimeCounter();
//    files = new TreeMap<>();
//    for (EntityEX entity : readers.getTransientEntities()) {
//      if (entity.getLocation() != null) {
//        String classFile = entity.getLocation().getClassFile();
//        if (!files.containsKey(classFile)) {
//          files.put(classFile, new ExtractedFile(this, classFile));
//          counter.increment();
//        }
//      }
//    }
//    counter.logTimeAndCount(8, "file(s) loaded");
//  }
//  
//  Collection<EntityEX> loadEntities(ExtractedFile file) {
//    Collection<EntityEX> result = new ArrayList<>();
//    String path = file.getClassFile();
//    for (EntityEX entity : readers.getTransientEntities()) {
//      if (entity.getLocation() != null && path.equals(entity.getLocation().getClassFile())){
//        result.add(new EntityEX(entity));
//      }
//    }
//    return result;
//  }
//  
//  Collection<RelationEX> loadRelations(ExtractedFile file) {
//    Collection<RelationEX> result = new ArrayList<>();
//    String path = file.getClassFile();
//    for (RelationEX relation : readers.getTransientRelations()) {
//      if (relation.getLocation() != null && path.equals(relation.getLocation().getClassFile())) {
//        result.add(new RelationEX(relation));
//      }
//    }
//    return result;
//  }
//  
//  Collection<LocalVariableEX> loadLocalVariables(ExtractedFile file) {
//    Collection<LocalVariableEX> result = new ArrayList<>();
//    String path = file.getClassFile();
//    for (LocalVariableEX var : readers.getTransientLocalVariables()) {
//      if (var.getLocation() != null && path.equals(var.getLocation().getClassFile())) {
//        result.add(new LocalVariableEX(var));
//      }
//    }
//    return result;
//  }
//}
