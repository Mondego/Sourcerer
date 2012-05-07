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
package edu.uci.ics.sourcerer.tools.core.repo.model.internal;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.tools.core.repo.model.ProjectLocation;
import edu.uci.ics.sourcerer.tools.core.repo.model.RepositoryProperties;
import edu.uci.ics.sourcerer.util.io.EntryWriter;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractRepository<Project extends AbstractRepoProject<? extends AbstractRepository<Project, Batch>, ?>, Batch extends BatchImpl<Project>> {
  public static final Argument<String> REPO_PROPERTIES = new StringArgument("repo-properties-file", "repo.properties", "File name for repo properties file.").permit();
  public static final Argument<String> PROJECT_CACHE = new StringArgument("project-cache-file", "project-cache.txt", "File containing a cached list of the projects.").permit();
  public static final Argument<Boolean> CLEAR_CACHES = new BooleanArgument("clear-caches", false, "Clear all repository caches.").permit();
  
  protected RepoFileImpl repoRoot;
  protected RepositoryProperties properties;
  
  private RepoFileImpl cache;
  private BatchSetImpl<Project, Batch> batchSet;
  
  protected AbstractRepository(RepoFileImpl repoRoot) {
    this.repoRoot = repoRoot.asRoot();
    repoRoot.makeDirs();
    properties = new RepositoryProperties(repoRoot.getChild(REPO_PROPERTIES));
  }
  
  protected void clearCache() {
    cache.delete();
  }
  
  protected abstract Project createProject(ProjectLocationImpl loc);

  protected Project addProject(Integer batch, Integer checkout) {
    return batchSet.add(batch, checkout);
  }
  
  private final void populateProjects() {
    if (batchSet == null) {
      batchSet = new BatchSetImpl<Project, Batch>(this);
      cache = repoRoot.getChild(PROJECT_CACHE.getValue());
      if (repoRoot.exists()) {
        if (cache.exists() && !CLEAR_CACHES.getValue()) {
          try {
            for (ProjectLocationImpl loc : IOUtils.deserialize(ProjectLocationImpl.class, cache.toFile(), true)) {
              batchSet.add(loc.getBatchNumber(), loc.getCheckoutNumber());
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
        SimpleSerializer writer = null;
        EntryWriter<ProjectLocationImpl> ew = null;
        try {
          writer = IOUtils.makeSimpleSerializer(cache.toFile());
          ew = writer.getEntryWriter(ProjectLocationImpl.class);
          for (Project project : getProjects()) {
            ew.write(project.getLocation());
          }
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Unable to write project cache.", e);
        } finally {
          IOUtils.close(ew);
          IOUtils.close(writer);
        }
      }
    }
  }
  
  public Batch createBatch() {
    if (batchSet == null) {
      populateProjects();
    }
    return batchSet.createBatch();
  }
  
  public abstract Batch newBatch(RepoFileImpl dir, Integer batch);
 
  public Collection<Batch> getBatches() {
    if (batchSet == null) {
      populateProjects();
     }
    return batchSet.getBatches();
  }
  
  public Batch getBatch(ProjectLocation loc) {
    ProjectLocationImpl impl = (ProjectLocationImpl) loc;
    return batchSet.getBatch(impl.getBatchNumber());
  }
  
  public Project getProject(String path) {
    int slash = path.indexOf('/');
    if (slash == -1) {
      throw new IllegalArgumentException(path + " is not a valid path");
    } else {
      Integer batch = Integer.valueOf(path.substring(0, slash));
      Integer checkout = Integer.valueOf(path.substring(slash + 1));
      return getProject(batch, checkout);
    }
  }
  
  public Project getProject(Integer batch, Integer checkout) {
    if (batchSet == null) {
      populateProjects();
    }
    BatchImpl<Project> b = batchSet.getBatch(batch);
    if (b == null) {
      return null;
    } else {
      return b.getProject(checkout);
    }
  }
  
  public Project getProject(ProjectLocation loc) {
    ProjectLocationImpl location = (ProjectLocationImpl) loc;
    return getProject(location.getBatchNumber(), location.getCheckoutNumber());
  }
  
  public Collection<Project> getProjects() {
    if (batchSet == null) {
      populateProjects();
    }
    return batchSet;
  }
  
  public int getProjectCount() {
    return getProjects().size();
  }
  
  @Override
  public String toString() {
    return repoRoot.getRelativePath().toString();
  }
}
