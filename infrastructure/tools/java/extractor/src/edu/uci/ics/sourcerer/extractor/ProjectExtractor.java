package edu.uci.ics.sourcerer.extractor;

import static edu.uci.ics.sourcerer.util.io.Logging.*;
import static edu.uci.ics.sourcerer.repo.AbstractRepository.*;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IFile;

import edu.uci.ics.sourcerer.extractor.ast.FeatureExtractor;
import edu.uci.ics.sourcerer.extractor.io.IJarFileWriter;
import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.extractor.resources.EclipseUtils;
import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.base.IJarFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;

public class ProjectExtractor {
  public static void extract() {
    Set<String> completed = Logging.initializeResumeLogger();
    
    // Load the input repository
    logger.info("Loading the input repository...");
    Repository input = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
    
    // Load the output repository
    ExtractedRepository output = ExtractedRepository.getUninitializedRepository(OUTPUT_REPO.getValue());
    
    logger.info("Getting the project listing...");
    Collection<RepoProject> projects = input.getProjects();
    logger.info("--- Extracting " + projects.size() + " projects ---");
    int count = 0;
    for (RepoProject project : projects) {
      logger.info("Extracting " + project.getProjectPath() + " (" + ++count + " of " + projects.size() + ")");
      if (completed.contains(project.getProjectPath())) {
        logger.info("  Project already extracted");
      } else {
        logger.info("  Getting file list...");
        IFileSet files = project.getFileSet();
        
        logger.info("  Initializing project...");
        EclipseUtils.initializeProject(files.getJarFiles());
        
        logger.info("  Loading " + files.getUniqueJavaFileCount() + " unique java files into project...");
        Collection<IFile> uniqueFiles = EclipseUtils.loadFilesIntoProject(files.getUniqueJavaFiles());
        
        logger.info("  Loading " + files.getBestDuplicateJavaFileCount() + " chosen duplicate files into project...");
        Collection<IFile> bestDuplicateFiles = EclipseUtils.loadFilesIntoProject(files.getBestDuplicateJavaFiles());
        
        // Set up the writer bundle
        WriterBundle bundle = new WriterBundle(new File(project.getOutputPath(output.getBaseDir())), input);
        
        // Set up the feature extractor
        FeatureExtractor extractor = new FeatureExtractor(bundle);
        
        // Write out the jar files
        IJarFileWriter jarWriter = bundle.getJarFileWriter();
        for (IJarFile file : files.getJarFiles()) {
          jarWriter.writeJarFile(file.getHash());
        }
        
        logger.info("  Extracting " + uniqueFiles.size() + " unique java files...");
        extractor.extractSourceFiles(uniqueFiles);
        
        logger.info("  Extracting " + bestDuplicateFiles.size() + " duplicate java files...");
        extractor.extractSourceFiles(bestDuplicateFiles);
        
        // Close the output files
        extractor.close();
        
        // Copy the properties file
        project.copyPropertiesFile(output.getBaseDir());
        
        // The project is completed
        logger.log(RESUME, project.getProjectPath());
        
        FileUtils.resetTempDir();
      }
    }
    
    FileUtils.cleanTempDir();
    
    logger.info("Done!");
  }
}
