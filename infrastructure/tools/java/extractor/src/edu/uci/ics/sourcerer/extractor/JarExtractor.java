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

import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.OUTPUT_REPO;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;

import org.eclipse.jdt.core.IClassFile;

import edu.uci.ics.sourcerer.extractor.ast.FeatureExtractor;
import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.extractor.resources.EclipseUtils;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedJar;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.general.IndexedJar;
import edu.uci.ics.sourcerer.repo.general.JarIndex;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarExtractor {
  public static void extract() {
    // Load the input repository
    logger.info("Loading the input repository...");
    Repository input = Repository.getRepository(INPUT_REPO.getValue());
    
    // Load the output repository
    ExtractedRepository output = ExtractedRepository.getUninitializedRepository(OUTPUT_REPO.getValue());
    
    logger.info("Getting the jar index...");
    JarIndex index = input.getJarIndex();
    logger.info("--- Extracting " + index.getIndexSize() + " jars ---");
    int count = 0;
    for (IndexedJar jar : index.getIndexedJars()) {
      logger.info("Extracting " + jar.toString() + " (" + ++count + " of " + index.getIndexSize() + ")");
      ExtractedJar extracted = jar.getExtractedJar(output);
      if (extracted.extracted()) {
        logger.info("  Jar already extracted");
      } else {
        // Set up logging
        Logging.addFileLogger(extracted.getContent());

        logger.info("  Initializing project...");
        EclipseUtils.initializeJarProject(jar);
        
        logger.info("  Getting class files...");
        Collection<IClassFile> classFiles = EclipseUtils.getClassFiles(jar);
        
        logger.info("  Extracting " + classFiles.size() + " class files...");
        
        // Set up the writer bundle
        WriterBundle bundle = new WriterBundle(extracted.getContent());
                
        // Set up the feature extractor
        FeatureExtractor extractor = new FeatureExtractor(bundle);
        
        // Extract
        extractor.extractClassFiles(classFiles);
        
        // Close the output files
        extractor.close();
        
        // Write the properties file
        extracted.reportExecution(extractor.foundSource(), extractor.sourceError());
       
        // End the error logging
        Logging.removeFileLogger(extracted.getContent());
      }
    }
    
    logger.info("Copying jar index file...");
    FileUtils.copyFile(input.getJarIndexFile(), output.getJarIndexFile());
    
    logger.info("Done!");
  }
}
