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

import edu.uci.ics.sourcerer.extractor.io.IMissingTypeWriter;
import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FeatureExtractor {
  public static final Property<Boolean> PPA = new BooleanProperty("ppa", false, "Extractor", "Do partial program analysis.");
  
  private ASTParser parser;
  private WriterBundle bundle;
  
  public FeatureExtractor(WriterBundle bundle) {
    this.bundle = bundle;
    parser = ASTParser.newParser(AST.JLS3);
  }
 
  public void close() {
    bundle.close();
  }
  
  public static class ClassExtractionReport extends SourceExtractionReport {
    private int extractedFromBinary = 0;
    private int binaryExtractionExceptions = 0;
    
    private ClassExtractionReport() {}
    
    protected void reportBinaryExtraction() {
      extractedFromBinary++;
    }
    
    protected void reportBinaryExtractionException() {
      binaryExtractionExceptions++;
    }
   
    public int getExtractedFromBinary() {
      return extractedFromBinary;
    }
    
    public int getBinaryExtractionExceptions() {
      return binaryExtractionExceptions;
    }
  }
  
  public ClassExtractionReport extractClassFiles(Collection<IClassFile> classFiles) {
    ClassExtractionReport report = new ClassExtractionReport();
    ClassFileExtractor extractor = new ClassFileExtractor(bundle);
    ReferenceExtractorVisitor visitor = new ReferenceExtractorVisitor(bundle);
    IMissingTypeWriter missingTypeWriter = bundle.getMissingTypeWriter();
    for (IClassFile classFile : classFiles) {
      try {
        if (ClassFileExtractor.isTopLevel(classFile)) {
          ISourceRange source = classFile.getSourceRange();
          
          if (source == null || source.getLength() == 0) {
            source = classFile.getSourceRange();
            if (source == null || source.getLength() == 0) {
              extractor.extractClassFile(classFile);
              report.reportBinaryExtraction();
            }
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
                  foundProblem = true;
                }
              }
              if (foundProblem) {
                continue;
              }
              
              checkForMissingTypes(unit, report, missingTypeWriter);
              if (!report.hadMissingType()) {
                try {
                  unit.accept(visitor);
                  report.reportSourceExtraction();
                } catch (Exception e) {
                  logger.log(Level.SEVERE, "Error in extracting " + classFile.getElementName(), e);
                  for (IProblem problem : unit.getProblems()) {
                    if (problem.isError()) {
                      logger.log(Level.SEVERE, "Error in source for class file (" + classFile.getElementName() + "): " + problem.getMessage());
                    }
                  }
                  report.reportSourceExtractionException();
                  try {
                    extractor.extractClassFile(classFile);
                    report.reportBinaryExtraction();
                  } catch (Exception e2) {
                    logger.log(Level.SEVERE, "Unable to extract " + classFile.getElementName(), e2);
                    report.reportBinaryExtractionException();
                  }
                }
              }
            } catch (Exception e) {
              logger.log(Level.SEVERE, "Error in extracting " + classFile.getElementName(), e);
              extractor.extractClassFile(classFile);
              report.reportBinaryExtraction();
            }
          }
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Unable to extract " + classFile.getElementName(), e);
        report.reportBinaryExtractionException();
      }
    }
    return report;
  }
   
  public static class SourceExtractionReport {
    private boolean missingType = false;
    private int extractedFromSource = 0;
    private int sourceExtractionExceptions = 0;
    
    private SourceExtractionReport() {}
    
    protected void reportMissingType() {
      missingType = true;
    }
   
    protected void reportSourceExtraction() {
      extractedFromSource++;
    }
    
    protected void reportSourceExtractionException() {
      sourceExtractionExceptions++;
    }
    
    public boolean hadMissingType() {
      return missingType;
    }
    
    public int getExtractedFromSource() {
      return extractedFromSource;
    }
    
    public int getSourceExtractionExceptions() {
      return sourceExtractionExceptions;
    }
  }
  
  public void extractSourceFiles(Collection<IFile> sourceFiles) {
    ReferenceExtractorVisitor visitor = new ReferenceExtractorVisitor(bundle);
    SourceExtractionReport report = new SourceExtractionReport();
    IMissingTypeWriter missingTypeWriter = bundle.getMissingTypeWriter();
    
    for (IFile source : sourceFiles) {
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
        report.reportSourceExtractionException();
        continue;
      }
      
      checkForMissingTypes(unit, report, missingTypeWriter);
      if (!report.hadMissingType()) {
        try {
          unit.accept(visitor);
          report.reportSourceExtraction();
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Error in extracting " + source.getName(), e);
          report.reportSourceExtractionException();
        }
      }
    }
  }
  
  private void checkForMissingTypes(CompilationUnit unit, SourceExtractionReport report, IMissingTypeWriter writer) {
    // Check for the classpath problem
    for (IProblem problem : unit.getProblems()) {
      if (problem.isError() && (problem.getID() == IProblem.IsClassPathCorrect || problem.getID() == IProblem.ImportNotFound)) {
        writer.writeMissingType(problem.getArguments()[0]);
        report.reportMissingType();
      }
    }
  }
  
