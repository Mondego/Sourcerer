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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceBatch;
import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceProject;
import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceProject.ContentAdder;
import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceProject.DeletionFilter;
import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceRepository;
import edu.uci.ics.sourcerer.tools.core.repo.model.RepoFile;
import edu.uci.ics.sourcerer.tools.core.repo.model.RepositoryFactory;
import edu.uci.ics.sourcerer.tools.core.repo.model.SourceProjectProperties;
import edu.uci.ics.sourcerer.tools.link.model.Project;
import edu.uci.ics.sourcerer.util.LetterCounter;
import edu.uci.ics.sourcerer.util.TimeCounter;
import edu.uci.ics.sourcerer.util.io.Console;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class RepoBuilder {
  private RepoBuilder() {}
  
  public static void interactiveRepositoryAdder() {
    ModifiableSourceRepository repo = RepositoryFactory.INSTANCE.loadModifiableSourceRepository(RepositoryFactory.OUTPUT_REPO);
    
    Console console = Console.create();
    String description = console.readLine("Batch description: ");
    String source = console.readLine("Source: ");
    
    ModifiableSourceBatch batch = null;
    LetterCounter counter = new LetterCounter();
    
    while (true) {
      if (batch == null) {
        batch = repo.createBatch();
        batch.getProperties().DESCRIPTION.setValue(description);
        batch.getProperties().save();
      } else if (batch.getProjectCount() >= 1000) {
        if (counter.getCount() == 0) {
          batch.getProperties().DESCRIPTION.setValue(description + ", Part " + counter.getNext());
          batch.getProperties().save();
        }
        batch = repo.createBatch();
        batch.getProperties().DESCRIPTION.setValue(description + ", Part " + counter.getNext());
        batch.getProperties().save();
      }
      ModifiableSourceProject newProject = batch.createProject();
      SourceProjectProperties props = newProject.getProperties();
      while (true) {
        props.NAME.setValue(console.readLine("Name: "));
        props.SOURCE.setValue(source);
        props.PROJECT_URL.setValue(console.readLine("Project URL: "));
        props.SVN_URL.setValue(console.readLine("SVN URL: "));
        if (console.readLine("OK? (enter to continue): ").equals("")) {
          break;
        }
      }
      props.save();
      if (!console.readLine("Add another project? (enter to continue): ").equals("")) {
        break;
      }
    }
  }
  
  public static void addProjectsToRepository(DualFileArgument projectList, String description) {
    ModifiableSourceRepository repo = RepositoryFactory.INSTANCE.loadModifiableSourceRepository(RepositoryFactory.OUTPUT_REPO);
    
    ModifiableSourceBatch batch = null;
    LetterCounter counter = new LetterCounter();
    try {
      logger.info("Adding projects from " + projectList.toString() + " to repository");
      TimeCounter timer = new TimeCounter(100, 2, "projects added");
      for (Project project : IOUtils.deserialize(Project.class, projectList, true)) {
        if (batch == null) {
          batch = repo.createBatch();
          batch.getProperties().DESCRIPTION.setValue(description);
          batch.getProperties().save();
        } else if (batch.getProjectCount() >= 1000) {
          if (counter.getCount() == 0) {
            batch.getProperties().DESCRIPTION.setValue(description + ", Part " + counter.getNext());
            batch.getProperties().save();
          }
          batch = repo.createBatch();
          batch.getProperties().DESCRIPTION.setValue(description + ", Part " + counter.getNext());
          batch.getProperties().save();
        }
        ModifiableSourceProject newProject = batch.createProject();
        SourceProjectProperties props = newProject.getProperties();
        props.NAME.setValue(project.getName());
//        props.URL.setValue(project.getUrl());
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
    ModifiableSourceRepository repo = RepositoryFactory.INSTANCE.loadModifiableSourceRepository(RepositoryFactory.INPUT_REPO);
    
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Downloading projects", "projects downloaded");
    
    SimpleDateFormat format = new SimpleDateFormat("MMM-dd-yyyy");

    for (final ModifiableSourceProject project : repo.getProjects()) {
      if (!project.hasContent() || project.getProperties().DOWNLOAD_DATE.getValue() == null) {
        task.start("Downloading content for " + project.getProperties().NAME.getValue() + " (" + project.getLocation() + ")");
        if (project.hasContent()) {
          project.deleteContent();
        }
        ContentAdder adder = new ContentAdder() {
          @Override
          public boolean addContent(File file) {
            String url = project.getProperties().SVN_URL.getValue(); 
            if (url != null) {
              return Downloader.download(Downloader.Type.SVN, url, file);
            } else {
              logger.log(Level.SEVERE, "No url");
              return false;
            }
          }
        };
        if (project.addContent(adder)) {
          project.getProperties().DOWNLOAD_DATE.setValue(format.format(new Date()).toLowerCase());
          project.getProperties().save();
        }
        task.finish();
      }
      task.progress();
    }
    task.finish();
  }
  
  public static void cleanSubversionContent() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Cleaning SVN content");
    ModifiableSourceRepository repo = RepositoryFactory.INSTANCE.loadModifiableSourceRepository(RepositoryFactory.INPUT_REPO);
    
    DeletionFilter filter = new DeletionFilter() {
      @Override
      public boolean shouldDelete(RepoFile file) {
        return file.isDirectory() && file.getName().equals(".svn");
      }
    };
    
    task.start("Cleaning projects", "projects cleaned");
    for (ModifiableSourceProject project : repo.getProjects()) {
      task.progress("Examining project %d, " + project);
      task.start("Cleaning");
      project.delete(filter);
      task.finish();
    }
    task.finish();
    
    task.finish();
  }
//  
//  public static void fixProjectProperties() {
//    SourceRepository repo = RepositoryFactory.INSTANCE.loadSourceRepository(RepositoryFactory.INPUT_REPO);
//    
//    TaskProgressLogger task = new TaskProgressLogger();
//    
//    task.start("Fixing project properties", "projects fixed", 100);
//    
//    for (SourceProject project : repo.getProjects()) {
//      SourceProjectProperties properties = project.getProperties();
//      if (properties.DOWNLOAD_DATE.getValue() != null) {
//        if (properties.SVN_URL.getValue() != null) {
//          properties.CONTENT_URL.setValue(null);
//          properties.save();
//        }
//      }
//      task.progress();
//    }
//    
//    task.finish();
//  }
}
