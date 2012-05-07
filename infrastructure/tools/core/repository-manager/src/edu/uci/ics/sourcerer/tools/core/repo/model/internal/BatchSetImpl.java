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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class BatchSetImpl<Project extends AbstractRepoProject<? extends AbstractRepository<Project, Batch>, ?>, Batch extends BatchImpl<Project>> extends AbstractCollection<Project> {
  private AbstractRepository<Project, Batch> repo;
  
  private TreeMap<Integer, Batch> batches;
  private int size;
  
  protected BatchSetImpl(AbstractRepository<Project, Batch> repo) {
    this.repo = repo;
    batches = Helper.newTreeMap();
    size = 0;
  }
  
  protected Project add(Integer batch, Integer checkout) {
    Batch b = batches.get(batch);
    if (b == null) {
      RepoFileImpl dir = repo.repoRoot.getChild(batch.toString());
      b = repo.newBatch(dir, batch);
      batches.put(batch, b);
    }
    size++;
    return b.add(checkout);
  }
  
  protected Batch createBatch() {
    Integer batch = null;
    if (batches.isEmpty()) {
      batch = Integer.valueOf(0);
    } else {
      batch = batches.lastKey() + 1;
    }
    Batch b = repo.newBatch(repo.repoRoot.getChild(batch.toString()), batch);
    batches.put(batch, b);
    return b;
  }
  
  protected Batch getBatch(Integer batch) {
    return batches.get(batch);
  }
  
  protected Collection<Batch> getBatches() {
    return batches.values();
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
              projectIter = batchIter.next().getProjects().iterator();
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
