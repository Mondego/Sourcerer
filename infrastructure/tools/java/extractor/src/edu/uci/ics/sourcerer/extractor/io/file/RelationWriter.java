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

import edu.uci.ics.sourcerer.extractor.io.IRelationWriter;
import edu.uci.ics.sourcerer.extractor.io.Location;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.extracted.Extracted;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class RelationWriter extends ExtractorWriter implements IRelationWriter {
  public RelationWriter(File output, IFileSet input) {
    super(new File(output, Extracted.RELATION_FILE.getValue()), input);
  }

  private void writeRelation(Relation type, String lhs, String rhs, Location location) {
    write(RelationEX.getSourceLine(type, lhs, rhs, convertToRelativePath(location.getPath()), location.getStartPosition(), location.getLength()));
  }
  
  @Override
  public void writeInside(String innerFqn, String outerFqn, Location location) {
    write(RelationEX.getSourceLineInside(innerFqn, outerFqn, convertToRelativePath(location.getPath())));
  }
  
  @Override
  public void writeExtends(String subTypeFqn, String superTypeFqn, Location location) {
    writeRelation(Relation.EXTENDS, subTypeFqn, superTypeFqn, location);
  }
  
  @Override
  public void writeImplements(String subTypeFqn, String superTypeFqn, Location location) {
    writeRelation(Relation.IMPLEMENTS, subTypeFqn, superTypeFqn, location);
  }
  
  @Override
  public void writeHolds(String fqn, String type, Location location) {
    writeRelation(Relation.HOLDS, fqn, type, location);
  }
  
  @Override
  public void writeReads(String reader, String field, Location location) {
    writeRelation(Relation.READS, reader, field, location);
  }
  
  @Override
  public void writeWrites(String writer, String field, Location location) {
    writeRelation(Relation.WRITES, writer, field, location);
  }
  
  @Override
  public void writeCalls(String caller, String callee, Location location) {
    writeRelation(Relation.CALLS, caller, callee, location);
  }

  @Override
  public void writeReturns(String fqn, String returnType, Location location) {
    writeRelation(Relation.RETURNS, fqn, returnType, location);
  }
    
  @Override
  public void writeThrows(String fqn, String exceptionType, Location location) {
    writeRelation(Relation.THROWS, fqn, exceptionType, location);
  }
  
  @Override
  public void writeCasts(String fqn, String newType, Location location) {
    writeRelation(Relation.CASTS, fqn, newType, location);
  }
  
  @Override
  public void writeChecks(String fqn, String checkedType, Location location) {
    writeRelation(Relation.CHECKS, fqn, checkedType, location);
  }
  
  @Override
  public void writeAnnotatedBy(String entity, String annotation, Location location) {
    writeRelation(Relation.ANNOTATED_BY, entity, annotation, location);
  }
  
  @Override
  public void writeUses(String fqn, String type, Location location) {
    writeRelation(Relation.USES, fqn, type, location);
  }
  
  @Override
  public void writeParametrizedBy(String fqn, String typeVariable, int pos, Location location) {
    write(RelationEX.getSourceLineParametrizedBy(fqn, typeVariable, pos, convertToRelativePath(location.getPath()), location.getStartPosition(), location.getLength()));
  }

  @Override
  public void writeInstantiates(String creator, String created, Location location) {
    writeRelation(Relation.INSTANTIATES, creator, created, location);
  }
}
