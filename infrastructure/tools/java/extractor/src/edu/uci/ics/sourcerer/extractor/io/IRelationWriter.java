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
package edu.uci.ics.sourcerer.extractor.io;

import edu.uci.ics.sourcerer.model.Location;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public interface IRelationWriter extends IExtractorWriter {
  public void writeInside(String innerFqn, String outerFqn, Location location);

  public void writeExtends(String subTypeFqn, String superTypeFqn, Location location);

  public void writeImplements(String subTypeFqn, String superTypeFqn, Location loclocation);

  public void writeHolds(String fqn, String type, Location location);

  public void writeReads(String reader, String field, Location location);

  public void writeWrites(String writer, String field, Location location);

  public void writeCalls(String caller, String called, Location location);

  public void writeInstantiates(String creator, String created, Location location);

  public void writeReturns(String fqn, String returnType, Location location);

  public void writeThrows(String fqn, String exceptionType, Location location);

  public void writeCasts(String fqn, String newType, Location location);

  public void writeChecks(String fqn, String checkedType, Location location);

  public void writeAnnotatedBy(String entity, String annotation, Location location);

  public void writeUses(String fqn, String type, Location location);

  public void writeParametrizedBy(String fqn, String typeVariable, int pos, Location location);
  
  public void writeOverrides(String fqn, String overriddenFqn, Location location);
}