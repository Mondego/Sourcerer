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
package edu.uci.ics.sourcerer.tools.java.component.identifier.stats;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.MissingTypeEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaRepository;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExternalAndMissingComparator {
  public static final Argument<File> EXTERNAL_REPO = new FileArgument("external-repo", "External repo");
  public static final Argument<File> MISSING_REPO = new FileArgument("missing-repo", "Missing repo");
  
  public static void compareExternalAndMissing() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Comparing External and Missing Types");
    // Load up the external repo
    ExtractedJavaRepository external = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(EXTERNAL_REPO);
    
    // Load up the missing repo
    ExtractedJavaRepository missing = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(MISSING_REPO);
    
    // Iterate through the projects in the external repo
    task.start("Examining projects", "projects examined", 1_000);
    for (ExtractedJavaProject externalProject : external.getProjects()) {
      Set<String> externalImports = new HashSet<>();
      Set<String> missingExternalImports = new HashSet<>();
      
      {
        ReaderBundle bundle = ReaderBundle.create(externalProject.getExtractionDir().toFile(), externalProject.getCompressedFile().toFile());
        for (ImportEX imp : bundle.getTransientImports()) {
          externalImports.add(imp.getImported());
        }
        for (MissingTypeEX miss : bundle.getTransientMissingTypes()) {
          missingExternalImports.add(miss.getFqn());
        }
      }
      Set<String> missingImports = new HashSet<>();
      Set<String> missingMissingImports = new HashSet<>();
      
      ExtractedJavaProject missingProject = missing.getProject(externalProject.getLocation());
      {
        ReaderBundle bundle = ReaderBundle.create(missingProject.getExtractionDir().toFile(), missingProject.getCompressedFile().toFile());
        for (ImportEX imp : bundle.getTransientImports()) {
          missingImports.add(imp.getImported());
        }
        for (MissingTypeEX miss : bundle.getTransientMissingTypes()) {
          missingMissingImports.add(miss.getFqn());
        }
      }
      
      // Verify that the external imports and the missing imports are the same
      if (!externalImports.equals(missingImports)) {
        task.start("External and Missing imports don't match for : " + externalProject.toString());
        if (!missingImports.containsAll(externalImports)) {
          task.start("External but not missing");
          for (String fqn : externalImports) {
            if (!missingImports.contains(fqn)) {
              task.report(fqn);
            }
          }
          task.finish();
        }
        if (externalImports.containsAll(missingImports)) {
          task.start("Missing but not external");
          for (String fqn : missingImports) {
            if (!externalImports.contains(fqn)) {
              task.report(fqn);
            }
          }
          task.finish();
        }
        task.finish();
      }

      // Verify that the external missing types are a subset of the imports
      if (!externalImports.containsAll(missingExternalImports)) {
        task.start("Missing external type not found as import for: " + externalProject.toString());
        for (String fqn : missingExternalImports) {
          if (!externalImports.contains(fqn)) {
            task.report(fqn);
          }
        }
        task.finish();
      }
      
      // Verify that the missing missing types are a subset of the imports
      if (!missingImports.containsAll(missingMissingImports)) {
        task.start("Missing missing type not found as import for: " + externalProject.toString());
        for (String fqn : missingExternalImports) {
          if (!missingImports.contains(fqn)) {
            task.report(fqn);
          }
        }
        task.finish();
      }
      
      // Check if there are any missing missing imports that aren't external missing imports
      if (!missingExternalImports.containsAll(missingMissingImports)) {
        task.start("Missing import that isn't external for: " + externalProject.toString());
        for (String fqn : missingMissingImports) {
          if (!missingExternalImports.contains(fqn)) {
            task.report(fqn);
          }
        }
        task.finish();
      }

      task.progress();
    }
    task.finish();
    
    task.finish();
  }
}
