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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.uci.ics.sourcerer.tools.java.extractor.Extractor;
import edu.uci.ics.sourcerer.tools.java.extractor.eclipse.EclipseUtils;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ImportWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.MissingTypeWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.WriterBundle;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFileSet;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProjectProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJavaRepository;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MissingTypeIdentifier {
  private final ASTParser parser;
  
  private MissingTypeIdentifier() {
    parser = ASTParser.newParser(AST.JLS4);
  }
  
  static MissingTypeIdentifier create() {
    return new MissingTypeIdentifier();
  }
  
  public static void identifyExternalTypes() {
    identifyMissingTypes(false);
  }
  
  public static void identifyMissingTypes() {
    identifyMissingTypes(true);
  }
  
  private static void identifyMissingTypes(boolean includeJars) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Identifying " + (includeJars ? "missing" : "external") + " types with Eclipse");
    
    // Load the input repository
    task.start("Loading projects");
    JavaRepository repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    Collection<? extends JavaProject> projects = repo.getProjects();
    task.finish();
    
    // Load the output repository
    ModifiableExtractedJavaRepository extracted = JavaRepositoryFactory.INSTANCE.loadModifiableExtractedJavaRepository(JavaRepositoryFactory.OUTPUT_REPO);
    
    MissingTypeIdentifier identifier = new MissingTypeIdentifier();
    
    task.start("Identifying external types in " + projects.size() + " projects", "projects processed", 1);
    for (JavaProject project : projects) {
      task.progress("Processing " + project + " (%d of " + projects.size() + ")");
      ModifiableExtractedJavaProject extractedProject = extracted.getMatchingProject(project);
      if (Boolean.TRUE.equals(extractedProject.getProperties().EXTRACTED.getValue())) {
        if (Extractor.FORCE_REDO.getValue()) {
          extractedProject.reset(project);
        } else {
          task.report("Project already processed");
          continue;
        }
      }
      
      task.start("Getting project contents");
      JavaFileSet files = project.getContent();
      task.finish();
      
      if (includeJars) {
        EclipseUtils.initializeProject(files.getJarFiles());
      } else {
        EclipseUtils.initializeProject(Collections.<JarFile>emptyList());
      }
      
      task.start("Loading " + files.getFilteredJavaFiles().size() + " java files into project");
      Map<JavaFile, IFile> sourceFiles = EclipseUtils.loadFilesIntoProject(files.getFilteredJavaFiles());
      task.finish();
      
      MissingTypeCollection missingTypes = identifier.identifyMissingTypes(sourceFiles);
      
      // Write the missing and resolved types to disk
      try (WriterBundle bundle = new WriterBundle(extractedProject.getExtractionDir().toFile())) {
        MissingTypeWriter writer = bundle.getMissingTypeWriter();
        for (MissingType type : missingTypes.getMissingTypes()) {
          writer.writeMissingType(type.getFqn());
        }
        
        ImportWriter importWriter = bundle.getImportWriter();
        for (String type : missingTypes.getImports()) {
          importWriter.writeImport(type, false, false, null);
        }
      }

      
      // Write the properties files
      ExtractedJavaProjectProperties properties = extractedProject.getProperties();
      properties.EXTRACTED.setValue(true);
      properties.save();
      
      task.report(missingTypes.getImportCount() + " imports types identified");
      task.report(missingTypes.getMissingTypeCount() + (includeJars ? " missing" : " external") + " types identified");
      
    }
    task.finish();
    
    task.finish();
  }
  
  MissingTypeCollection identifyMissingTypes(Map<JavaFile, IFile> sourceFiles) {
    MissingTypeVisitor visitor = MissingTypeVisitor.create();
    for (Map.Entry<JavaFile, IFile> entry : sourceFiles.entrySet()) {
      ICompilationUnit icu = JavaCore.createCompilationUnitFrom(entry.getValue());
      
      parser.setIgnoreMethodBodies(false);
      parser.setStatementsRecovery(false);
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
