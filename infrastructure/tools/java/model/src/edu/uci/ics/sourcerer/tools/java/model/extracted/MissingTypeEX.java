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
public final class MissingTypeEX implements SimpleSerializable {
  public static final Argument<String> MISSING_TYPE_FILE = new StringArgument("missing-type-file", "missing-types.txt", "Filename for missing types.").permit();
  
  private String fqn;
  
  public MissingTypeEX() {}
  
  public MissingTypeEX(String fqn) {
    this.fqn = fqn;
  }
  
  public MissingTypeEX update(String fqn) {
    this.fqn = fqn;
    return this;
  }
  
  public String getFqn() {
    return fqn;
  }
}
