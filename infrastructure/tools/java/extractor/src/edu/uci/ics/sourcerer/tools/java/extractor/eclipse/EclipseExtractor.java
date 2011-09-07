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
package edu.uci.ics.sourcerer.tools.java.extractor.eclipse;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.Closeable;
import java.util.Collection;
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
import org.eclipse.jdt.internal.core.BinaryType;

import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("restriction")
public class EclipseExtractor implements Closeable {
  private TaskProgressLogger task;
  private ASTParser parser;
  private WriterBundle bundle;
  
  public EclipseExtractor(TaskProgressLogger task, WriterBundle bundle) {
    this.task = task;
    this.bundle = bundle;
    parser = ASTParser.newParser(AST.JLS3);
  }
  
  @Override
  public void close() {
    IOUtils.close(bundle);
  }
  
  public boolean extractClassFiles(Collection<IClassFile> classFiles) {
    task.start("Extracting " + classFiles.size() + " class files", "class files extracted", 500);
    boolean oneWithSource = false;
    ClassFileExtractor extractor = new ClassFileExtractor(bundle);
    ReferenceExtractorVisitor visitor = new ReferenceExtractorVisitor(bundle);
    for (IClassFile classFile : classFiles) {
      task.progress();
      try {
        if (ClassFileExtractor.isTopLevelOrAnonymous(classFile)) {
          ISourceRange source = classFile.getSourceRange();
          
          boolean hasSource = true; 
          if (source == null || source.getLength() == 0) {
            source = classFile.getSourceRange();
            if (source == null || source.getLength() == 0) {
              hasSource = false;
            }
          }
          
          if (hasSource) {
            // Verify that the source file matches the binary file
            BinaryType type = (BinaryType)classFile.getType();
            if (type.isAnonymous()) {
              continue;
            }
            String sourceFile = type.getPackageFragment().getElementName() + "." + type.getSourceFileName(null);
            String fqn = classFile.getType().getFullyQualifiedName() + ".java";
            if (!fqn.equals(sourceFile)) {
              logger.log(Level.WARNING, "Source fqn mismatch: " + sourceFile + " " + fqn);
              continue;
            }
          }
          
          if (!hasSource) {
            extractor.extractClassFile(classFile);
          } else {
            try {
              parser.setStatementsRecovery(true);
              parser.setResolveBindings(true);
              parser.setBindingsRecovery(true);
              parser.setSource(classFile);
              
              CompilationUnit unit = (CompilationUnit) parser.createAST(null);
              boolean foundProblem = false;
              // start by checking for a "public type" error
              // just skip this unit in if one is found 
              for (IProblem problem : unit.getProblems()) {
                if (problem.isError() && problem.getID() == IProblem.PublicClassMustMatchFileName) {
                  logger.info("umm");
                  foundProblem = true;
                }
              }
              if (foundProblem) {
                continue;
              }
              
//              boolean secondOrder = checkForMissingTypes(unit, report, missingTypeWriter);
                visitor.setBindingFreeMode(checkForMissingTypes(unit));
                try {
                  unit.accept(visitor);
                  oneWithSource = true;
//                  report.reportSourceExtraction();
                } catch (Exception e) {
                  logger.log(Level.SEVERE, "Error in extracting " + classFile.getElementName(), e);
                  for (IProblem problem : unit.getProblems()) {
                    if (problem.isError()) {
                      logger.log(Level.SEVERE, "Error in source for class file (" + classFile.getElementName() + "): " + problem.getMessage());
                    }
                  }
                  try {
                    extractor.extractClassFile(classFile);
                  } catch (Exception e2) {
                    logger.log(Level.SEVERE, "Unable to extract " + classFile.getElementName(), e2);
                  }
                }
//                visitor.setBindingFreeMode(false);
//              }
            } catch (Exception e) {
              logger.log(Level.SEVERE, "Error in extracting " + classFile.getElementName(), e);
              extractor.extractClassFile(classFile);
            }
          }
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Unable to extract " + classFile.getElementName(), e);
      }
    }
    task.finish();
    return oneWithSource;
  }
  
  public void extractSourceFiles(Collection<IFile> sourceFiles) {
    task.start("Extracting " + sourceFiles.size() + " source files", "sources files extracted", 500);

    ReferenceExtractorVisitor visitor = new ReferenceExtractorVisitor(bundle);
    for (IFile source : sourceFiles) {
      task.progress();
      ICompilationUnit icu = JavaCore.createCompilationUnitFrom(source);

      parser.setStatementsRecovery(true);
      parser.setResolveBindings(true);
      parser.setBindingsRecovery(true);
      parser.setSource(icu);
  
      CompilationUnit unit = null;
      try {
        unit = (CompilationUnit)parser.createAST(null);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error in creating AST for " + source.getName(), e);
        continue;
      }
  
      visitor.setBindingFreeMode(checkForMissingTypes(unit));
      
      try {
        visitor.setCompilationUnitSource(icu.getSource());
        unit.accept(visitor);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error in extracting " + source.getName(), e);
      }
    }
    task.finish();
  }
  
  private boolean checkForMissingTypes(CompilationUnit unit) {
    // Check for the classpath problem
    for (IProblem problem : unit.getProblems()) {
      if (problem.isError()) {
        if (problem.getID() == IProblem.IsClassPathCorrect) {
          return true;
        }
      }
    }
    return false;
  }
}
