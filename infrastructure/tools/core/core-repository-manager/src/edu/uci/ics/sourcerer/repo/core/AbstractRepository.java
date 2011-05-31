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
package edu.uci.ics.sourcerer.repo.core;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.LineFileWriter;
import edu.uci.ics.sourcerer.util.io.LineFileWriter.EntryWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractRepository <Project extends RepoProject> {
  public static final Argument<File> INPUT_REPO = new FileArgument("input-repo", "The root directory of the input repository.");
  public static final Argument<File> OUTPUT_REPO = new FileArgument("output-repo", "The root directory of the output repository.");
  
  public static final Argument<String> REPO_PROPERTIES = new StringArgument("repo-properties-file", "File name for repo properties file.");
  public static final Argument<String> PROJECT_CACHE = new StringArgument("project-cache-file", "project-cache.txt", "File containing a cached list of the projects.");
  public static final Argument<String> BATCH_PROPERTIES = new StringArgument("batch-properties-file", "batch.properties", "File name for batch properties file.");
  
  protected RepoFile repoRoot;
  
  private BatchSet batchSet;
  
  protected AbstractRepository(RepoFile repoRoot) {
    this.repoRoot = repoRoot;
  }
  
  protected abstract Project createProject(ProjectLocation loc);
  
  protected void populateProjects() {
    if (batchSet == null) {
      batchSet = new BatchSet();
      if (repoRoot.exists()) {
        RepoFile cache = repoRoot.getChild(PROJECT_CACHE.getValue());
        if (cache.exists()) {
          try {
            for (ProjectLocation loc : FileUtils.readLineFile(ProjectLocation.class, cache.toFile(), true)) {
              batchSet.add(loc.getBatch(), loc.getCheckout());
            }
            return;
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to load project cache: " + cache.toString(), e);
          }
        }
        Pattern pattern = Pattern.compile("\\d*");
        for (File batch : repoRoot.toFile().listFiles()) {
          if (batch.isDirectory() && pattern.matcher(batch.getName()).matches()) {
            for (File checkout : batch.listFiles()) {
              if (pattern.matcher(checkout.getName()).matches()) {
                batchSet.add(Integer.valueOf(batch.getName()), Integer.valueOf(checkout.getName()));
              }
            }
          }
        }
        LineFileWriter writer = null;
        EntryWriter<ProjectLocation> ew = null;
        try {
          writer = FileUtils.getLineFileWriter(cache.toFile());
          ew = writer.getEntryWriter(ProjectLocation.class);
          for (Project project : getProjects()) {
            ew.write(project.getLocation());
          }
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Unable to write project cache.", e);
        } finally {
          FileUtils.close(ew);
          FileUtils.close(writer);
        }
      }
    }
  }
  
  public Collection<Project> getProjects() {
    if (batchSet == null) {
      populateProjects();
    }
    return batchSet;
  }

  private class BatchSet extends AbstractCollection<Project> {
    private Map<Integer, Batch> batches;
    private int size;
    
    private BatchSet() {
      batches = Helper.newTreeMap();
      size = 0;
    }
    
    private void add(Integer batch, Integer checkout) {
      Batch b = batches.get(batch);
      if (b == null) {
        RepoFile dir = repoRoot.getChild(batch.toString());
        b = new Batch(dir, batch);
        batches.put(batch, b);
      }
      b.add(checkout);
      size++;
    }
    
    @Override
    public Iterator<Project> iterator() {
      return new Iterator<Project>() {
        Iterator<Batch> batchIter = batches.values().iterator();
        Iterator<Project> projectIter = null;
        Project next = null;
        
        @Override
        public boolean hasNext() {
          while (next == null) {
            if (projectIter == null) {
              if (batchIter == null) {
                return false;
              } else if (batchIter.hasNext()) {
                projectIter = batchIter.next().projects.values().iterator();
              } else {
                batchIter = null;
                return false;
              }
            } else if (projectIter.hasNext()) {
              next = projectIter.next();
            } else {
              projectIter = null;
            }
          }
          return true;
        }

        @Override
        public Project next() {
          if (hasNext()) {
            Project retval = next;
            next = null;
            return retval;
          } else {
            throw new NoSuchElementException();
          }
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public int size() {
      return size;
    }
  }
  
  private final class Batch {
    private final RepoFile dir;
    private final Integer batch;
    private final RepoFile properties;
    private final Map<Integer, Project> projects;
    
    private Batch(RepoFile dir, Integer batch) {
      this.dir = dir;
      this.batch = batch;
      properties = dir.getChild(BATCH_PROPERTIES.getValue());
      projects = Helper.newTreeMap();
    }
    
    private void add(Integer checkout) {
      projects.put(checkout, createProject(new ProjectLocation(batch, checkout,dir.getChild(checkout.toString()))));
    }
  }
}
