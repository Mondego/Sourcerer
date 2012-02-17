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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFile;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MissingTypeIdentifier {
  private final ASTParser parser;
  
  private MissingTypeIdentifier() {
    parser = ASTParser.newParser(AST.JLS4);
  }
  
  public static MissingTypeIdentifier create() {
    return new MissingTypeIdentifier();
  }
  
  public MissingTypeCollection identifyMissingTypes(Map<JavaFile, IFile> sourceFiles) {
    MissingTypeVisitor visitor = MissingTypeVisitor.create();
    for (Map.Entry<JavaFile, IFile> entry : sourceFiles.entrySet()) {
      ICompilationUnit icu = JavaCore.createCompilationUnitFrom(entry.getValue());
      
      parser.setStatementsRecovery(true);
      parser.setResolveBindings(true);
      parser.setBindingsRecovery(false);
      parser.setSource(icu);

      CompilationUnit unit = null;
      try {
        unit = (CompilationUnit) parser.createAST(null);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error in creating AST for " + entry.getKey(), e);
        continue;
      }
      
      try {
        unit.accept(visitor);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error in analyzing " + entry.getKey(), e);
      }
    }
    return visitor.getMissingTypes();
  }
}
