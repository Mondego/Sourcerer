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
package edu.uci.ics.sourcerer.repo.internal.core;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.core.IFileSet;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.LineFileReader;
import edu.uci.ics.sourcerer.util.io.LineFileWriter;
import edu.uci.ics.sourcerer.util.io.LineFileWriter.EntryWriter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractFileSet implements IFileSet {
  private final File cache;
  private ContentDirectory root;
  private Collection<ContentFile> files;
  
  protected AbstractFileSet(SourceProject project) {
    cache = project.getLocation().getProjectRoot().getChildFile(FILE_CACHE.getValue());
    root = makeRoot(project.getContentFile());
    files = Helper.newArrayList();
    
    if (CLEAR_FILE_CACHE.getValue() || !cache.exists() || !readCache()) {
      populateFileSet();
    }
  }
  
  protected final void reset() {
    root = makeRoot(root.getFile());
    files.clear();
    populateFileSet();
  }
  
  private final void populateFileSet() {
    Deque<RepoFile> stack = Helper.newStack();
    stack.push(getRoot().getFile());
      
    while (!stack.isEmpty()) {
      RepoFile dir = stack.pop();
      for (RepoFile child : dir.getChildren()) {
        if (child.isDirectory()) {
          stack.push(child);
        } else {
          addFile(child);
        }
      }
    }
    
    LineFileWriter writer = null;
    try {
      writer = FileUtils.getLineFileWriter(cache);
      EntryWriter<RepoFile> ew = writer.getEntryWriter(RepoFile.class);
      for (ContentFile file : files) {
        ew.write(file.getFile());
      }
      ew.close();
      return;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write file cache", e);
    } finally {
      FileUtils.close(writer);
    }
    FileUtils.delete(cache);
  }
  
  private final boolean readCache() {
    LineFileReader reader = null;
    try {
      reader = FileUtils.getLineFileReader(cache);
      for (RepoFile file : reader.readNextToIterable(RepoFile.class)) {
        addFile(file);
      }
      return true;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to load file cache", e);
      root = makeRoot(root.getFile());
      files.clear();
      return false;
    } finally {
      FileUtils.close(reader);
    }
  }
  
  protected final void addFile(RepoFile file) {
    files.add(createFile(file));
  }

  protected abstract ContentDirectory makeRoot(RepoFile file);
  protected abstract ContentFile createFile(RepoFile file);
  
  public Collection<ContentFile> getFiles() {
    return files;
  }
  
  public ContentDirectory getRoot() {
    return root;
  }
}
