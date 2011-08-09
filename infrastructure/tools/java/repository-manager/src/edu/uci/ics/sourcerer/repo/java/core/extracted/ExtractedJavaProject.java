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
package edu.uci.ics.sourcerer.repo.java.core.extracted;

import edu.uci.ics.sourcerer.tools.core.repo.model.internal.ProjectLocationImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoProjectImpl;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedJavaProject extends RepoProjectImpl {
  private ExtractedJavaProjectProperties properties;
  
  protected ExtractedJavaProject(ProjectLocationImpl loc) {
    super(loc);
  }
  
  public ExtractedJavaProjectProperties getProperties() {
    if (properties == null) {
      properties = new ExtractedJavaProjectProperties(propFile);
    }
    return properties;
  }
}
