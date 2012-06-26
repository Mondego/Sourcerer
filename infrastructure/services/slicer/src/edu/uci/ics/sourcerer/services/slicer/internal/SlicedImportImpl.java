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
package edu.uci.ics.sourcerer.services.slicer.internal;

import edu.uci.ics.sourcerer.services.slicer.model.SlicedImport;
import edu.uci.ics.sourcerer.tools.java.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class SlicedImportImpl implements SlicedImport {
  private final boolean isStatic;
  private final boolean isOnDemand;
  private final Integer eid;
  private final Integer offset;
  private final Integer length;
  
  SlicedImportImpl(TypedQueryResult result) {
    isStatic = result.getResult(ImportsTable.STATIC);
    isOnDemand = result.getResult(ImportsTable.ON_DEMAND);
    eid = result.getResult(ImportsTable.EID);
    offset = result.getResult(ImportsTable.OFFSET);
    length = result.getResult(ImportsTable.LENGTH);
  }
  
  @Override
  public boolean isStatic() {
    return isStatic;
  }
  
  @Override
  public boolean isOnDemand() {
    return isOnDemand;
  }
  
  @Override
  public Integer getEid() {
    return eid;
  }
  
  @Override
  public Integer getOffset() {
    return offset;
  }
  
  @Override
  public Integer getLength() {
    return length;
  }
}
