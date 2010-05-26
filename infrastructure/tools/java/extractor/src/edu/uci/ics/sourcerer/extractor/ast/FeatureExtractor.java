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
import java.util.Map;
import java.util.Set;
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
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.internal.core.BinaryType;

import edu.uci.ics.sourcerer.extractor.Extractor;
import edu.uci.ics.sourcerer.extractor.io.IMissingTypeWriter;
import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("restriction")
public final class FeatureExtractor {
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
    private boolean sourceSkipped = false;
    private int extractedFromBinary = 0;
    private int binaryExtractionExceptions = 0;
    
    private ClassExtractionReport() {}
    
    protected void reportBinaryExtraction() {
      extractedFromBinary++;
    }
    
    protected void reportBinaryExtractionException() {
      binaryExtractionExceptions++;
    }
    
    protected void reportSourceSkipped() {
      sourceSkipped = true;
    }
   
    public int getExtractedFromBinary() {
      return extractedFromBinary;
    }
    
    public int getBinaryExtractionExceptions() {
      return binaryExtractionExceptions;
    }
    
    public boolean sourceSkipped() {
      return sourceSkipped;
    }
  }
  
  public ClassExtractionReport extractClassFiles(Collection<IClassFile> classFiles, boolean force) {
    ClassExtractionReport report = new ClassExtractionReport();
    ClassFileExtractor extractor = new ClassFileExtractor(bundle);
    ReferenceExtractorVisitor visitor = new ReferenceExtractorVisitor(bundle);
    IMissingTypeWriter missingTypeWriter = bundle.getMissingTypeWriter();
    
    for (IClassFile classFile : classFiles) {
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
          
          if (!hasSource || Extractor.EXTRACT_BINARY.getValue()) {
            extractor.extractClassFile(classFile);
            report.reportBinaryExtraction();
            if (hasSource) {
              report.reportSourceSkipped();
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
              if (report.hadMissingSecondOrder() && force) {
                extractor.extractClassFile(classFile);
                report.reportBinaryExtraction();
              } else if (!report.hadMissingSecondOrder() && (force || !report.hadMissingType())) {
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
    private boolean missingSecondOrder = false;
    private int extractedFromSource = 0;
    private int sourceExtractionExceptions = 0;
    
    public SourceExtractionReport() {}
    
    protected void reportMissingType() {
      missingType = true;
    }
    
    protected void reportMissingSecondOrder() {
      missingSecondOrder = true;
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
    
    public boolean hadMissingSecondOrder() {
      return missingSecondOrder;
    }
    
    public int getExtractedFromSource() {
      return extractedFromSource;
    }
    
    public int getSourceExtractionExceptions() {
      return sourceExtractionExceptions;
    }
  }
  
  public SourceExtractionReport extractSourceFiles(SourceExtractionReport report, Collection<IFile> sourceFiles, boolean force) {
    ReferenceExtractorVisitor visitor = new ReferenceExtractorVisitor(bundle);
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
      if (!report.hadMissingType() || force) {
        if (report.hadMissingSecondOrder()) {
          logger.warning("Skipping extraction of " + source.getName() + " because of missing second order type.");
        } else {
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
    return report;
  }
  
  @SuppressWarnings("unchecked")
  private void checkForMissingTypes(CompilationUnit unit, SourceExtractionReport report, IMissingTypeWriter writer) {
    Set<String> onDemandImports = Helper.newHashSet();
    Map<String, String> singleTypeImports = Helper.newHashMap();
    Set<String> simpleNames = Helper.newHashSet();
    
    // Check for the classpath problem
    for (IProblem problem : unit.getProblems()) {
      if (problem.isError()) {
        if (problem.getID() == IProblem.IsClassPathCorrect) {
          writer.writeMissingType(problem.getArguments()[0]);
          report.reportMissingSecondOrder();
        } else if (problem.getID() == IProblem.ImportNotFound) {
          String prefix = problem.getArguments()[0];
          // Go and find all the imports with this prefix
          boolean found = false;
          for (ImportDeclaration imp : (List<ImportDeclaration>)unit.imports()) {
            String fqn = imp.getName().getFullyQualifiedName();
            if (fqn.startsWith(prefix)) {
              if (imp.isOnDemand()) {
                onDemandImports.add(fqn);
              } else {
                String simpleName = fqn.substring(fqn.lastIndexOf('.') + 1);
                String oldFqn = singleTypeImports.get(simpleName);
                if (oldFqn != null && !oldFqn.equals(fqn)) {
                  logger.log(Level.SEVERE, "Two fqns with the same simple name: " + fqn + " and " + oldFqn);
                } else {
                  singleTypeImports.put(simpleName, fqn);
                }
              }
//              writer.writeMissingType(imp.getName().getFullyQualifiedName());
              found = true;
            }
          }
          if (!found) {
            logger.log(Level.SEVERE, "Unable to find import matching: " + prefix);
            writer.writeMissingType(prefix);
          }
          report.reportMissingType();
        } else if (problem.getID() == IProblem.UndefinedType) {
          simpleNames.add(problem.getArguments()[0]);
          report.reportMissingType();
        }
      }
    }
    
    for (String imp : onDemandImports) {
      writer.writeMissingType(imp);
    }
    for (String imp : singleTypeImports.values()) {
      writer.writeMissingType(imp);
    }
    for (String simpleName : simpleNames) {
      if (!singleTypeImports.containsKey(simpleName)) {
        for (String imp : onDemandImports) {
          writer.writeMissingType(imp + "." + simpleName);
        }
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
