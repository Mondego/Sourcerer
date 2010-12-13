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

import java.util.Collection;
import java.util.Collections;

import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.model.Project;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedLibrary;
import edu.uci.ics.sourcerer.util.TimeCounter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ImportJavaLibrariesStageTwo extends ExtractedImporterThread {
  private Iterable<ExtractedLibrary> libraries;
  
  protected ImportJavaLibrariesStageTwo(DatabaseConnection connection, SynchronizedUnknownsMap unknowns, Iterable<ExtractedLibrary> libraries) {
    super(connection, unknowns);
    this.libraries = libraries;
  }

  @Override
  public void doImport() {
    TimeCounter counter = new TimeCounter();
    
    Collection<Integer> libraryProjects = projectQueries.getProjectIDsByType(Project.JAVA_LIBRARY);
    Integer primitiveProject = projectQueries.getPrimitiveProjectID(); 
    libraryProjects.add(primitiveProject);
    classifier = new RelationClassifier(libraryProjects);
    
    buildInClause(Collections.singleton(primitiveProject));
    
    for (ExtractedLibrary library : libraries) {
      logger.info("Stage two import of " + library.getName() + "(" + library.getRelativePath() + ")");
      
      Integer projectID = projectQueries.getProjectIDByName(library.getName());
      
      loadEntityMap(projectID);
      loadFileMap(projectID);
      insertRemainingEntities(library, projectID);
      loadRemainingEntityMap(projectID);
      insertRelations(library, projectID);
      insertImports(library, projectID);
      insertComments(library, projectID);
      
      clearMaps();
      counter.increment();
    }
    logger.info(counter.reportTimeAndCount(2, "libraries completed stage two of import"));
  }

}
