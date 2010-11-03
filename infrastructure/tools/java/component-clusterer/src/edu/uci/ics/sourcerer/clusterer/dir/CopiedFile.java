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
package edu.uci.ics.sourcerer.clusterer.dir;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CopiedFile {
  private int copied30 = 0;
  private int copied50 = 0;
  private int copied80 = 0;
  
  public CopiedFile() {}
  
  protected void increment30() {
    copied30++;
  }
  
  protected void increment50() {
    copied30++;
    copied50++;
  }
  
  protected void increment80() {
    copied30++;
    copied50++;
    copied80++;
  }
  
  public boolean matches30() {
    return copied30 > 0;
  }
  
  public boolean matches50() {
    return copied50 > 0;
  }
  
  public boolean matches80() {
    return copied80 > 0;
  }
  
  public int get30() {
    return copied30;
  }
  
  public int get50() {
    return copied50;
  }
  
  public int get80() {
    return copied80;
  }
}
