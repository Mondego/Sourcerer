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
package edu.uci.ics.sourcerer.tools.java.repo.model.extracted.internal;

import edu.uci.ics.sourcerer.tools.core.repo.model.internal.ProjectLocationImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoProjectImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProjectProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJavaProject;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class ExtractedJavaProjectImpl extends RepoProjectImpl<ExtractedJavaRepositoryImpl> implements ModifiableExtractedJavaProject {
  private ExtractedJavaProjectProperties properties;
  
  private ExtractedJavaProjectImpl(ExtractedJavaRepositoryImpl repo, ProjectLocationImpl loc) {
    super(repo, loc);
  }

  static ExtractedJavaProjectImpl make(ExtractedJavaRepositoryImpl repo, ProjectLocationImpl loc) {
    ExtractedJavaProjectImpl project = new ExtractedJavaProjectImpl(repo, loc);
    if (!Boolean.TRUE.equals(project.getProperties().EXTRACTED.getValue())) {
      loc.getProjectRoot().delete();
    }
    return project;
  }
  
  @Override
  public void reset(JavaProject project) {
    loc.getProjectRoot().delete();
    loc.getProjectRoot().makeDirs();
    getProperties().clear();
    properties.copy(project.getProperties());
    properties.save();
  }

  @Override
  public ExtractedJavaProjectProperties getProperties() {
    if (properties == null) {
      properties = new ExtractedJavaProjectProperties(propFile);
    }
    return properties;
  }
  
  @Override
  public RepoFileImpl getExtractionDir() {
    return loc.getProjectRoot();
  }
}
