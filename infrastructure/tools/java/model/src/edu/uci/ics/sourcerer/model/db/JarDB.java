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
package edu.uci.ics.sourcerer.model.db;

public class JarDB {
  private String jarID;
  private String hash;
  private String name;
  private String group;
  private String version;
  private boolean hasSource;
  
  public JarDB(String jarID, String hash, String name, String group, String version, boolean hasSource) {
    this.jarID = jarID;
    this.hash = hash;
    this.name = name;
    this.group = group;
    this.version = version;
    this.hasSource = hasSource;
  }

  public String getJarID() {
    return jarID;
  }

  public String getHash() {
    return hash;
  }

  public String getName() {
    return name;
  }

  public String getGroup() {
    return group;
  }

  public String getVersion() {
    return version;
  }

  public boolean hasSource() {
    return hasSource;
  }
}
