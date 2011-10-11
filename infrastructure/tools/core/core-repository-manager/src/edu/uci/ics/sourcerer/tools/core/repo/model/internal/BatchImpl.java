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

import java.util.Collection;
import java.util.TreeMap;

import edu.uci.ics.sourcerer.tools.core.repo.model.BatchProperties;
import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableBatch;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class BatchImpl<Project extends AbstractRepoProject<? extends AbstractRepository<Project, ? extends BatchImpl<Project>>, ?>> implements ModifiableBatch {
  private final AbstractRepository<Project, ?> repo;
  private final RepoFileImpl dir;
  private final Integer batch;
  private final BatchProperties properties;
  private final TreeMap<Integer, Project> projects;
  
  protected BatchImpl(AbstractRepository<Project, ?> repo, RepoFileImpl dir, Integer batch) {
    this.repo = repo;
    this.dir = dir;
    this.batch = batch;
    this.properties = new BatchProperties(dir.getChild(BATCH_PROPERTIES_FILE.getValue()));
    this.projects = Helper.newTreeMap();
  }
  
  protected Project add(Integer checkout) {
    Project project = repo.createProject(new ProjectLocationImpl(batch, checkout, dir.getChild(checkout.toString())));
    projects.put(checkout, project);
    return project;
  }
  
  @Override
  public Project getProject(Integer checkout) {
    return projects.get(checkout);
  }
  
  @Override
  public Collection<Project> getProjects() {
    return projects.values();
  }
  
  @Override
  public int getProjectCount() {
    return projects.size();
  }
  
  @Override
  public Integer getBatchNumber() {
    return batch;
  }

  @Override
  public BatchProperties getProperties() {
    return properties;
  }
  
  
  @Override
  public Project createProject() {
    repo.clearCache();
    Integer nextCheckout = projects.isEmpty() ? 0 : (projects.lastKey() + 1); 
    return repo.addProject(batch, nextCheckout);
  }
}
