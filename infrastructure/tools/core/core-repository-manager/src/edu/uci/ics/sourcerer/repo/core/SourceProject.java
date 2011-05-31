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
package edu.uci.ics.sourcerer.repo.core;

import edu.uci.ics.sourcerer.util.io.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SourceProject extends RepoProject {
  public static final Argument<String> PROJECT_CONTENT = new StringArgument("project-content-dir", "content", "Project contents.");
  public static final Argument<String> PROJECT_CONTENT_ZIP = new StringArgument("project-content-zip-file", "content.zip", "Project contents.");
  
  private final RepoFile content;
  
  protected SourceProject(ProjectLocation loc) {
    super(loc);
    RepoFile possibleContent = loc.getProjectRoot().getChild(PROJECT_CONTENT.getValue());
    if (possibleContent.exists()) {
      content = possibleContent;
    } else {
      possibleContent = loc.getProjectRoot().getChild(PROJECT_CONTENT_ZIP.getValue());
      if (possibleContent.exists()) {
        content = possibleContent;
      } else {
        content = null;
      }
    }
  }
}
