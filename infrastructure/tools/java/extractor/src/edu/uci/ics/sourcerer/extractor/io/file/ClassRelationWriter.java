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
package edu.uci.ics.sourcerer.extractor.io.file;

import java.io.File;

import edu.uci.ics.sourcerer.extractor.io.IClassRelationWriter;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.extracted.Extracted;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClassRelationWriter extends ExtractorWriter implements IClassRelationWriter {
  public ClassRelationWriter(File output, IFileSet input) {
    super(new File(output, Extracted.RELATION_FILE.getValue()), input);
  }
  
  @Override
  public void writeInside(String innerFqn, String outerFqn, String path) {
    write(RelationEX.getClassLine(Relation.INSIDE, innerFqn, outerFqn, path));
  }
  
  @Override
  public void writeExtends(String subTypeFqn, String superTypeFqn, String path) {
    write(RelationEX.getClassLine(Relation.EXTENDS, subTypeFqn, superTypeFqn, path));
  }
  
  @Override
  public void writeImplements(String subTypeFqn, String superTypeFqn, String path) {
    write(RelationEX.getClassLine(Relation.IMPLEMENTS, subTypeFqn, superTypeFqn, path));
  }
  
  @Override
  public void writeHolds(String fqn, String type, String path) {
    write(RelationEX.getClassLine(Relation.HOLDS, fqn, type, path));
  }
  
  @Override
  public void writeReturns(String fqn, String returnType, String path) {
    write(RelationEX.getClassLine(Relation.RETURNS, fqn, returnType, path));
  }
   
  @Override
  public void writeThrows(String fqn, String exceptionType, String path) {
    write(RelationEX.getClassLine(Relation.THROWS, fqn, exceptionType, path));
  }
  
  @Override
  public void writeParametrizedBy(String fqn, String typeVariable, int position, String path) {
    write(RelationEX.getClassLineParametrizedBy(fqn, typeVariable, position, path));
  }
}
