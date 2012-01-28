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

import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceRepository;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class SourceRepositoryImpl extends AbstractRepository<SourceProjectImpl, SourceBatchImpl<SourceProjectImpl>> implements ModifiableSourceRepository {
  private SourceRepositoryImpl(RepoFileImpl repoRoot) {
    super(repoRoot);
  }
  
  public static SourceRepositoryImpl load(RepoFileImpl repoRoot) {
    SourceRepositoryImpl repo = new SourceRepositoryImpl(repoRoot);
    // Verify the repository type
    String type = repo.properties.REPOSITORY_TYPE.getValue();
    // If it's a new repo
    if (type == null) {
      repo.properties.REPOSITORY_TYPE.setValue(ModifiableSourceRepository.class.getName());
      repo.properties.save();
      return repo;
    } else if (type.equals(ModifiableSourceRepository.class.getName())) {
      return repo;
    } else {
      logger.severe("Invalid repository type: " + type);
      return null;
    }
  }
  

  @Override
  protected SourceProjectImpl createProject(ProjectLocationImpl loc) {
    return new SourceProjectImpl(this, loc);
  }
  
  @Override
  public SourceBatchImpl<SourceProjectImpl> newBatch(RepoFileImpl dir, Integer batch) {
    return new SourceBatchImpl<SourceProjectImpl>(this, dir, batch);
  }
}
