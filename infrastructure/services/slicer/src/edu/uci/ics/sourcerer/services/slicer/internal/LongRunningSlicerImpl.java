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
package edu.uci.ics.sourcerer.services.slicer.internal;

import edu.uci.ics.sourcerer.util.TimeoutManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LongRunningSlicerImpl extends AbstractSlicerImpl {
  private TimeoutManager<SlicerDatabaseAccessor> accessorManager =
      new TimeoutManager<SlicerDatabaseAccessor>(new TimeoutManager.Instantiator<SlicerDatabaseAccessor>() {
        @Override
        public SlicerDatabaseAccessor create() {
          return SlicerDatabaseAccessor.create();
        }
      }, 10 * 60 * 1000);
  

  @Override
  protected SlicerDatabaseAccessor getAccessor() {
    return accessorManager.get();
  }
  
  @Override
  public void close() {
    accessorManager.destroy();

  }
}
