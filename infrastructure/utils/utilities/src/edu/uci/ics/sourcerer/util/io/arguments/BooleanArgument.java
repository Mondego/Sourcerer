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
package edu.uci.ics.sourcerer.util.io.arguments;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.logging.Level;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class BooleanArgument extends Argument<Boolean> {
  public BooleanArgument(String name, Boolean defaultValue, String description) {
    super(name, defaultValue, description);
  }

  @Override
  public String getType() {
    return "bool";
  }
  
  @Override
  protected Boolean parseString(String value) {
    if ("true".equals(value)) {
      return true;
    } else if ("false".equals(value)) {
      return false;
    } else {
      logger.log(Level.SEVERE, value + " is not a valid boolean value for " + getName());
      return null;
    }
  }
}
