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

import java.util.Collection;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class UsedJarExParser implements ModelExParser<UsedJarEX> {
  private UsedJarExParser() {}
  
  public static UsedJarExParser getParser() {
    return new UsedJarExParser();
  }
  
  public static String getLine(String hash, String ... missingTypes) {
    StringBuilder line = new StringBuilder(hash);
    for (String missingType : missingTypes) {
      line.append(" ").append(missingType);
    }
    return line.toString();
  }
  
  @Override
  public UsedJarEX parseLine(String line) {
    String parts[] = line.split(" ");
    Collection<String> missingTypes = Helper.newLinkedList();
    for (int i = 1; i < parts.length; i++) {
      missingTypes.add(parts[i]);
    }
    return new UsedJarEX(parts[0], missingTypes);
  }
}
