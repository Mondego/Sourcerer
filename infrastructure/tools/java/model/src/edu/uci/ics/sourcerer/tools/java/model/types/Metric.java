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
package edu.uci.ics.sourcerer.tools.java.model.types;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public enum Metric {
  // Source code metrics
  LINES_OF_CODE,
  COMMENT_LOC,
  CLASS_COMMENT_LOC,
  PARTIAL_COMMENT_LOC,
  NON_WHITESPACE_LOC,
  CODE_LOC,
  
  NUMBER_OF_UNCONDITIONAL_JUMPS,
  NUMBER_OF_NESTED_LEVELS,
  
  // Computed source code metrics
  COMMENT_FREQUENCY,
  CLASS_COMMENT_FREQUENCY,
  
  // FindBugs Metrics
  FB_TOTAL_CLASSES,
  FB_REFERENCED_CLASSES,
  FB_SIZE,
  FB_BUGS,
  FB_PRIORITY_1,
  FB_PRIORITY_2,
  FB_PRIORITY_3,
  
  // Raw bytecode metrics
  BC_CYCLOMATIC_COMPLEXITY,
  BC_NUMBER_OF_STATEMENTS,
  BC_NUMBER_OF_INSTRUCTIONS,
  BC_VOCABULARY_SIZE,
  
  // Computed bytecode metrics
  BC_AVERAGE_SIZE_OF_STATEMENTS,
  BC_WEIGHTED_METHODS_PER_CLASS,
  BC_VOCABULARY_FREQUENCY,
  
  // Computed OO Metrics
  NUMBER_OF_BASE_CLASSES,
  NUMBER_OF_DERIVED_CLASSES,
  RATIO_OF_DERIVED_TO_BASE_CLASSES,
  
  NUMBER_OF_BASE_INTERFACES,
  NUMBER_OF_DERIVED_INTERFACES,
  RATIO_OF_DERIVED_TO_BASE_INTERFACES,
  
  NUMBER_OF_CLASS_CHILDREN,
  NUMBER_OF_DIRECT_CLASS_CHILDREN,
  
  NUMBER_OF_INTERFACE_CHILDREN,
  NUMBER_OF_DIRECT_INTERFACE_CHILDREN,
  
  NUMBER_OF_IMPLEMENTED_INTERFACES,
  NUMBER_OF_SUPER_INTERFACES,
  
  EFFERENT_COUPLING, // Fan out
  EFFERENT_COUPLING_INTERNAL,
  AFFERENT_COUPLING, // Fan in
  AFFERENT_COUPLING_INTERNAL,
  
  DEPTH_OF_INHERITANCE_TREE_CC,
  DEPTH_OF_INHERITANCE_TREE_INTERNAL_CC,
  DEPTH_OF_INHERITANCE_TREE_CI,
  DEPTH_OF_INHERITANCE_TREE_INTERNAL_CI,
  DEPTH_OF_INHERITANCE_TREE_II,
  DEPTH_OF_INHERITANCE_TREE_INTERNAL_II,
  
  LACK_OF_COHESION_F,
  LACK_OF_COHESION_FM,
  LACK_OF_COHESION_D,
  
  RESPONSE_FOR_CLASS,
  ;
}
