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

import edu.uci.ics.sourcerer.tools.core.repo.model.ProjectLocation;
import edu.uci.ics.sourcerer.util.io.Ignore;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProjectLocationImpl implements ProjectLocation, SimpleSerializable {
  private Integer batch;
  private Integer checkout;
  private @Ignore RepoFileImpl projectRoot;
  
  protected ProjectLocationImpl() {}
  
  protected ProjectLocationImpl(Integer batch, Integer checkout, RepoFileImpl projectRoot) {
    this.batch = batch;
    this.checkout = checkout;
    this.projectRoot = projectRoot;
  }
  
  @Override
  public RepoFileImpl getProjectRoot() {
    return projectRoot;
  }
  
  public Integer getBatchNumber() {
    return batch;
  }
  
  public Integer getCheckoutNumber() {
    return checkout;
  }
  
  @Override
  public String toString() {
    return batch + "/" + checkout;
  }
}
