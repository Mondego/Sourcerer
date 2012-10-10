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
package edu.uci.ics.sourcerer.tools.java.extractor.misc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.extractor.Extractor;
import edu.uci.ics.sourcerer.tools.java.model.extracted.UsedJarEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaRepository;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedRepositoryAnalyzer {
  public static void createUsedJarFilter() {
    ExtractedJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    
    Collection<? extends ExtractedJavaProject> projects = repo.getProjects();
    
    TaskProgressLogger task = TaskProgressLogger.get();
    
    Set<String> jars = new HashSet<>();
    
    task.start("Processing " + projects.size() + " projets", "projects processed", 500);
    for (ExtractedJavaProject project : projects) {
      ReaderBundle reader = ReaderBundle.create(project.getExtractionDir().toFile(), project.getCompressedFile().toFile());
      
      for (UsedJarEX jar : reader.getTransientUsedJars()) {
        jars.add(jar.getHash());
      }
      task.progress();
    }
    task.report(jars.size() + " jars used");
    task.finish();
    
    task.start("Writing jars to filter file");
    try (BufferedWriter bw = IOUtils.makeBufferedWriter(Extractor.JAR_FILTER)) {
      for (String hash : jars) {
        bw.write(hash);
        bw.newLine();
      }
      task.finish();
    } catch (IOException e) {
      task.exception(e);
    }
  }
}
