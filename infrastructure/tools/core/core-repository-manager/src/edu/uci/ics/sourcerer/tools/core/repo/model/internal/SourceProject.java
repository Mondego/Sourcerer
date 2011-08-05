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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.ISourceProjectM;
import edu.uci.ics.sourcerer.tools.core.repo.model.SourceProjectProperties;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class SourceProject extends RepoProject implements ISourceProjectM {
  private RepoFile contentFile;
  private SoftReference<AbstractFileSet> files; 
  
  private SourceProjectProperties properties;
  
  protected SourceProject(ProjectLocation loc) {
    super(loc);
    contentFile = getProjectFile(PROJECT_CONTENT);
    if (!contentFile.exists()) {
      RepoFile possibleContent = getProjectFile(PROJECT_CONTENT_ZIP);
      if (possibleContent.exists()) {
        contentFile = possibleContent;
      }
    }
  }
  
  @Override
  public SourceProjectProperties getProperties() {
    if (properties == null) {
      properties = new SourceProjectProperties(propFile);
    }
    return properties;
  }
  
  @Override
  public boolean deleteContent() {
    return contentFile.delete();
  }

  @Override
  public boolean addContent(File file, boolean move) {
    boolean success = false;
    if (move) {
      success = FileUtils.moveFile(file, contentFile.toFile());
    } else {
      success = FileUtils.copyFile(file, contentFile.toFile());
    }
    if (!success) {
      logger.log(Level.SEVERE, "Unable to move/copy content from " + file.getPath() + " to " + contentFile.toFile().getPath());
    }
    
    if (files != null) {
      AbstractFileSet fileSet = files.get();
      if (fileSet != null) {
        fileSet.reset();
      }
    }
    
    return success;
  }
  
  @Override
  public boolean hasContent() {
    return contentFile.exists();
  }
  
  @Override
  public AbstractFileSet getContent() {
    AbstractFileSet ret = null;
    if (files != null) {
      ret = files.get();
    }
    if (ret == null) {
      ret = new FileSet(this);
      files = new SoftReference<AbstractFileSet>(ret);
    }
    return ret;
  }
  
  protected RepoFile getContentFile() {
    return contentFile;
  }
}
