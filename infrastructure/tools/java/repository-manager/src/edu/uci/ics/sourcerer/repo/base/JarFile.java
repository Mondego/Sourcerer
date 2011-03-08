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
package edu.uci.ics.sourcerer.repo.base;

import edu.uci.ics.sourcerer.repo.general.RepoFile;
import edu.uci.ics.sourcerer.util.io.LWField;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarFile implements IJarFile {
  @LWField
  private String hash;
  @LWField
  private RepoFile file;

  protected JarFile() {}
  
  public JarFile(String hash, RepoFile file) {
    this.hash = hash;
    this.file = file;
  }

  @Override
  public String getHash() {
    return hash;
  }
  
  @Override
  public RepoFile getFile() {
    return file;
  }
  
  @Override
  public String toString() {
    return file.getName();
  }
}
