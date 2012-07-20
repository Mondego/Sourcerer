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
package edu.uci.ics.sourcerer.tools.java.db.type;

import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifiers;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ModeledMethod extends ModeledStructuralEntity {
  private final String params;
  private final String rawParams;

  ModeledMethod(Integer entityID, Modifiers mods, String fqn, Entity type, Integer fileID, Integer projectID, String params, String rawParams) {
    super(entityID, mods, fqn, type, fileID, projectID);
    this.params = params;
    this.rawParams = rawParams;
  }
  
  public String getParams() {
    return params;
  }
  
  public String getRawParams() {
    return rawParams;
  }
}
