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
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceProject;
import edu.uci.ics.sourcerer.tools.core.repo.model.SourceProjectProperties;
import edu.uci.ics.sourcerer.util.CachedReference;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractSourceProject<Repo extends AbstractRepository<?, ?>, FileSet extends AbstractFileSet> extends AbstractRepoProject<Repo, SourceProjectProperties> implements ModifiableSourceProject {
  private RepoFileImpl contentFile;
  private CachedReference<FileSet> files = new CachedReference<FileSet>() {
    @Override
    protected FileSet create() {
      return makeFileSet();
    }
  }; 
  
  protected AbstractSourceProject(Repo repo, ProjectLocationImpl loc) {
    super(repo, loc);
    contentFile = getProjectFile(PROJECT_CONTENT);
    if (!contentFile.exists()) {
      RepoFileImpl possibleContent = getProjectFile(PROJECT_CONTENT_ZIP);
      if (possibleContent.exists()) {
        contentFile = possibleContent;
      }
    }
  }
  
  @Override
  protected SourceProjectProperties makeProperties(RepoFileImpl propFile) {
    return new SourceProjectProperties(propFile);
  }
  
  @Override
  public boolean deleteContent() {
    return contentFile.delete();
  }

  @Override
  public void delete(DeletionFilter filter) {
    getContent().delete(filter);
  }

  @Override
  public boolean addContent(File file) {
    if (FileUtils.copyFile(file, contentFile.toFile())) {
      if (files != null) {
        AbstractFileSet fileSet = files.get();
        if (fileSet != null) {
          fileSet.init(false, true);
        }
      }
      return true;
    } else {
      logger.log(Level.SEVERE, "Unable to copy content from " + file.getPath() + " to " + contentFile.toFile().getPath());
      return false;
    }
  }
  
  @Override
  public boolean addContent(ContentAdder adder) {
    if (adder.addContent(contentFile.toFile())) {
      if (files != null) {
        AbstractFileSet fileSet = files.get();
        if (fileSet != null) {
          fileSet.init(false, true);
        }
      }
      return true;
    } else {
      logger.log(Level.SEVERE, "Unable to add content to " + contentFile.toFile().getPath());
      return false;
    }
  }
  
  @Override
  public boolean hasContent() {
    return contentFile.exists();
  }
    
  protected abstract FileSet makeFileSet();
  
  @Override
  public FileSet getContent() {
    return files.get();
  }
  
  protected RepoFileImpl getContentFile() {
    return contentFile;
  }
}
