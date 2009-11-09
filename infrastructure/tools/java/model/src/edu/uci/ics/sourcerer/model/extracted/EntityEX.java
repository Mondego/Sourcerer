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
package edu.uci.ics.sourcerer.model.extracted;

import edu.uci.ics.sourcerer.model.Entity;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class EntityEX implements ModelEX {
  private Entity type;
  private String fqn;
  private String mods;
  private String path;
  private String startPos;
  private String length;
  
  protected EntityEX(Entity type, String fqn, String mods) {
    this(type, fqn, mods, null, null, null);
  }
  
  protected EntityEX(Entity type, String fqn, String mods, String path, String offset, String length) {
    this.type = type;
    this.fqn = fqn;
    this.mods = mods;
    this.path = path;
    this.startPos = offset;
    this.length = length;
  }
  
  public Entity getType() {
    return type;
  }

  public String getFqn() {
    return fqn;
  }

  public String getMods() {
    return mods;
  }

  public String getPath() {
    return path;
  }

  public String getStartPosition() {
    return startPos;
  }

  public String getLength() {
    return length;
  }
  
  public String toString() {
    return type + " " + fqn;
  }
}
