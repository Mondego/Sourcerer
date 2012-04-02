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
package edu.uci.ics.sourcerer.tools.java.utilization.stats;

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
      Set<String> foundExternal = new HashSet<>();
      Set<String> missingExternal = new HashSet<>();
      
      {
        ReaderBundle bundle = new ReaderBundle(externalProject.getExtractionDir().toFile());
        for (ImportEX imp : bundle.getTransientImports()) {
          foundExternal.add(imp.getImported());
        }
        for (MissingTypeEX miss : bundle.getTransientMissingTypes()) {
          missingExternal.add(miss.getFqn());
        }
      }
      Set<String> foundMissing = new HashSet<>();
      Set<String> missingMissing = new HashSet<>();
      
      ExtractedJavaProject missingProject = missing.getProject(externalProject.getLocation());
      {
        ReaderBundle bundle = new ReaderBundle(missingProject.getExtractionDir().toFile());
        for (ImportEX imp : bundle.getTransientImports()) {
          foundMissing.add(imp.getImported());
        }
        for (MissingTypeEX miss : bundle.getTransientMissingTypes()) {
          missingMissing.add(miss.getFqn());
        }
      }

      Set<String> externalUnion = new HashSet<>();
      externalUnion.addAll(foundExternal);
      externalUnion.addAll(missingExternal);
      
      Set<String> missingUnion = new HashSet<>();
      missingUnion.addAll(foundMissing);
      missingUnion.addAll(missingMissing);
      
      // Verify that there's no overlap between found and missing
      if (externalUnion.size() != foundExternal.size() + missingExternal.size()) {
        task.start("Duplicate external type for: " + externalProject.toString());
        for (String fqn : externalUnion) {
          if (foundExternal.contains(fqn) && missingExternal.contains(fqn)) {
            task.report(fqn);
          }
        }
        task.finish();
      }
      if (missingUnion.size() != foundMissing.size() + missingMissing.size()) {
        task.start("Duplicate missing type for: " + externalProject.toString());
        for (String fqn : missingUnion) {
          if (foundMissing.contains(fqn) && missingMissing.contains(fqn)) {
            task.report(fqn);
          }
        }
        task.finish();
      }
      // Verify that the unions are the same      
      if (!externalUnion.equals(missingUnion)) {
        task.start("Union does not match for: " + externalProject.toString());
        if (!externalUnion.containsAll(missingUnion)) {
          task.start("Missing but not external");
          for (String fqn : missingUnion) {
            if (!externalUnion.contains(fqn)) {
              task.report(fqn);
            }
          }
          task.finish();
        }
        if (!missingUnion.containsAll(externalUnion)) {
          task.start("External but not missing");
          for (String fqn : externalUnion) {
            if (!missingUnion.contains(fqn)) {
              task.report(fqn);
            }
          }
          task.finish();
        }
        task.finish();
      }
      task.progress();
    }
    task.finish();
    
    task.finish();
  }
}
