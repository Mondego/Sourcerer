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

import edu.uci.ics.sourcerer.extractor.io.IJarRelationWriter;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.extracted.RelationExJarParser;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.io.Property;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarRelationWriter extends ExtractorWriter implements IJarRelationWriter {
  public JarRelationWriter(Repository input) {
    super(input, Property.RELATION_FILE);
  }
  
  @Override
  public void writeInside(String innerFqn, String outerFqn) {
    write(RelationExJarParser.getLine(Relation.INSIDE, innerFqn, outerFqn));
  }
  
  @Override
  public void writeExtends(String subTypeFqn, String superTypeFqn) {
    write(RelationExJarParser.getLine(Relation.EXTENDS, subTypeFqn, superTypeFqn));
  }
  
  @Override
  public void writeImplements(String subTypeFqn, String superTypeFqn) {
    write(RelationExJarParser.getLine(Relation.IMPLEMENTS, subTypeFqn, superTypeFqn));
  }
  
  @Override
  public void writeHolds(String fqn, String type) {
    write(RelationExJarParser.getLine(Relation.HOLDS, fqn, type));
  }
  
  @Override
  public void writeReturns(String fqn, String returnType) {
    write(RelationExJarParser.getLine(Relation.RETURNS, fqn, returnType));
  }
   
  @Override
  public void writeThrows(String fqn, String exceptionType) {
    write(RelationExJarParser.getLine(Relation.THROWS, fqn, exceptionType));
  }
  
  @Override
  public void writeParametrizedBy(String fqn, String typeVariable, int position) {
    write(RelationExJarParser.getLineParametrizedBy(fqn, typeVariable, position));
  }
}
