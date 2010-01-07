package edu.uci.ics.sourcerer.extractor;

import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.OUTPUT_REPO;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;

import edu.uci.ics.sourcerer.extractor.ast.FeatureExtractor;
import edu.uci.ics.sourcerer.extractor.ast.FeatureExtractor.SourceExtractionReport;
import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.extractor.resolver.MissingTypeResolver;
import edu.uci.ics.sourcerer.extractor.resources.EclipseUtils;
import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.base.IJarFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedProject;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.general.IndexedJar;
import edu.uci.ics.sourcerer.repo.general.JarIndex;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;

public class ProjectExtractor {
  public static void extract(MissingTypeResolver resolver) {
    // Load the input repository
    logger.info("Loading the input repository...");
    Repository input = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
    
    // Load the output repository
    ExtractedRepository output = ExtractedRepository.getRepository(OUTPUT_REPO.getValue());
    
    logger.info("Getting the jar index...");
    JarIndex index = input.getJarIndex();
    logger.info("Getting the project listing...");
    Collection<RepoProject> projects = input.getProjects();
    logger.info("--- Extracting " + projects.size() + " projects ---");
    int count = 0;
    for (RepoProject project : projects) {
      logger.info("Extracting " + project.getProjectPath() + " (" + ++count + " of " + projects.size() + ")");
      ExtractedProject extracted = project.getExtractedProject(output);
      if (extracted.extracted()) {
        logger.info("  Project already extracted");
      } else if (extracted.hasMissingTypes() && !Extractor.RESOLVE_MISSING_TYPES.getValue()) {
        logger.info("  Project has missing types");
      } else {
        // Set up logging
        Logging.addFileLogger(extracted.getContent());
        
        logger.info("  Getting file list...");
        IFileSet files = project.getFileSet();
        
        Collection<IndexedJar> jars = Helper.newHashSet();
        Collection<String> paths = Helper.newHashSet();
        if (Extractor.USE_PROJECT_JARS.getValue()) {
          for (IJarFile jar : files.getJarFiles()) {
            IndexedJar indexed = index.getIndexedJar(jar.getHash());
            if (indexed == null) {
              logger.log(Level.SEVERE, "Unable to locate " + jar.getName() + "(" + jar.getHash() + ") in index, so using directly");
              paths.add(jar.getPath());
            } else { 
              jars.add(indexed);
              paths.add(indexed.getJarFile().getPath());
            }
          }
        }
        Collection<IFile> uniqueFiles = null;
        Collection<IFile> bestDuplicateFiles = null;
        boolean missingTypes = false;
        while (true) {
          // Set up the writer bundle
          WriterBundle bundle = new WriterBundle(new File(project.getOutputPath(output.getBaseDir())), files);
          boolean force = false;
          if (missingTypes) {
            logger.info("  Resolving missing types...");
            Collection<IndexedJar> newJars = resolver.resolveMissingTypes(index, extracted, bundle.getUsedJarWriter());
            if (newJars.isEmpty()) {
              force = true;
              logger.info("  No jars found to resolve missing types...");
            } else {
              logger.info("  Adding " + newJars.size() + " jar(s) to the classpath...");
              EclipseUtils.addToClasspath(newJars);
              jars.addAll(newJars);
            }
          } else {
            logger.info("  Initializing project with " + jars.size() + " jars...");
            EclipseUtils.initializeProject(paths);
          }
          
          if (!missingTypes) {
            logger.info("  Loading " + files.getUniqueJavaFileCount() + " unique java files into project...");
            uniqueFiles = EclipseUtils.loadFilesIntoProject(files.getUniqueJavaFiles());
            
            logger.info("  Loading " + files.getBestDuplicateJavaFileCount() + " chosen duplicate files into project...");
            bestDuplicateFiles = EclipseUtils.loadFilesIntoProject(files.getBestDuplicateJavaFiles());
          }
          
          // Set up the feature extractor
          FeatureExtractor extractor = new FeatureExtractor(bundle);
          
          if (force) {
            logger.info("  Unable to resolve all missing types, so forcing compilation...");
          }
          
          SourceExtractionReport report = new SourceExtractionReport();
          logger.info("  Extracting " + uniqueFiles.size() + " unique java files...");
          extractor.extractSourceFiles(report, uniqueFiles, force);
          
          logger.info("  Extracting " + bestDuplicateFiles.size() + " duplicate java files...");
          extractor.extractSourceFiles(report, bestDuplicateFiles, force);
          
          // Close the output files
          extractor.close();
         
          if (Extractor.RESOLVE_MISSING_TYPES.getValue()) {
            if (force) {
              extracted.reportForcedExtraction(report.getExtractedFromSource(), report.getSourceExtractionExceptions(), jars.size());
              break;
            } else if (!(report.hadMissingType() || report.hadMissingSecondOrder())) {
              extracted.reportSuccessfulExtraction(report.getExtractedFromSource(), report.getSourceExtractionExceptions(), jars.size());
              break;
            } else {
              missingTypes = report.hadMissingType() || report.hadMissingSecondOrder();
              logger.info("  Redoing because of missing types...");
            }
          } else {
            // Write the properties file
            if (report.hadMissingType() || report.hadMissingSecondOrder()) {
              extracted.reportMissingTypeExtraction();
            } else {
              extracted.reportSuccessfulExtraction(report.getExtractedFromSource(), report.getSourceExtractionExceptions(), jars.size());
            }
            break;
          }
        }
        Logging.removeFileLogger(extracted.getContent());
        FileUtils.resetTempDir();
      }
    }
    
    FileUtils.cleanTempDir();
    
    logger.info("Done!");
  }
}
