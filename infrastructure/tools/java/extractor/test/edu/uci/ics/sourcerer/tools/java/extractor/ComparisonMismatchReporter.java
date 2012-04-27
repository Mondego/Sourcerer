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
package edu.uci.ics.sourcerer.tools.java.extractor;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import edu.uci.ics.sourcerer.tools.java.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ComparisonMismatchReporter {
  private String preText;
  private int baseIndent;
  
  public ComparisonMismatchReporter(int indent, String text) {
    this.preText = text;
    this.baseIndent = indent;
  }
  
  private static final String SPACES = "          ";
  private void log(int indent, String text) {
    if (preText != null) {
      logger.info(SPACES.substring(0, baseIndent) + preText);
      preText = null;
    }
    logger.info(SPACES.substring(0, baseIndent + indent) + text);
  }
  
  private void missing(String text, RelationEX relation) {
    if (relation.getType() == Relation.ANNOTATED_BY && relation.getRhs().equals("java.lang.Override")) {
      
    } else {
      log(2, text + relation);
    }
  }
  
  public void missingFromA(EntityEX entity) {
    log(2, "Repo A is missing " + entity);
  }
  
  public void missingFromA(RelationEX relation) {
    missing("Repo A is missing ", relation);
  }
  
  public void missingFromA(LocalVariableEX var) {
    log(2, "Repo A is missing " + var);
  }
  
  public void missingFromB(EntityEX entity) {
    log(2, "Repo B is missing " + entity);
  }
  
  public void missingFromB(RelationEX relation) {
    missing("Repo B is missing ", relation);
  }
  
  public void missingFromB(LocalVariableEX var) {
    log(2, "Repo B is missing " + var);
  }
}
