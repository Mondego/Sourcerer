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

import edu.uci.ics.sourcerer.repo.general.AbstractBinaryProperties;
import edu.uci.ics.sourcerer.repo.general.LibraryProperties;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedLibrary extends ExtractedBinary {
  private LibraryProperties properties;
  
  public ExtractedLibrary(File content, String relativePath) {
    super(content, relativePath);
    properties = LibraryProperties.load(getPropertiesFile());
  }
    
  public void createPropertiesFile(String name, int fromBinary, int binaryExceptions, int fromSource, int sourceExceptions) {
    LibraryProperties.create(getPropertiesFile(), name, fromBinary, binaryExceptions, fromSource, sourceExceptions);
  }
  
  protected AbstractBinaryProperties getBinaryProperties() {
    return properties;
  }
  
  public void copyLibraryJar(File orig) {
    File copy = new File(content, "lib.jar");
    FileUtils.copyFile(orig, copy);
  }
  
  public void copyLibraryJarSource(File orig) {
    File copy = new File(content, "source.jar");
    FileUtils.copyFile(orig, copy);
  }
}
