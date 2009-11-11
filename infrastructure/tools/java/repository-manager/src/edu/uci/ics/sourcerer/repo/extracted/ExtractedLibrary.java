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

import java.io.File;

import edu.uci.ics.sourcerer.repo.general.LibraryProperties;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedLibrary extends Extracted {
  private LibraryProperties properties;
  
  public ExtractedLibrary(File content) {
    super(content);
    properties = LibraryProperties.load(getPropertiesFile());
  }
    
  public void createPropertiesFile(String name, int fromBinary, int binaryExceptions, int fromSource, int sourceExceptions) {
    LibraryProperties.create(getPropertiesFile(), name, fromBinary, binaryExceptions, fromSource, sourceExceptions);
  }
  
  public String getName() {
    return properties.getName();
  }
  
  public boolean extracted() {
    return properties.extracted();
  }
  
  public boolean hasSource() {
    return getExtractedFromSource() + getSourceExceptions() > 0;
  }
  
  public boolean sourceError() {
    return getSourceExceptions() > 0;
  }
  
  public boolean binaryError() {
    return getBinaryExceptions() > 0;
  }
  
  public int getExtractedFromBinaryCount() {
    return properties.getExtractedFromBinary();
  }
  
  public int getExtractedFromSource() {
    return properties.getExtractedFromSource();
  }
  
  public int getSourceExceptions() {
    return properties.getSourceExceptions();
  }
  
  public int getBinaryExceptions() {
    return properties.getBinaryExceptions();
  }
}
