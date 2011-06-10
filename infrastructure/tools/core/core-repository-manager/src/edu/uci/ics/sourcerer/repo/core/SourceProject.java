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

import java.io.File;
import java.util.Collection;

import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SourceProject extends RepoProject {
  public static final Argument<String> PROJECT_CONTENT = new StringArgument("project-content-dir", "content", "Project contents.");
  public static final Argument<String> PROJECT_CONTENT_ZIP = new StringArgument("project-content-zip-file", "content.zip", "Project contents.");
  
  private RepoFile contentFile;
  
  private SourceProjectProperties properties;
  
  protected SourceProject(ProjectLocation loc) {
    super(loc);
    RepoFile possibleContent = getProjectFile(PROJECT_CONTENT);
    if (possibleContent.exists()) {
      contentFile = possibleContent;
    } else {
      possibleContent = getProjectFile(PROJECT_CONTENT_ZIP);
      if (possibleContent.exists()) {
        contentFile = possibleContent;
      } else {
        contentFile = null;
      }
    }
  }
  
  public SourceProjectProperties getProperties() {
    if (properties == null) {
      properties = new SourceProjectProperties(propFile);
    }
    return properties;
  }
  
  /**
   * Deletes the project's contents.
   *  
   * @return <tt>true</tt> if successful
   */
  public boolean deleteContent() {
    if (contentFile != null) {
      return contentFile.delete();
    } else {
      return true;
    }
  }
  
  /**
   * Copies the contents of <tt>file</tt> into the
   * project's <tt>content</tt>> directory. Will
   * not work if the project's contents are compressed.
   * 
   * This will not overwrite anything.
   */
  public void addContent(File file) {
    if (contentFile == null) {
      contentFile = getProjectFile(PROJECT_CONTENT);
    }
    if (contentFile.isDirectory()) {
      FileUtils.copyFile(file, contentFile.toDir());
    } else {
      throw new IllegalStateException("May not add content to a compressed project.");
    }
  }
  
  public void zipContent() {}
  
  public FileSet getContent() {
    return null;
  }
  
  protected RepoFile getContentFile() {
    if (contentFile == null) {
      contentFile = getProjectFile(PROJECT_CONTENT);
    }
    return contentFile;
  }
}
