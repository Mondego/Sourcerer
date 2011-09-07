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

import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LocalVariableSet {
  private Map<LocalVariable, Collection<LocalVariableEX>> localVariables;
  
  private LocalVariableSet() {
    localVariables = new EnumMap<>(LocalVariable.class);
  }
  
  static LocalVariableSet make(Collection<LocalVariableEX> localVariables) {
    LocalVariableSet set = new LocalVariableSet();
    for (LocalVariableEX var : localVariables) {
      Collection<LocalVariableEX> list = set.localVariables.get(var.getType());
      if (list == null) {
        list = new LinkedList<>();
        set.localVariables.put(var.getType(), list);
      }
      list.add(var);
    }
    return set;
  }
  
  static void destructiveCompare(ComparisonMismatchReporter reporter, LocalVariableSet setA, LocalVariableSet setB) {
    for (LocalVariable type : LocalVariable.values()) {
      Collection<LocalVariableEX> listA = setA.localVariables.get(type);
      Collection<LocalVariableEX> listB = setB.localVariables.get(type);
      if (listA == null && listB != null) {
        for (LocalVariableEX var : listB) {
          reporter.missingFromA(var);
        }
      } else if (listA != null && listB == null) {
        for (LocalVariableEX var : listA) {
          reporter.missingFromB(var);
        }
      } else if (listA != null && listB != null) {
        for (LocalVariableEX varA : listA) {
          Iterator<LocalVariableEX> iterB = listB.iterator();
          boolean missing = true;
          while (iterB.hasNext()) {
            LocalVariableEX varB = iterB.next();
            if (varA.getType() == LocalVariable.LOCAL) {
              if (varA.getParent().equals(varB.getParent()) && varA.getName().equals(varB.getName())) {
                iterB.remove();
                missing = false;
                break;
              }
            } else if (varA.getType() == LocalVariable.PARAM) {
              if (varA.getParent().equals(varB.getParent()) && varA.getPosition().equals(varB.getPosition())) {
                iterB.remove();
                missing = false;
                break;
              }
            }
          }
          if (missing) {
            reporter.missingFromB(varA);
          }
        }
        for (LocalVariableEX varB : listB) {
          reporter.missingFromA(varB);
        }
      }
    }
  }
}
