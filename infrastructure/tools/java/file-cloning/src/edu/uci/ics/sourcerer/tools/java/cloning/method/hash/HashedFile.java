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
package edu.uci.ics.sourcerer.tools.java.cloning.method.hash;

import edu.uci.ics.sourcerer.util.io.LWField;
import edu.uci.ics.sourcerer.util.io.LineWriteable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class HashedFile implements LineWriteable {
  @LWField private String project;
  @LWField private String path;
  @LWField private String md5;
  @LWField private String sha;
  @LWField private long length;
  
  protected HashedFile() {}
  
  protected void set(String project, String path, String md5, String sha, long length) {
    this.project = project;
    this.path = path;
    this.md5 = md5;
    this.sha = sha;
    this.length = length;
  }
  
  public String getProject() {
    return project;
  }
  
  public String getPath() {
    return path;
  }
  
  public String getMd5() {
    return md5;
  }

  public String getSha() {
    return sha;
  }

  public long getLength() {
    return length;
  }
}
