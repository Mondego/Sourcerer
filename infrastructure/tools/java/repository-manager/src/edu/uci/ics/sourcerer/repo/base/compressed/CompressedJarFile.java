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
package edu.uci.ics.sourcerer.repo.base.compressed;

import java.io.File;

import edu.uci.ics.sourcerer.repo.base.IJarFile;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CompressedJarFile implements IJarFile {
  private String hash;
  private String name;
  private String relativePath;
  
  private CompressedFileSet fileSet;
  
  private File file;
  private boolean fileRetrieved;
  
  protected CompressedJarFile(String relativePath, String hash, CompressedFileSet fileSet) {
    this.hash = hash;
    this.relativePath = relativePath;
    int index = relativePath.lastIndexOf('/');
    if (index == -1) {
      name = relativePath;
    } else {
      name = relativePath.substring(index + 1);
    }
    this.fileSet = fileSet;
  }

  @Override
  public String getHash() {
    return hash;
  }
  
  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public String getPath() {
    if (getFile() == null) {
      return null;
    } else {
      return file.getPath();
    }
  }
  
  @Override
  public File getFile() {
    if (!fileRetrieved) {
      file = fileSet.extractFileToTemp(relativePath);
      fileRetrieved = true;
    }
    return file;
  }
}
