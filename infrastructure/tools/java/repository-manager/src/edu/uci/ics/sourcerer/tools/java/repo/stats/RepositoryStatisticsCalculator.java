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
package edu.uci.ics.sourcerer.tools.java.repo.stats;

import java.util.Map;
import java.util.TreeMap;

import edu.uci.ics.sourcerer.tools.core.repo.model.ContentFile;
import edu.uci.ics.sourcerer.tools.core.repo.model.RepoFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.CreationistMap;
import edu.uci.ics.sourcerer.util.SizeCounter;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter.Alignment;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepositoryStatisticsCalculator {
  private RepositoryStatisticsCalculator() {}
  
  public static void calculateRepositoryStatistics() {
    JavaRepository repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    
    int projects = 0;
    int projectsWithFiles = 0;
    
    TaskProgressLogger task = new TaskProgressLogger("Calculating project statistics");
    task.start("Analyzing projects", "projects analyzed", 100);
    
    SizeCounter total = new SizeCounter();
    CreationistMap<String, SizeCounter> files = new CreationistMap<>(SizeCounter.class);
    
    for (JavaProject project : repo.getProjects()) {
      projects++;
      if (!project.getContent().getFiles().isEmpty()) {
        projectsWithFiles++;
      }
    
      
      for (ContentFile file : project.getContent().getFiles()) {
        RepoFile rFile = file.getFile();
        long length = rFile.toFile().length();
        
        total.add(length);
        
        String extension = rFile.getName();
        int idx = extension.lastIndexOf('.');
        if (idx == -1) {
          extension = "No Extension";
        } else {
          extension = extension.substring(idx);
        }
        files.get(extension).add(length);
      }
      task.progress();
    }
    task.finish();
    
    TreeMap<SizeCounter, String> sortedFiles = new TreeMap<>();
    for (Map.Entry<String, SizeCounter> entry : files.entrySet()) {
      sortedFiles.put(entry.getValue(), entry.getKey());
    }
    
    try (TablePrettyPrinter printer = TablePrettyPrinter.getLoggerPrettyPrinter()) {
      printer.beginTable(2);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Projects");
      printer.addCell(projects);
      printer.beginRow();
      printer.addCell("Projects with Content");
      printer.addCell(projectsWithFiles);
      printer.addDividerRow();
      printer.endTable();
      
      printer.beginTable(3);
      printer.addDividerRow();
      printer.addRow("", "Count", "Size");
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Files");
      printer.addCell(total.getCountString(), Alignment.RIGHT);
      printer.addCell(total.getSizeString(), Alignment.RIGHT);
      printer.addDividerRow();
      for (Map.Entry<SizeCounter, String> entry : sortedFiles.descendingMap().entrySet()) {
        printer.beginRow();
        printer.addCell(entry.getValue() + " Files");
        printer.addCell(entry.getKey().getCountString(), Alignment.RIGHT);
        printer.addCell(entry.getKey().getSizeString(), Alignment.RIGHT);
        
      }
      printer.addDividerRow();
      printer.endTable();
    }
  }
}
