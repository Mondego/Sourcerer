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
package edu.uci.ics.sourcerer.tools.java.repo.misc;

import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceProject.DeletionFilter;
import edu.uci.ics.sourcerer.tools.core.repo.model.RepoFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.ModifiableJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.ModifiableJavaRepository;
import edu.uci.ics.sourcerer.util.MemoryStatsReporter;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepositoryCleaner {
  public static void cleanNonJavaFiles() {
    ModifiableJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadModifiableJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    
    class CustomFilter implements DeletionFilter {
      long deleted = 0l;
      @Override
      public boolean shouldDelete(RepoFile file) {
        boolean delete = !(file.isDirectory() || file.getName().endsWith(".java") || file.getName().endsWith(".jar"));
        if (delete) {
          deleted += file.toFile().length();
        }
        return delete;
      }
    };
    
    CustomFilter filter = new CustomFilter();
    
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Cleaning Java repository at " + JavaRepositoryFactory.INPUT_REPO.getValue(), "projects cleaned", 500);
    for (ModifiableJavaProject project : repo.getProjects()) {
      project.delete(filter);
      task.progress("%d projects cleaned (" + MemoryStatsReporter.formatSize(filter.deleted) + ") in %s");
    }
    task.report("Cleaned a total of " + MemoryStatsReporter.formatSize(filter.deleted));
    task.finish();
  }
}
