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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IClassFile;

import edu.uci.ics.sourcerer.tools.java.extractor.bytecode.ASMExtractor;
import edu.uci.ics.sourcerer.tools.java.extractor.eclipse.EclipseExtractor;
import edu.uci.ics.sourcerer.tools.java.extractor.eclipse.EclipseUtils;
import edu.uci.ics.sourcerer.tools.java.extractor.io.FileWriter;
import edu.uci.ics.sourcerer.tools.java.extractor.io.UsedJarWriter;
import edu.uci.ics.sourcerer.tools.java.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFileSet;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProjectProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJavaRepository;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;

/**
 * @author Joel Ossher 
 *
 */
public class Extractor {
  private JavaRepository repo;
  private ModifiableExtractedJavaRepository extracted;
  
  private TaskProgressLogger task;
  
  private Extractor() {
    // Load the input repository
    repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    // Load the output repository
    extracted = JavaRepositoryFactory.INSTANCE.loadModifiableExtractedJavaRepository(JavaRepositoryFactory.OUTPUT_REPO);
    
    task = new TaskProgressLogger();
  }
  
  public static void extractLibrariesWithASM() {
    Extractor extractor = new Extractor();
    
    extractor.task.start("Performing library extraction with ASM");
    extractor.extractJars(false, true, true);
    extractor.task.finish();
  }
  
  public static void extractLibrariesWithEclipse() {
    Extractor extractor = new Extractor();
    
    extractor.task.start("Performing library extraction with Eclipse");
    extractor.extractJars(true, false, true);
    extractor.task.finish();
  }
  
  public static void extractLibraries() {
    Extractor extractor = new Extractor();
    
    extractor.task.start("Performing library extraction with Eclipse and ASM");
    extractor.extractJars(true, true, true);
    extractor.task.finish();
  }

  
  public static void extractProjectJarsWithASM() {
    Extractor extractor = new Extractor();
    
    extractor.task.start("Performing project jar extraction with ASM");
    extractor.extractJars(false, true, false);
    extractor.task.finish();
  }
  
  public static void extractProjectJarsWithEclipse() {
    Extractor extractor = new Extractor();
    
    extractor.task.start("Performing project jar extraction with Eclipse");
    extractor.extractJars(true, false, false);
    extractor.task.finish();
  }
  
  public static void extractProjectJars() {
    Extractor extractor = new Extractor();
    
    extractor.task.start("Performing project jar extraction with Eclipse");
    extractor.extractJars(true, true, false);
    extractor.task.finish();
  }
  
  public static void extractProjectsWithEclipse() {
    Extractor extractor = new Extractor();
    
    extractor.task = new TaskProgressLogger();
    extractor.task.start("Performing project extraction with Eclipse");
    
    extractor.task.start("Loading projects");
    Collection<? extends JavaProject> projects = extractor.repo.getProjects();
    extractor.task.finish();
    
    extractor.extractProjects(projects);
    
    extractor.task.finish();
  }
  
