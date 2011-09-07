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
package edu.uci.ics.sourcerer.tools.java.repo.model.internal;

import edu.uci.ics.sourcerer.tools.core.repo.model.internal.AbstractRepository;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.BatchImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.ModifiableJavaBatch;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaBatchImpl extends BatchImpl<JavaProjectImpl> implements ModifiableJavaBatch {
  protected JavaBatchImpl(AbstractRepository<JavaProjectImpl, ?> repo, RepoFileImpl dir, Integer batch) {
    super(repo, dir, batch);
  }
}
