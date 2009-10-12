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
package edu.uci.ics.sourcerer.extractor;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IClassFile;

import edu.uci.ics.sourcerer.extractor.ast.FeatureExtractor;
import edu.uci.ics.sourcerer.extractor.io.IJarFileWriter;
import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.extractor.io.WriterFactory;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyJarFileWriter;
import edu.uci.ics.sourcerer.extractor.io.file.CommentWriter;
import edu.uci.ics.sourcerer.extractor.io.file.EntityWriter;
import edu.uci.ics.sourcerer.extractor.io.file.FileWriter;
import edu.uci.ics.sourcerer.extractor.io.file.ImportWriter;
import edu.uci.ics.sourcerer.extractor.io.file.JarEntityWriter;
import edu.uci.ics.sourcerer.extractor.io.file.JarFileWriter;
import edu.uci.ics.sourcerer.extractor.io.file.LocalVariableWriter;
import edu.uci.ics.sourcerer.extractor.io.file.ProblemWriter;
import edu.uci.ics.sourcerer.extractor.io.file.RelationWriter;
import edu.uci.ics.sourcerer.extractor.resources.EclipseUtils;
import edu.uci.ics.sourcerer.repo.IndexedJar;
import edu.uci.ics.sourcerer.repo.JarIndex;
import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.base.IJarFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Extractor implements IApplication {
  public static void extract(PropertyManager properties) {
    if (properties.isSet(Property.REPO_MODE)) {
      Set<String> completed = Logging.initializeResumeLogger();
      
      // Initialize EclipseUtils
      EclipseUtils.initialize();
      
      // Initialize the FeatureExtractor
      FeatureExtractor extractor = new FeatureExtractor(); 
      extractor.setPPA(properties.isSet(Property.PPA));
      
      // Get an uninitialized output repository
      ExtractedRepository output = ExtractedRepository.getUninitializedRepository(properties.getValueAsFile(Property.OUTPUT));
      // Start with the system jars
      logger.info("Getting the library jars");
      Collection<IPath> libraryJars = EclipseUtils.getLibraryJars();
      logger.info("Extracting " + libraryJars.size() + " library jars");
      int count = 0;
      for (IPath library : libraryJars) {
        File lib = library.toFile();
        logger.info("-------------------------------");
        logger.info("Extracting " + lib.getName() + " (" + ++count + " of " + libraryJars.size() + ")");
        
        if (completed.contains("**" + lib.getName())) {
          logger.info("Library already completed!");
        } else {
          logger.info("Initializing project");
          EclipseUtils.initializeLibraryProject(library);
          
          logger.info("Getting class files");
          Collection<IClassFile> classFiles = EclipseUtils.getClassFiles();
          
          logger.info("Extracting " + classFiles.size() + " class files");
          // Set up the writer bundle
          String libPath = new File(output.getLibsDir(), lib.getName()).getPath();
          properties.setProperty(Property.OUTPUT, libPath);
          WriterBundle bundle = new WriterBundle();
          extractor.setBundle(bundle);
          
          // Extract!
          extractor.extractClassFiles(classFiles);
          
          // Close the output files
          extractor.close();
          
          // The jar is completed
          logger.log(Logging.RESUME, "**" + lib.getName());
        }
      }
      
      // Set up the input repository
      File repoRoot = new File(properties.getValue(Property.REPO_ROOT));
      File tempDir = FileUtils.getTempDir();
      Repository repo = Repository.getRepository(repoRoot, tempDir);
      
      // Start by analyzing the jars
      logger.info("Getting the jar index");
      JarIndex jarIndex = repo.getJarIndex();
      logger.info("Extracting " + jarIndex.getIndexSize() + " jars");
      count = 0;
      for (IndexedJar jar : jarIndex.getIndexedJars()) {
        logger.info("-------------------------------");
        logger.info("Extracting " + jar.getRelativePath() + " (" + ++count + " of " + jarIndex.getIndexSize() + ")");
        
        if (completed.contains(jar.getRelativePath())) {
          logger.info("Jar already completed!");
        } else {
          logger.info("Initializing project");
          EclipseUtils.initializeJarProject(jar);
          
          logger.info("Getting class files");
          Collection<IClassFile> classFiles = EclipseUtils.getClassFiles();
          
          logger.info("Extracting " + classFiles.size() + " class files");
          // Set up the properties
          properties.setProperty(Property.OUTPUT, jar.getOutputPath(output.getJarsDir()));
          WriterBundle bundle = new WriterBundle();
          extractor.setBundle(bundle);;
          
          // Extract!
          extractor.extractClassFiles(classFiles);
          
          // Close the output files
          extractor.close();
          
          // Copy the properties file
          jar.copyPropertiesFile(output.getJarsDir());
          
          // The jar is completed
          logger.log(Logging.RESUME, jar.getRelativePath());
        }
      }
      
      FileUtils.copyFile(repo.getJarIndexFile(), output.getJarIndexFile());
      
      // If jar only, terminate
      if (properties.isSet(Property.JAR_ONLY)) {
        logger.info("Done!");
        return;
      }
      
      // Now analyze the projects
      logger.info("Getting project list");
      Collection<RepoProject> projects = repo.getProjects();
      logger.info("Extracting " + projects.size() + " projects");
      count = 0;
      
      for (RepoProject project : projects) {
        logger.info("-------------------------------");
        logger.info("Extracting " + project.getProjectPath() + " (" + ++count + " of " + projects.size() + ")");
        
        if (!properties.isSet(Property.FORCE_REDO) && completed.contains(project.getProjectPath())) {
          logger.info("Project already completed!");
        } else {
          logger.info("Getting file list");
          IFileSet files = project.getFileSet();
          
          logger.info("Initializing project with " + files.getJarFileCount() + " jar files");
          EclipseUtils.initializeProject(files.getJarFiles());
          
          logger.info("Loading " + files.getUniqueJavaFileCount() + " unique files into project");
          Collection<IFile> uniqueIFiles = EclipseUtils.loadFilesIntoProject(files.getUniqueJavaFiles());
          
          logger.info("Loading " + files.getBestDuplicateJavaFileCount() + " chosen duplicate files into project");
          Collection<IFile> bestDuplicateIFiles = EclipseUtils.loadFilesIntoProject(files.getBestDuplicateJavaFiles());
          
          // Set up the output
          properties.setProperty(Property.OUTPUT, project.getOutputPath(output.getBaseDir()));
          WriterBundle bundle = new WriterBundle(repo);
          extractor.setBundle(bundle);
          
          // Write out jar files
          IJarFileWriter jarWriter = WriterFactory.createWriter(null, Property.JAR_FILE_WRITER, DummyJarFileWriter.class);
          for (IJarFile file : files.getJarFiles()) {
            jarWriter.writeJarFile(file.getHash());
          }
          jarWriter.close();
          
          logger.info("Extracting references for " + uniqueIFiles.size() + " compilation units");
          extractor.extractSourceFiles(uniqueIFiles);
          
          logger.info("Extracting references for " + bestDuplicateIFiles.size() + " compilation units");
          extractor.extractSourceFiles(bestDuplicateIFiles);
          
          extractor.close();
          
          repo.cleanTempDir();
          
          logger.info("Copying properties file");
          FileUtils.copyFile(project.getPropertiesFile(), new File(project.getOutputPath(output.getBaseDir()), project.getPropertiesFile().getName()));
          
          logger.log(Logging.RESUME, project.getProjectPath());
        }
      }
      logger.info("Done!");
    } else {
//      // Initialize EclipseUtils
//      EclipseUtils.initialize();
//      
//      logger.info("Getting file list");
//      File input = new File(properties.getValue(Property.INPUT));
//      if (!input.exists() || !input.isDirectory()) {
//        logger.log(Level.SEVERE, "Input needs to be a directory: " + properties.getValue(Property.INPUT));
//        return;
//      }
//        
//      IFileSet files = new FileSet(input, input.getPath());
//      
//      logger.info("Initializing project with " + files.getJarFileCount() + " jar files");
//      EclipseUtils.initializeProject(files.getJarFiles());
//      
//      logger.info("Loading " + files.getUniqueJavaFileCount() + " unique files into project");
//      Collection<IFile> uniqueIFiles = EclipseUtils.loadFilesIntoProject(files.getUniqueJavaFiles());
//      
//      logger.info("Loading " + files.getBestDuplicateJavaFileCount() + " chosen duplicate files into project");
//      Collection<IFile> bestDuplicateIFiles = EclipseUtils.loadFilesIntoProject(files.getBestDuplicateJavaFiles());
//      
//      FeatureExtractor extractor = new FeatureExtractor();
//      
//      extractor.resetOutput();
//      extractor.setPPA(properties.isSet(Property.PPA));
// 
//      logger.info("Extracting references for " + uniqueIFiles.size() + " compilation units");
//      extractor.extractSourceFiles(uniqueIFiles);
//      
//      logger.info("Extracting references for " + bestDuplicateIFiles.size() + " compilation units");
//      extractor.extractSourceFiles(bestDuplicateIFiles);
//      
//      extractor.close();
//
//      logger.info("Done!");
    }
  }

  @Override
  public Object start(IApplicationContext context) throws Exception {
    String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
    PropertyManager properties = PropertyManager.getProperties();
    properties.setProperty(Property.FILE_WRITER, FileWriter.class.getName());
    properties.setProperty(Property.JAR_FILE_WRITER, JarFileWriter.class.getName());
    properties.setProperty(Property.IMPORT_WRITER, ImportWriter.class.getName());
    properties.setProperty(Property.ENTITY_WRITER, EntityWriter.class.getName());
    properties.setProperty(Property.RELATION_WRITER, RelationWriter.class.getName());
    properties.setProperty(Property.LOCAL_VARIABLE_WRITER, LocalVariableWriter.class.getName());
    properties.setProperty(Property.COMMENT_WRITER, CommentWriter.class.getName());
    properties.setProperty(Property.JAR_ENTITY_WRITER, JarEntityWriter.class.getName());
    properties.setProperty(Property.PROBLEM_WRITER, ProblemWriter.class.getName());
    Extractor.extract(properties);
    return EXIT_OK;
  }

  @Override
  public void stop() {}
}