  private void extractJars(boolean withEclipse, boolean withASM, boolean lib) {
    Collection<? extends JarFile> jars = null;
    task.start("Loading jar files");
    if (lib) {
      jars = repo.getLibraryJarFiles();
    } else {
      jars = repo.getProjectJarFiles();
    }
    task.finish();
    task.start("Extracting " + jars.size() + " jar files", "jar files extracted");
    
    if (!(withEclipse || withASM)) {
      throw new IllegalStateException("Must choose either Eclipse or ASM.");
    }
    
    if (withEclipse && lib) {
      task.start("Initializing eclipse project");
      EclipseUtils.initializeLibraryProject(jars);
      task.finish();
    }
    
    for (JarFile jar : jars) {
      task.progress("Extracting " + jar + " (%d of " + jars.size() + ")");
      ModifiableExtractedJarFile extractedJar = extracted.getMatchingJarFile(jar);
      if (Boolean.TRUE.equals(extractedJar.getProperties().EXTRACTED.getValue())) {
        if (Main.FORCE_REDO.getValue()) {
          extractedJar.reset(jar);
        } else {
          task.report("Library already extracted");
          continue;
        }
      } 
      
      // Set up logging
      Logging.addFileLogger(extractedJar.getExtractionDir().toFile());

      if (withEclipse && !lib) {
        task.start("Initializing eclipse project");
        EclipseUtils.initializeJarProject(jars);
        task.finish();
      }

      // Set up the writer bundle
      WriterBundle writers = new WriterBundle(extractedJar.getExtractionDir().toFile());
    
      ASMExtractor asmExtractor = null;
      if (withASM) {
        asmExtractor = new ASMExtractor(task, writers);
      }
      boolean hasSource = false;
      if (withEclipse) {
        task.start("Getting class files");
        Collection<IClassFile> classFiles = EclipseUtils.getClassFiles(jar);
        task.finish();

        // Extract
        try (EclipseExtractor extractor = new EclipseExtractor(task, writers, asmExtractor)) {
          hasSource = extractor.extractClassFiles(classFiles);
        }
      } else {
        asmExtractor.extractJar(jar.getFile().toFile());
      }
      IOUtils.close(asmExtractor);
        
      // Write the properties files
      ExtractedJarProperties properties = extractedJar.getProperties();
      properties.EXTRACTED.setValue(true);
      properties.HAS_SOURCE.setValue(hasSource);
      properties.save(); 

      // End the error logging
      Logging.removeFileLogger(extractedJar.getExtractionDir().toFile());
    }
    task.finish();
  }
  
  private void extractProjects(Collection<? extends JavaProject> projects) {
    task.start("Extracting " + projects.size() + " projects", "projects extracted");
    for (JavaProject project : projects) {
      task.progress("Extracting " + project + " (%d of " + projects.size() + ")");
      ModifiableExtractedJavaProject extractedProject = extracted.getMatchingProject(project);
      if (Boolean.TRUE.equals(extractedProject.getProperties().EXTRACTED.getValue())) {
        if (Main.FORCE_REDO.getValue()) {
          extractedProject.reset(project);
        } else {
          logger.info("  Project already extracted");
          continue;
        }
      }
      
      // Set up logging
      Logging.addFileLogger(extractedProject.getExtractionDir().toFile());
      
      task.report("Getting project contents");
      JavaFileSet files = project.getContent();
      
      task.start("Loading " + files.getJarFiles().size() + " jar files into classpath");
      EclipseUtils.initializeProject(files.getJarFiles());
      task.finish();
      
      task.start("  Loading " + files.getFilteredJavaFiles().size() + " java files into project");
      Collection<IFile> sourceFiles = EclipseUtils.loadFilesIntoProject(files.getFilteredJavaFiles());
      task.finish();
      
      // Set up the writer bundle
      WriterBundle bundle = new WriterBundle(extractedProject.getExtractionDir().toFile());

      // Write out the jars
      // Write out the used jars
      FileWriter fileWriter = bundle.getFileWriter();
      UsedJarWriter jarWriter = bundle.getUsedJarWriter();
      for (JarFile jar : files.getJarFiles()) {
        JarProperties props = jar.getProperties();
        fileWriter.writeFile(File.JAR, props.NAME.getValue(), null, props.HASH.getValue());
        jarWriter.writeUsedJar(props.HASH.getValue());
      }

      // Extract
      try (EclipseExtractor extractor = new EclipseExtractor(task, bundle)) {
        extractor.extractSourceFiles(sourceFiles);
      }
      
      // Write the properties files
      ExtractedJavaProjectProperties properties = extractedProject.getProperties();
      properties.EXTRACTED.setValue(true);
      properties.save();
      
      // End the error logging
      Logging.removeFileLogger(extractedProject.getExtractionDir().toFile());
    }
    task.finish();
  }
}
