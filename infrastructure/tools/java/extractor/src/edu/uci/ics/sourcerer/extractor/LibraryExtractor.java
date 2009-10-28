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
import static edu.uci.ics.sourcerer.util.io.Logging.RESUME;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;

import edu.uci.ics.sourcerer.extractor.ast.FeatureExtractor;
import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.extractor.resources.EclipseUtils;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.util.io.Logging;

/**
 * @author Joel Ossher 
 *
 */
public class LibraryExtractor {
  public static void extract() {
    Set<String> completed = Logging.initializeResumeLogger();
    
    // Load the output repository
    ExtractedRepository output = ExtractedRepository.getUninitializedRepository(OUTPUT_REPO.getValue());
    
    logger.info("Getting the library jars...");
    Collection<IPath> libraryJars = EclipseUtils.getLibraryJars();
    logger.info("--- Extracting " + libraryJars.size() + " library jars ---");
    int count = 0;
    for (IPath library : libraryJars) {
      logger.info("Extracting " + library.lastSegment() + " (" + ++count + " of " + libraryJars.size() + ")");
      if (completed.contains(library.lastSegment())) {
        logger.info("  Library already extracted");
      } else {
        logger.info("  Initializing project...");
        EclipseUtils.initializeLibraryProject(library);
        
        logger.info("  Getting class files...");
        Collection<IClassFile> classFiles = EclipseUtils.getClassFiles(library);
        
        logger.info("  Extracting " + classFiles.size() + " class files...");
        
        // Set up the writer bundle
        File libPath = new File(output.getLibsDir(), library.lastSegment());
        WriterBundle bundle = new WriterBundle(libPath);
        
        // Set up the feature extractor
        FeatureExtractor extractor = new FeatureExtractor(bundle);
        
        // Extract
        extractor.extractClassFiles(classFiles);
        
        // Close the output files
        extractor.close();
        
        // The library is completed
        logger.log(RESUME, library.lastSegment());
      }
    }
    
    logger.info("Done!");
  }
}