//  @SuppressWarnings("unchecked")
//  private boolean reattemptCompilation(CompilationUnit unit) {
//    IProblem[] problems = unit.getProblems();
//    if (problems.length == 0) {
//      return false;
//    } else {
//      boolean foundIndirectRef = false;
//      Collection<String> missingPrefix = Helper.newHashSet();
//      Collection<String> missingRefs = Helper.newHashSet();
//      // Check problems for missing indirect references
//      for (IProblem problem : problems) {
//        if (problem.isError()) {
//          String message = problem.getMessage();
//          if (message.startsWith("The type") && message.endsWith("is indirectly referenced from required .class files")) {
//            foundIndirectRef = true;
//            missingRefs.add(problem.getArguments()[0]);
//          } else if (message.startsWith("The import") && message.endsWith("cannot be resolved")) {
//            missingPrefix.add(problem.getArguments()[0]);
//          }
//        }
//      }
//      
//      // Check for unresolved imports
//      for (ImportDeclaration imp : (List<ImportDeclaration>)unit.imports()) {
//        try {
//          String name = imp.getName().getFullyQualifiedName();
//          IBinding binding = imp.resolveBinding();
//          if (binding == null) {
//            // if there was a missing indirect reference, all bindings will be null
//            if (foundIndirectRef) {
//              if (isPrefix(missingPrefix, name)) {
//                missingRefs.add(name);
//              }
//            } else {
//              if (isPrefix(missingPrefix, name)) {
//                // should be unresolved
//                missingRefs.add(name);
//              } else {
//                logger.log(Level.SEVERE, "This reference should have a missing prefix: " + name);
//              }
//            }
//          } else {
//            if (!binding.getJavaElement().exists()) {
//              if (isPrefix(missingPrefix, name)) {
//                // should be unresolved
//                missingRefs.add(name);
//              } else {
//                logger.log(Level.SEVERE, "This reference should have a missing prefix: " + name);
//              }
//            }
//          }
//        } catch (Exception e) {
//          logger.log(Level.SEVERE, "Exception in getting binding!", e);
//          String name = imp.getName().getFullyQualifiedName();
//          if (isPrefix(missingPrefix, name)) {
//            // should be unresolved
//            missingRefs.add(name);
//          } else {
//            logger.log(Level.SEVERE, "This reference should have a missing prefix: " + name);
//          }
//        }
//        
//        // Let's find the jars that work
//        return false;
//      }
//      return false;
//    }
//  }
  
//  private boolean isPrefix(Collection<String> prefixes, String word) {
//    for (String prefix : prefixes) {
//      if (word.startsWith(prefix)) {
//        return true;
//      }
//    }
//    return false;
//  }
}
