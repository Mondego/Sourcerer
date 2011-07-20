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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.tools.core.repo.model.IRepositoryMod;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;
import edu.uci.ics.sourcerer.util.io.internal.FileUtils;
import edu.uci.ics.sourcerer.util.io.internal.SimpleSerializerImpl;
import edu.uci.ics.sourcerer.util.io.internal.SimpleSerializerImpl.EntryWriter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractRepository <Project extends RepoProject> implements IRepositoryMod<Project, Batch<Project>> {
  public static final Argument<String> REPO_PROPERTIES = new StringArgument("repo-properties-file", "File name for repo properties file.");
  public static final Argument<String> PROJECT_CACHE = new StringArgument("project-cache-file", "project-cache.txt", "File containing a cached list of the projects.");
  
  protected RepoFile repoRoot;
  
  private RepoFile cache;
  private BatchSet<Project> batchSet;
  
  protected AbstractRepository(RepoFile repoRoot) {
    this.repoRoot = repoRoot;
  }
  
  protected void clearCache() {
    cache.delete();
  }
  
  protected abstract Project createProject(ProjectLocation loc);

  protected Project addProject(Integer batch, Integer checkout) {
    return batchSet.add(batch, checkout);
  }
  
  private final void populateProjects() {
    if (batchSet == null) {
      batchSet = new BatchSet<Project>(this);
      cache = repoRoot.getChild(PROJECT_CACHE.getValue());
      if (repoRoot.exists()) {
        if (cache.exists()) {
          try {
            for (ProjectLocation loc : FileUtils.readLineFile(ProjectLocation.class, cache.toFile(), true)) {
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
        SimpleSerializerImpl writer = null;
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
  
  @Override
  public Batch<Project> createBatch() {
    if (batchSet == null) {
      populateProjects();
    }
    return batchSet.createBatch();
  }
 
  @Override
  public Collection<Batch<Project>> getBatches() {
    if (batchSet == null) {
      populateProjects();
     }
    return batchSet.getBatches();
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
    Batch<Project> b = batchSet.getBatch(batch);
    if (b == null) {
      return null;
    } else {
      return b.getProject(checkout);
    }
  }
  
  public Collection<Project> getProjects() {
    if (batchSet == null) {
      populateProjects();
    }
    return batchSet;
  }
}
