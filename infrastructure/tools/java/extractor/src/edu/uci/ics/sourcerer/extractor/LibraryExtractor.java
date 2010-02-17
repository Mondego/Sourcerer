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

import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.OUTPUT_REPO;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.util.Collection;

import org.eclipse.jdt.core.IClassFile;

import edu.uci.ics.sourcerer.extractor.ast.FeatureExtractor;
import edu.uci.ics.sourcerer.extractor.ast.FeatureExtractor.ClassExtractionReport;
import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.extractor.resources.EclipseUtils;
import edu.uci.ics.sourcerer.extractor.resources.LibraryJar;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedLibrary;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.util.io.Logging;

/**
 * @author Joel Ossher 
 *
 */
public class LibraryExtractor {
  public static void extract() {
    // Load the output repository
    ExtractedRepository output = ExtractedRepository.getRepository(OUTPUT_REPO.getValue());
    
    logger.info("Getting the library jars...");
    Collection<LibraryJar> libraryJars = EclipseUtils.getLibraryJars();
    logger.info("--- Extracting " + libraryJars.size() + " library jars ---");
    logger.info("  Initializing project...");
    EclipseUtils.initializeLibraryProject();
    int count = 0;
    for (LibraryJar library : libraryJars) {
      logger.info("Extracting " + library + " (" + ++count + " of " + libraryJars.size() + ")");
      ExtractedLibrary extracted = library.getExtractedLibrary(output);
      if (extracted.extracted()) {
        logger.info("  Library already extracted");
      } else {
        // Set up logging
        Logging.addFileLogger(extracted.getOutputDir());

        logger.info("  Getting class files...");
        Collection<IClassFile> classFiles = EclipseUtils.getClassFiles(library);
        
        logger.info("  Extracting " + classFiles.size() + " class files...");
        
        // Set up the writer bundle
        WriterBundle bundle = new WriterBundle(extracted.getOutputDir());
        
        // Set up the feature extractor
        FeatureExtractor extractor = new FeatureExtractor(bundle);
        
        // Extract
        ClassExtractionReport report = extractor.extractClassFiles(classFiles, true);
        
        // Close the output files
        extractor.close();
        
        // Copy the library jar
        extracted.copyLibraryJar(new File(library.getPath()));
        String sourcePath = library.getSourcePath();
        if (sourcePath != null) {
          extracted.copyLibraryJarSource(new File(sourcePath));
        }
        
        // Write the properties files
        extracted.createPropertiesFile(library.getName(), report.getExtractedFromBinary(), report.getBinaryExtractionExceptions(), report.getExtractedFromSource(), report.getSourceExtractionExceptions());

        // End the error logging
        Logging.removeFileLogger(extracted.getOutputDir());
      }
    }
    
    logger.info("Done!");
  }
}
