// Sourcerer: an infrastructure for large-scale source code analysis.
// Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
package edu.uci.ics.sourcerer.repo;

import java.io.File;

import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class IndexedJar {
  private String basePath;
  private String relativePath;
  
  protected IndexedJar(String basePath, String relativePath) {
    this.basePath = basePath;
    this.relativePath = relativePath;
  }
  
  public String getRelativePath() {
    return relativePath;
  }
  
  public String getPath() {
    return basePath + relativePath; 
  }
  
  public File getFile() {
    return new File(getPath());
  }
  
  public File getPropertiesFile() {
    File file = getFile(); 
    String name = file.getName();
    name = name.substring(0, name.lastIndexOf('.'));
    return new File(file.getParentFile(), name + ".properties");
  }
    
  public String getOutputPath(File baseDir) {
    return baseDir.getPath() + "/" + relativePath;
  }
  
  public void copyPropertiesFile(File baseDir) {
    File outputDir = new File(getOutputPath(baseDir));
    String name = outputDir.getName();
    name = name.substring(0, name.lastIndexOf('.'));
    File output = new File(outputDir, name + ".properties");
    FileUtils.copyFile(getPropertiesFile(), output);
  }
}
