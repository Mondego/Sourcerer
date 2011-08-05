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
package edu.uci.ics.sourcerer.tools.link.downloader;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.IBatchM;
import edu.uci.ics.sourcerer.tools.core.repo.model.IRepoM;
import edu.uci.ics.sourcerer.tools.core.repo.model.ISourceProjectM;
import edu.uci.ics.sourcerer.tools.core.repo.model.ISourceProjectM.ContentAdder;
import edu.uci.ics.sourcerer.tools.core.repo.model.RepositoryFactory;
import edu.uci.ics.sourcerer.tools.core.repo.model.SourceProjectProperties;
import edu.uci.ics.sourcerer.tools.link.model.Project;
import edu.uci.ics.sourcerer.util.TimeCounter;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class RepoBuilder {
  private RepoBuilder() {}
  
  public static void addProjectsToRepository(DualFileArgument projectList, String description) {
    IRepoM<? extends ISourceProjectM, ? extends IBatchM<? extends ISourceProjectM>> repo = RepositoryFactory.INSTANCE.loadModifiableSourceRepository(RepositoryFactory.OUTPUT_REPO);
    
    IBatchM<? extends ISourceProjectM> batch = null;
    char starting = 'A';
    try {
      logger.info("Adding projects from " + projectList.toString() + " to repository");
      TimeCounter timer = new TimeCounter(100, 2, "projects added");
      for (Project project : IOUtils.deserialize(Project.class, projectList, true)) {
        if (batch == null) {
          batch = repo.createBatch();
          batch.getProperties().DESCRIPTION.setValue(description);
          batch.getProperties().save();
        } else if (batch.getProjectCount() >= 1000) {
          if (starting == 'A') {
            batch.getProperties().DESCRIPTION.setValue(description + ", Part A");
            batch.getProperties().save();
            starting++;
          }
          batch = repo.createBatch();
          batch.getProperties().DESCRIPTION.setValue(description + ", Part " + starting);
          batch.getProperties().save();
          starting++;
        }
        ISourceProjectM newProject = batch.createProject();
        SourceProjectProperties props = newProject.getProperties();
        props.NAME.setValue(project.getName());
        props.URL.setValue(project.getUrl());
        System.out.println(project.getUrl());
        props.SOURCE.setValue(project.getSource().getName());
        props.save();
        timer.increment();
      }
      timer.logTimeAndCount(0, "projects added");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading project listing.", e);
    }
  }
  
  public static void downloadProjectContent() {
    IRepoM<? extends ISourceProjectM, ? extends IBatchM<? extends ISourceProjectM>> repo = RepositoryFactory.INSTANCE.loadModifiableSourceRepository(RepositoryFactory.INPUT_REPO);
    
    logger.info("Downloading project content...");
    
    SimpleDateFormat format = new SimpleDateFormat("MMM-dd-yyyy");
    String day = format.format(new Date()).toLowerCase();
    
    TimeCounter timer = new TimeCounter();
    
    for (final ISourceProjectM project : repo.getProjects()) {
      if (!project.hasContent() || project.getProperties().DOWNLOAD_DATE.getValue() == null) {
        logger.info("Downloading content for " + project.getProperties().NAME.getValue() + " (" + project.getLocation() + ") from " + project.getProperties().URL.getValue());
        ContentAdder adder = new ContentAdder() {
          @Override
          public boolean addContent(File file) {
            return Downloader.download(project.getProperties().URL.getValue(), file);
          }
        };
        if (project.addContent(adder)) {
          project.getProperties().DOWNLOAD_DATE.setValue(day);
          project.getProperties().save();
        }
        timer.increment();
      }
    }
    timer.logTotalTimeAndCount(0, "projects downloaded");
  }
}
