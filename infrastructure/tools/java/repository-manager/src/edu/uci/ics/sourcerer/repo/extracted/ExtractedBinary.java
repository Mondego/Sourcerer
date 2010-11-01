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
package edu.uci.ics.sourcerer.repo.extracted;

import edu.uci.ics.sourcerer.repo.general.AbstractBinaryProperties;
import edu.uci.ics.sourcerer.repo.general.AbstractExtractedProperties;
import edu.uci.ics.sourcerer.repo.general.RepoFile;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class ExtractedBinary extends Extracted {
  public ExtractedBinary(RepoFile content) {
    super(content);
  }
  
  protected abstract AbstractBinaryProperties getBinaryProperties();
  
  protected AbstractExtractedProperties getProperties() {
    return getBinaryProperties();
  }
  
  public boolean empty() {
    return super.empty() && !hasBinary();
  }
  
  public boolean hasBinary() {
    return getExtractedFromBinary() + getBinaryExceptions() > 0;
  }
  
  public int getExtractedFromBinary() {
    return getBinaryProperties().getExtractedFromBinary();
  }
  
  public boolean sourceSkipped() {
    return getBinaryProperties().sourceSkipped();
  }
  
  public boolean hasBinaryExceptions() {
    return getBinaryExceptions() > 0;
  }
  
  public int getBinaryExceptions() {
    return getBinaryProperties().getBinaryExceptions();
  }
}
