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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.logging.Level;

import edu.uci.ics.sourcerer.model.Relation;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RelationExJarParser implements ModelExParser<RelationEX> {
private RelationExJarParser() {}
  
  public static RelationExJarParser getParser() {
    return new RelationExJarParser();
  }
  
  public static String getLine(Relation type, String lhs, String rhs) {
    return type.name() + " " + lhs + " " + rhs;
  }
  
  public static String getLineParametrizedBy(String lhs, String rhs, int position) {
    return Relation.PARAMETRIZED_BY + " " + lhs + " " + rhs + " " + position;
  }
  
  @Override
  public RelationEX parseLine(String line) {
    String[] parts = line.split(" ");
    
    try {
      Relation type = Relation.valueOf(parts[0]);
      if (type == Relation.PARAMETRIZED_BY) {
        return RelationEX.getJarParametrizedByRelation(parts[1], parts[2], parts[3]);
      } else {
        return RelationEX.getJarRelation(type, parts[1], parts[2]);
      }
    } catch (IllegalArgumentException e) {
      logger.log(Level.SEVERE, "Unable to parse line: " + line, e);
      return null;
    }
  }
}
