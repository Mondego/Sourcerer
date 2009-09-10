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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;

import edu.uci.ics.sourcerer.repo.IndexedJar;
import edu.uci.ics.sourcerer.repo.base.IJarFile;
import edu.uci.ics.sourcerer.repo.base.Repository;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CompressedJarFile implements IJarFile {
  private Repository repo;
  private String path;
  private long length;
  private String hash;
  
  protected CompressedJarFile(Repository repo, String path, long length, String hash) {
    this.repo = repo;
    this.path = path;
    this.length = length;
    this.hash = hash;
  }

  public String getZipPath() {
    return path;
  }
  
  @Override
  public String getPath() {
    return getFile().getPath();
  }
  
  public File getFile() {
    IndexedJar jar = repo.getJarIndex().getIndexedJar(hash);
    if (jar == null) {
      logger.severe("Unable to locate jar in index: " + path);
      return null;
    } else {
      return jar.getFile();
    }
  }
  
  public String getName() {
    int index = path.lastIndexOf('/');
    if (index == -1) {
      return path;
    } else {
      return path.substring(index + 1);
    }
  }
  
  public long getLength() {
    return length;
  }
  
  @Override
  public String getHash() {
    return hash;
  }
}
