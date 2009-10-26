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

import static edu.uci.ics.sourcerer.repo.AbstractRepository.*;
import static edu.uci.ics.sourcerer.util.io.Logging.*;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;

import edu.uci.ics.sourcerer.extractor.ast.FeatureExtractor;
import edu.uci.ics.sourcerer.extractor.io.WriterBundle;
import edu.uci.ics.sourcerer.extractor.resources.EclipseUtils;
import edu.uci.ics.sourcerer.repo.IndexedJar;
import edu.uci.ics.sourcerer.repo.JarIndex;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarExtractor {
  public static void extract() {
    Set<String> completed = Logging.initializeResumeLogger();
    
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
      if (completed.contains(jar.toString())) {
        logger.info("  Jar already extracted");
      } else {
        logger.info("  Initializing project...");
        EclipseUtils.initializeJarProject(jar);
        
        logger.info("  Getting class files...");
        Collection<IClassFile> classFiles = EclipseUtils.getClassFiles(new Path(jar.getJarFile().getPath()));
        
        logger.info("  Extracting " + classFiles.size() + " class files...");
        
        // Set up the writer bundle
        WriterBundle bundle = new WriterBundle(new File(jar.getOutputPath(output.getJarsDir())));
        
        // Set up the feature extractor
        FeatureExtractor extractor = new FeatureExtractor(bundle);
        
        // Extract
        if (extractor.extractClassFiles(classFiles)) {
          logger.info("    Found source, but error in extraction.");
          logger.log(Level.SEVERE, "Error in source extraction for " + jar.toString());
        }
        
        // Close the output files
        extractor.close();
        
        // Copy the properties file
        jar.copyPropertiesFile(output.getJarsDir());
       
        // The jar is completed
        logger.log(RESUME, jar.toString());
      }
    }
    
    logger.info("Copying jar index file...");
    FileUtils.copyFile(input.getJarIndexFile(), output.getJarIndexFile());
    
    logger.info("Done!");
  }
}
