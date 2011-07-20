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
package edu.uci.ics.sourcerer.tools.core.repo.internal;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;
import edu.uci.ics.sourcerer.tools.core.repo.model.IBatch;
import edu.uci.ics.sourcerer.tools.core.repo.model.IProject;
import edu.uci.ics.sourcerer.tools.core.repo.model.IRepository;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.internal.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepositoryStatistics {
  public static void printProjectCount(IRepository<IProject, IBatch<IProject>> repo) {
    logger.info(repo + " has " + repo.getProjects().size() + " projects");
  }
  
  public static final DualFileArgument PROJECT_NAMES_FILE = new DualFileArgument("project-names-file", "project-names.txt", "File containg a table of the project names.");
  public static void exportProjectNames(IRepository<IProject, IBatch<IProject>> repo) {
    TablePrettyPrinter printer = null;
    try {
      printer = TablePrettyPrinter.getTablePrettyPrinter(PROJECT_NAMES_FILE);
      printer.addHeader("Project names for " + repo);
      printer.beginTable(2);
      printer.addDividerRow();
      printer.addRow("Project Name", "Project Path");
      printer.addDividerRow();
      for (IProject project : repo.getProjects()) {
        printer.beginRow();
        printer.addCell(project.getProperties().NAME.getValue());
        printer.addCell(project.getLocation().getProjectRoot().getRelativePath().toString());
      }
      printer.endTable();
    } finally {
      FileUtils.close(printer);
    }
  }
}
