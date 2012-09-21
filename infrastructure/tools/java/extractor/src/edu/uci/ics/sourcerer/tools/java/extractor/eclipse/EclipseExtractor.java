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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.Closeable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.BinaryType;

import edu.uci.ics.sourcerer.tools.java.extractor.bytecode.ASMExtractor;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.WriterBundle;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFile;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("restriction")
public class EclipseExtractor implements Closeable {
  private final ASTParser parser;
  private final WriterBundle writers;
  private final ReferenceExtractorVisitor visitor;
  private final ASMExtractor asmExtractor;
  private final ClassFileExtractor eclipseExtractor;
  
  public EclipseExtractor(WriterBundle bundle) {
    this(bundle, null);
  }
  
  public EclipseExtractor(WriterBundle writers, ASMExtractor asmExtractor) {
    this.writers = writers;
    parser = ASTParser.newParser(AST.JLS4);
    visitor = new ReferenceExtractorVisitor(writers);
    if (asmExtractor == null) {
      this.asmExtractor = null;
      this.eclipseExtractor = new ClassFileExtractor(writers);
    } else {
      this.asmExtractor = asmExtractor;
      this.eclipseExtractor = null;
    }
  }
  
  @Override
  public void close() {
    IOUtils.close(writers);
  }
  
  private void extractClassFile(IClassFile classFile) {
    if (asmExtractor == null) {
      eclipseExtractor.extractClassFile(classFile);
    } else {
      try {
        asmExtractor.extract(classFile.getBytes());
      } catch (JavaModelException e) {
        logger.log(Level.SEVERE, "Unable to get bytecode for " + classFile.getElementName(), e);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error extracting bytecode for " + classFile.getElementName(), e);
      }
    }
  }

  public boolean extractClassFiles(Collection<IClassFile> classFiles) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Extracting " + classFiles.size() + " class files", "class files extracted", 500);
    boolean oneWithSource = false;
    
    Map<String, Collection<IClassFile>> memberMap = new HashMap<>();;
    Set<String> sourceFailed = new HashSet<>();
    
    Collection<IClassFile> parentTypes = new LinkedList<>();
    for (IClassFile classFile : classFiles) {
      IType type = classFile.getType();
      try {
        if (type.isMember() || type.isAnonymous() || type.isLocal()) {
          String key = null;
          IType dec = type.getDeclaringType();
          if (dec == null) {
            key = classFile.getElementName();
            int dollar = key.indexOf('$');
            if (dollar == -1) {
              if (classFile.getSource() == null) {
                // Doesn't matter, just make it a parentType
                parentTypes.add(classFile);
              } else {
                logger.log(Level.SEVERE, "Should have a dollar: " + key);
              }
            } else {
              key = key.substring(0, dollar) + ".class";
            }
          } else {
            key = dec.getClassFile().getElementName(); 
          }
          if (key != null) {
            Collection<IClassFile> members = memberMap.get(key);
            if (members == null) {
              members = new LinkedList<>();
              memberMap.put(key, members);
            }
            members.add(classFile);
          }
        } else {
          parentTypes.add(classFile);
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, classFile.getType().getFullyQualifiedName(), e);
        sourceFailed.add(classFile.getType().getFullyQualifiedName());
        extractClassFile(classFile);
      }
    }
    
    for (IClassFile classFile : parentTypes) {
      task.progress();
      try {
        IBuffer buffer = classFile.getBuffer();
        if (buffer == null || buffer.getLength() == 0) {
          extractClassFile(classFile);
          sourceFailed.add(classFile.getType().getFullyQualifiedName());
        } else {
          IType type = classFile.getType();
          // Handle Eclipse issue with GSSUtil
          if ("sun.security.jgss.GSSUtil".equals(type.getFullyQualifiedName())) {
            extractClassFile(classFile);
            sourceFailed.add(classFile.getType().getFullyQualifiedName());
            continue;
          }
          // Handle multiple top-level types
          {
            BinaryType bType = (BinaryType) type;
            String sourceFile = type.getPackageFragment().getElementName() + "." + bType.getSourceFileName(null);
            String fqn = classFile.getType().getFullyQualifiedName() + ".java";
            if (!fqn.equals(sourceFile)) {
              continue;
            }
          }
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
              foundProblem = true;
            }
          }
          if (foundProblem) {
            logger.log(Level.WARNING, "Giving up on " + classFile.getElementName());
            continue;
          }
              
          boolean trouble = checkForMissingTypes(unit);
          if (trouble) {
            sourceFailed.add(classFile.getType().getFullyQualifiedName());
            extractClassFile(classFile);
          } else {
            try {
              visitor.setCompilationUnitSource(classFile.getSource());
              visitor.setAdvisor(NamingAdvisor.create(classFile, memberMap.get(classFile.getElementName())));
              unit.accept(visitor);
              oneWithSource = true;
            } catch (Exception e) {
              logger.log(Level.SEVERE, "Error in extracting " + classFile.getElementName(), e);
//              for (IProblem problem : unit.getProblems()) {
//                if (problem.isError()) {
//                  logger.log(Level.SEVERE, "Error in source for class file (" + classFile.getElementName() + "): " + problem.getMessage());
//                }
//              }
              sourceFailed.add(classFile.getType().getFullyQualifiedName());
              extractClassFile(classFile);
            }
          }
        }
      } catch (JavaModelException | ClassCastException | IllegalArgumentException | NullPointerException e) {
        logger.log(Level.SEVERE, classFile.getElementName(), e);
        sourceFailed.add(classFile.getType().getFullyQualifiedName());
        extractClassFile(classFile);
      }
    }
    
    for (String failed : sourceFailed) {
      Collection<IClassFile> members = memberMap.get(failed);
      if (members != null) {
        for (IClassFile classFile : members) {
          extractClassFile(classFile);
        }
      }
    }
    task.finish();
    return oneWithSource;
  }
  
  public void extractSourceFiles(Map<JavaFile, IFile> sourceFiles) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Extracting " + sourceFiles.size() + " source files", "sources files extracted", 500);

    ReferenceExtractorVisitor visitor = new ReferenceExtractorVisitor(writers);
    for (Map.Entry<JavaFile, IFile> entry : sourceFiles.entrySet()) {
      IFile file = entry.getValue();
      // May put this in if there are still problems
//      EclipseUtils.setCharacterSet(file);
      ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);

      parser.setStatementsRecovery(true);
      parser.setResolveBindings(true);
      parser.setBindingsRecovery(true);
      parser.setSource(icu);
  
      CompilationUnit unit = null;
      try {
        unit = (CompilationUnit)parser.createAST(null);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error in creating AST for " + entry.getKey(), e);
        continue;
      }
  
      visitor.setBindingFreeMode(checkForMissingTypes(unit));
      
      try {
        visitor.setCompilationUnitSource(icu.getSource());
        visitor.setJavaFile(entry.getKey());
        visitor.setAdvisor(NamingAdvisor.create());
        unit.accept(visitor);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error in extracting " + entry.getKey(), e);
      }
      
      task.progress();
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