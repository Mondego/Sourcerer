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
package edu.uci.ics.sourcerer.tools.java.extractor.missing;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;

/**
 * Currently implemented forms of identification:
 *   import statements
 *   
 * Forms of identification to implement:
 *   Unknown simple names
 *   Missing fully qualified names
 *   Features of missing types, such as methods & fields
 *   
 * @author Joel Ossher (jossher@uci.edu)
 */
class MissingTypeVisitor extends ASTVisitor {
  private final MissingTypeCollection missingTypes;
  
  private MissingTypeVisitor() {
    missingTypes = MissingTypeCollection.create();
  }
  
  static MissingTypeVisitor create() {
    return new MissingTypeVisitor();
  }
  
  MissingTypeCollection getMissingTypes() {
    return missingTypes;
  }
  
  @Override
  public void endVisit(CompilationUnit node) {
    missingTypes.endFile();
  }

  @Override
  public boolean visit(ImportDeclaration node) {
    IBinding binding = node.resolveBinding();
    if (binding == null) {
      missingTypes.addMissingImport(node.getName().getFullyQualifiedName(), node.isOnDemand(), node.isStatic());
    } 
    missingTypes.addImport(node.getName().getFullyQualifiedName(), node.isOnDemand(), node.isStatic());
    return false;
  }
}
