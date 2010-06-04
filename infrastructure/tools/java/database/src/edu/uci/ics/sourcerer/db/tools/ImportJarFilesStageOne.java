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
package edu.uci.ics.sourcerer.db.tools;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.model.db.LimitedProjectDB;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedJar;
import edu.uci.ics.sourcerer.util.TimeCounter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ImportJarFilesStageOne extends ExtractedImporterThread {
  private Iterable<ExtractedJar> jars;
  
  protected ImportJarFilesStageOne(DatabaseConnection connection, Iterable<ExtractedJar> jars) {
    super(connection);
    this.jars = jars;
  }

  @Override
  public void doImport() {
    TimeCounter counter = new TimeCounter();
    
    for (ExtractedJar jar : jars) {
      logger.info("Stage one import of " + jar.getName() + "(" + jar.getRelativePath() + ")");
      
      logger.info("  Verifying that jar should be imported...");
      if (!jar.extracted()) {
        logger.info("    Extraction not completed... skipping");
        continue;
      }
      if (!jar.reallyExtracted()) {
        logger.info("    Extraction copied... skipping");
        continue;
      }
      LimitedProjectDB project = projectsTable.getLimitedProjectByHash(jar.getHash());
      if (project != null) {
        if (project.firstStageCompleted()) {
          logger.info("    Import already completed... skipping");
          continue;
        } else {
          logger.info("    Import not completed... deleting");
          deleteByProject(project.getProjectID());
        }
      }
      
      logger.info("  Inserting project...");
      String projectID = projectsTable.insert(jar);

      insertFiles(jar, projectID);
      loadFileMap(projectID);
      insertProblems(jar, projectID);
      insertEntities(jar, projectID);
      
      
      projectsTable.endFirstStageJarProjectInsert(projectID);
      clearMaps();
      counter.increment();
    }
    logger.info(counter.reportTimeAndCount(2, "jars completed stage one of import"));
  }
}
