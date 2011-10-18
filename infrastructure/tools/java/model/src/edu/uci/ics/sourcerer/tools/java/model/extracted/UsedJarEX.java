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
package edu.uci.ics.sourcerer.tools.java.model.extracted;

import edu.uci.ics.sourcerer.util.io.SimpleSerializable;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class UsedJarEX implements SimpleSerializable {
  public static final Argument<String> USED_JAR_FILE = new StringArgument("used-jar-file", "used-jars.txt", "Filename for used jar files.").permit();
  
  private String hash;
  private String[] missingTypes;
  
  public UsedJarEX() {}
  
  public UsedJarEX(String hash, String ... missingTypes) {
    this.hash = hash;
    this.missingTypes = missingTypes;
  }
  
  public UsedJarEX update(String hash, String ... missingTypes) {
    this.hash = hash;
    this.missingTypes = missingTypes;
    return this;
  }
  
  public String getHash() {
    return hash;
  }
  
  public String[] getMissingTypes() {
    return missingTypes;
  }
}
