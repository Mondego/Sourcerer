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
package edu.uci.ics.sourcerer.extractor.ast;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FeatureExtractor {
  public static final Property<Boolean> PPA = new BooleanProperty("ppa", false, "Extractor", "Do partial program analysis.");
  
  private ASTParser parser;
  private WriterBundle bundle;
  
  public FeatureExtractor() {
    parser = ASTParser.newParser(AST.JLS3);
  }
  
  public void setBundle(WriterBundle bundle) {
    if (this.bundle != null) {
      this.bundle.close();
    }
    this.bundle = bundle;
  }
  
  public void close() {
    bundle.close();
  }
  
  public void extractClassFiles(Collection<IClassFile> classFiles) {
    ClassFileExtractor extractor = new ClassFileExtractor(bundle);
    ReferenceExtractorVisitor visitor = new ReferenceExtractorVisitor(bundle);
    for (IClassFile classFile : classFiles) {
      try {
        ISourceRange source = classFile.getSourceRange();
        if (source == null || source.getLength() == 0) {
          extractor.extractClassFile(classFile);
        } else {
          parser.setStatementsRecovery(true);
          parser.setResolveBindings(true);
          parser.setBindingsRecovery(true);
          parser.setSource(classFile);
          
          CompilationUnit unit = (CompilationUnit) parser.createAST(null);
          unit.accept(visitor);
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Unable to extract " + classFile.getElementName(), e);
      }
    }
  }
   
  public void extractSourceFiles(Collection<IFile> sourceFiles) {
    ReferenceExtractorVisitor visitor = new ReferenceExtractorVisitor(bundle);
    
    int total = 0;
    
    for (IFile source : sourceFiles) {
//      if (doPPA) {
////        PPAUtil.getCU(source, new PPAOptions()).accept(visitor);
//      } else {
        ICompilationUnit icu = JavaCore.createCompilationUnitFrom(source);
        
        parser.setStatementsRecovery(true);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setSource(icu);
        
        try {
          CompilationUnit unit;
          do {
            unit = (CompilationUnit)parser.createAST(null);
          } while (reattemptCompilation(unit));
          unit.accept(visitor);
        } catch (NullPointerException e) {
          logger.log(Level.SEVERE, "Unable to create AST for " + icu.getResource().getLocation().toString(), e);
          total--;
        }
      }
      
      if (++total % 1000 == 0) {
        logger.info(total + " files extracted");
      }
//    }
    
    logger.info(total + " files extracted");
  }
  
  @SuppressWarnings("unchecked")
  private boolean reattemptCompilation(CompilationUnit unit) {
    IProblem[] problems = unit.getProblems();
    if (problems.length == 0) {
      return false;
    } else {
      boolean foundIndirectRef = false;
      Collection<String> missingPrefix = Helper.newHashSet();
      Collection<String> missingRefs = Helper.newHashSet();
      // Check problems for missing indirect references
      for (IProblem problem : problems) {
        if (problem.isError()) {
          String message = problem.getMessage();
          if (message.startsWith("The type") && message.endsWith("is indirectly referenced from required .class files")) {
            foundIndirectRef = true;
            missingRefs.add(problem.getArguments()[0]);
          } else if (message.startsWith("The import") && message.endsWith("cannot be resolved")) {
            missingPrefix.add(problem.getArguments()[0]);
          }
        }
      }
      
      // Check for unresolved imports
      for (ImportDeclaration imp : (List<ImportDeclaration>)unit.imports()) {
        try {
          String name = imp.getName().getFullyQualifiedName();
          IBinding binding = imp.resolveBinding();
          if (binding == null) {
            // if there was a missing indirect reference, all bindings will be null
            if (foundIndirectRef) {
              if (isPrefix(missingPrefix, name)) {
                missingRefs.add(name);
              }
            } else {
              if (isPrefix(missingPrefix, name)) {
                // should be unresolved
                missingRefs.add(name);
              } else {
                logger.log(Level.SEVERE, "This reference should have a missing prefix: " + name);
              }
            }
          } else {
            if (!binding.getJavaElement().exists()) {
              if (isPrefix(missingPrefix, name)) {
                // should be unresolved
                missingRefs.add(name);
              } else {
                logger.log(Level.SEVERE, "This reference should have a missing prefix: " + name);
              }
            }
          }
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Exception in getting binding!", e);
          String name = imp.getName().getFullyQualifiedName();
          if (isPrefix(missingPrefix, name)) {
            // should be unresolved
            missingRefs.add(name);
          } else {
            logger.log(Level.SEVERE, "This reference should have a missing prefix: " + name);
          }
        }
        
        // Let's find the jars that work
        return false;
      }
      return false;
    }
  }
  
  private boolean isPrefix(Collection<String> prefixes, String word) {
    for (String prefix : prefixes) {
      if (word.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }
}
