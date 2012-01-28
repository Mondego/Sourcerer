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

import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceRepository;
import edu.uci.ics.sourcerer.tools.core.repo.model.RepositoryFactory;
import edu.uci.ics.sourcerer.tools.core.repo.model.SourceRepository;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class InternalRepositoryFactory implements RepositoryFactory {
  @Override
  public SourceRepository loadSourceRepository(Argument<File> root) {
    logger.info("Loading source repository at: " + root.getValue().getPath());
    return SourceRepositoryImpl.load(RepoFileImpl.makeRoot(root));
  }

  @Override
  public ModifiableSourceRepository loadModifiableSourceRepository(Argument<File> root) {
    logger.info("Loading modifiable source repository at: " + root.getValue().getPath());
    return SourceRepositoryImpl.load(RepoFileImpl.makeRoot(root));
  }
}
