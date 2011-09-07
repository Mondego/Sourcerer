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
//import java.util.Map;
//
//import edu.uci.ics.sourcerer.model.extracted.EntityEX;
//import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
//import edu.uci.ics.sourcerer.model.extracted.RelationEX;
//import edu.uci.ics.sourcerer.repo.extracted.ExtractedProject;
//import edu.uci.ics.sourcerer.util.Helper;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class ExtractorOutputMap {
//  private Map<String, ExtractorOutput> outputByFile;
//  
//  private ExtractorOutputMap() {
//    outputByFile = Helper.newHashMap();
//  }
//  
//  private void buildEntitiesByFileMap(ExtractedProject project) {
//    for (EntityEX entity : project.getEntityReader()) {
//      ExtractorOutput output = outputByFile.get(entity.getPath());
//      if (output == null) {
//        output = new ExtractorOutput();
//        outputByFile.put(entity.getPath(), output);
//      }
//      output.add(entity);
//    }
//  }
//  
//  private void buildRelationsByFileMap(ExtractedProject project) {
//    for (RelationEX relation : project.getRelationReader()) {
//      ExtractorOutput output = outputByFile.get(relation.getPath());
//      if (output == null) {
//        output = new ExtractorOutput();
//        outputByFile.put(relation.getPath(), output);
//      }
//      output.add(relation);
//    }
//  }
//  
//  private void buildLocalVariablesByFileMap(ExtractedProject project) {
//    for (LocalVariableEX localVariable : project.getLocalVariableReader()) {
//      ExtractorOutput output = outputByFile.get(localVariable.getPath());
//      if (output == null) {
//        output = new ExtractorOutput();
//        outputByFile.put(localVariable.getPath(), output);
//      }
//      output.add(localVariable);
//    }
//  }
//  
//  public ExtractorOutput getExtractorOutput(String relativePath) {
//    return outputByFile.get(relativePath);
//  }
//  
//  public static ExtractorOutputMap getExtractorOutputMap(ExtractedProject project) {
//    ExtractorOutputMap map = new ExtractorOutputMap();
//    map.buildEntitiesByFileMap(project);
//    map.buildRelationsByFileMap(project);
//    map.buildLocalVariablesByFileMap(project);
//    return map;
//  }
//}
