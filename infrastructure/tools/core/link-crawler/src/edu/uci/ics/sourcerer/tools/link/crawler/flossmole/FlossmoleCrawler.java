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
package edu.uci.ics.sourcerer.tools.link.crawler.flossmole;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceBatch;
import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceProject;
import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceRepository;
import edu.uci.ics.sourcerer.tools.core.repo.model.RepositoryFactory;
import edu.uci.ics.sourcerer.tools.core.repo.model.SourceProjectProperties;
import edu.uci.ics.sourcerer.tools.link.model.Project;
import edu.uci.ics.sourcerer.tools.link.model.Source;
import edu.uci.ics.sourcerer.util.LetterCounter;
import edu.uci.ics.sourcerer.util.io.EntryWriter;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FlossmoleCrawler {
  public static final DualFileArgument GOOGLE_CODE_LIST = new DualFileArgument("google-code-list", "google-code-list.txt", "File containing list of crawled Google Code projects.");
  
  private FlossmoleCrawler() {
  }
  
  public static void crawlGoogleCode() {
    new DatabaseRunnable() {
      @Override
      public void action() {
        SelectQuery select = exec.createSelectQuery(GoogleCodeProjects.TABLE);
        select.addSelect(GoogleCodeProjects.PROJECT_NAME);
        TypedQueryResult result = select.select();
        
        SimpleSerializer writer = null;
        EntryWriter<Project> ew = null;
        Project project = new Project();
        try {
          writer = IOUtils.makeSimpleSerializer(GOOGLE_CODE_LIST);
          ew = writer.getEntryWriter(Project.class);
          while (result.next()) {
            String name = result.getResult(GoogleCodeProjects.PROJECT_NAME);
            project.set(name, "http://" + name + ".googlecode.com/svn" , Source.GOOGLE_CODE);
            ew.write(project);
            ew.flush();
          }
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Error writing to file.", e);
        } finally {
          IOUtils.close(ew, writer);
        }
        
      }
    }.run();
  }
  
  public static void addProjectsToRepository(DualFileArgument projectList) {
    ModifiableSourceRepository repo = RepositoryFactory.INSTANCE.loadModifiableSourceRepository(RepositoryFactory.OUTPUT_REPO);
    
    ModifiableSourceBatch batch = null;
    LetterCounter counter = new LetterCounter();
    try {
      TaskProgressLogger task = TaskProgressLogger.get();
      task.start("Adding Google Code Project Hosting projects from " + projectList+ " to repository", "projects added", 500);
      for (Project project : IOUtils.deserialize(Project.class, projectList, true)) {
        if (batch == null) {
          batch = repo.createBatch();
          batch.getProperties().DESCRIPTION.setValue("Projects from Google Code Project Hosting");
          batch.getProperties().save();
        } else if (batch.getProjectCount() >= 1000) {
          if (counter.getCount() == 0) {
            batch.getProperties().DESCRIPTION.setValue("Projects from Google Code Project Hosting, Part " + counter.getNext());
            batch.getProperties().save();
          }
          batch = repo.createBatch();
          batch.getProperties().DESCRIPTION.setValue("Projects from Google Code Project Hosting, Part " + counter.getNext());
          batch.getProperties().save();
        }
        ModifiableSourceProject newProject = batch.createProject();
        SourceProjectProperties props = newProject.getProperties();
        props.NAME.setValue(project.getName());
        props.PROJECT_URL.setValue("http://code.google.com/p/" + project.getName() + "/");
        props.SVN_URL.setValue(project.getUrl());
        props.SOURCE.setValue(project.getSource().getName());
        props.save();
        task.progress();
      }
      task.finish();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading project listing.", e);
    }
  }
}
