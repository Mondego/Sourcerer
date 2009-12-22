package edu.uci.ics.sourcerer.extractor;

import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.OUTPUT_REPO;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.util.Collection;

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
        
        Collection<IndexedJar> previousJars = null;
        Collection<IndexedJar> jars = Helper.newHashSet();
        for (IJarFile jar : files.getJarFiles()) {
          jars.add(index.getIndexedJar(jar.getHash()));
        };
        Collection<IFile> uniqueFiles = null;
        Collection<IFile> bestDuplicateFiles = null;
        boolean missingTypes = extracted.hasMissingTypes();
        while (true) {
          // Set up the writer bundle
          WriterBundle bundle = new WriterBundle(new File(project.getOutputPath(output.getBaseDir())), input);
          
          if (missingTypes) {
            logger.info("  Resolving missing types...");
            jars = resolver.resolveMissingTypes(index, extracted, jars, bundle.getUsedJarWriter());
            if (previousJars == null) {
              logger.info("  Initializing project with " + jars.size() + " jars...");
              EclipseUtils.initializeProject(jars);
            } else {
              jars.removeAll(previousJars);
              logger.info("  Adding " + jars.size() + " jars (" + (previousJars.size() + 1) + ") to project...");
              EclipseUtils.addToProjectClasspath(jars);
              jars.addAll(previousJars);
            }
          } else {
            // TODO: print out the used jars for this case
            logger.info("  Initializing project...");
            EclipseUtils.initializeProject(jars);
          }
          
          if (previousJars == null) {
            logger.info("  Loading " + files.getUniqueJavaFileCount() + " unique java files into project...");
            uniqueFiles = EclipseUtils.loadFilesIntoProject(files.getUniqueJavaFiles());
            
            logger.info("  Loading " + files.getBestDuplicateJavaFileCount() + " chosen duplicate files into project...");
            bestDuplicateFiles = EclipseUtils.loadFilesIntoProject(files.getBestDuplicateJavaFiles());
          }
          
          // Set up the feature extractor
          FeatureExtractor extractor = new FeatureExtractor(bundle);
          
          boolean force = previousJars != null && jars.size() <= previousJars.size();
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
          
          previousJars = jars;
          
          if (Extractor.RESOLVE_MISSING_TYPES.getValue()) {
            if (force) {
              extracted.reportForcedExtraction(report.getExtractedFromSource(), report.getSourceExtractionExceptions());
              break;
            } else if (!(report.hadMissingType() || report.hadMissingSecondOrder())) {
              extracted.reportSuccessfulExtraction(report.getExtractedFromSource(), report.getSourceExtractionExceptions());
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
              extracted.reportSuccessfulExtraction(report.getExtractedFromSource(), report.getSourceExtractionExceptions());
            }
            break;
          }
        }
        
        FileUtils.resetTempDir();
      }
    }
    
    FileUtils.cleanTempDir();
    
    logger.info("Done!");
  }
}
