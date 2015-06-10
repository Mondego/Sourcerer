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

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import edu.uci.ics.sourcerer.services.slicer.model.SlicedFile;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class SlicedFileImpl implements SlicedFile {
  private static final Comparator<SlicedEntityImpl> COMP = new Comparator<SlicedEntityImpl>() {
    @Override
    public int compare(SlicedEntityImpl o1, SlicedEntityImpl o2) {
      Integer o1o = o1.getOffset();
      Integer o2o = o2.getOffset();
      if (o1o == null && o2o == null) {
        return Integer.compare(o1.getEntityID(), o2.getEntityID());
      } else if (o1o == null) {
        return 1;
      } else if (o2o == null) {
        return -1;
      } else {
        return o1o.compareTo(o2o);
      }
    }
  };
  
  private final Integer fileID;
  private final TreeSet<SlicedEntityImpl> entities;
  private Collection<SlicedImportImpl> imports;
    
  SlicedFileImpl(Integer fileID) {
    this.fileID = fileID;
    entities = new TreeSet<>(COMP);
  }
  
  @Override
  public Integer getFileID() {
    return fileID;
  }
  
  void addEntity(SlicedEntityImpl entity) {
    entities.add(entity);
  }
  
  @Override
  public Collection<? extends SlicedEntityImpl> getEntities() {
    return entities;
  }
  
  void setImports(Collection<SlicedImportImpl> imports) {
    this.imports = imports;
  }
  
  @Override
  public Collection<? extends SlicedImportImpl> getImports() {
    return imports;
  }
}
