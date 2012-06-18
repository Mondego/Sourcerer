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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.FileSet;
import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceProject.DeletionFilter;
import edu.uci.ics.sourcerer.util.io.EntryWriter;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.SimpleDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractFileSet implements FileSet {
  public static final Argument<String> CACHE_DIR = new StringArgument("cache-dir", "cache", "Directory containing project cache files.").permit();
  public static final Argument<String> FILE_CACHE = new StringArgument("file-cache-file", "file-cache.txt", "Cache of the file set's files.").permit();
  
  protected final File cacheDir;
  private final File cache;
  private ContentDirectoryImpl root;
  private Collection<ContentFileImpl> files;
  
  protected AbstractFileSet(AbstractSourceProject<?, ?> project) {
    cacheDir = project.getLocation().getProjectRoot().getChildFile(CACHE_DIR.getValue());
    cache = new File(cacheDir, FILE_CACHE.getValue());
    root = ContentDirectoryImpl.makeRoot(project.getContentFile());
  }
  
  public void delete(DeletionFilter filter) {
    init(false, false);
    Deque<RepoFileImpl> stack = new LinkedList<>();
    stack.push(root.getFile());
    boolean deleted = false;
    while (!stack.isEmpty()) {
      RepoFileImpl dir = stack.pop();
      if (filter.shouldDelete(dir)) {
        dir.delete();
        deleted = true;
      } else {
        for (RepoFileImpl child : dir.getChildren()) {
          if (child.isDirectory()) {
            stack.push(child);
          } else {
            if (filter.shouldDelete(child)) {
              child.delete();
              deleted = true;
            }
          }
        }
      }
    }
    if (deleted) {
      init(false, true);
    }
  }
  
  public void init(boolean loadNow, boolean clearCache) {
    if (clearCache) {
      FileUtils.delete(cacheDir);
    }
    root = ContentDirectoryImpl.makeRoot(root.getFile());
    if (loadNow) {
      if (files == null) {
        files = new ArrayList<>();
      } else {
        files.clear();
      }
      populateFileSet();
    } else {
      files = null;
    }
  }

  protected void populateFileSet() {
    if (AbstractRepository.CLEAR_CACHES.getValue() || !cache.exists() || !readCache()) {
      Deque<RepoFileImpl> stack = new LinkedList<>();
      stack.add(root.getFile());
      while (!stack.isEmpty()) {
        RepoFileImpl dir = stack.pop();
        Collection<RepoFileImpl> children = dir.getChildren();
        // See if this directory has a "trunk"
        RepoFileImpl trunk = null;
        for (RepoFileImpl child : children) {
          if (child.isDirectory() && child.getName().equals("trunk")) {
            trunk = child;
            break;
          }
        }
        // If there's a trunk, follow only that
        if (trunk != null) {
          stack.push(trunk);
        } else {
          for (RepoFileImpl child : children) {          
            if (child.isDirectory()) {
              stack.push(child);
            } else {
              addFile(child);
            }
          }
        }
      }
      
      cache.getParentFile().mkdirs();
      try (SimpleSerializer writer = IOUtils.makeSimpleSerializer(cache)) {
        EntryWriter<RepoFileImpl> ew = writer.getEntryWriter(RepoFileImpl.class);
        for (ContentFileImpl file : files) {
          ew.write(file.getFile());
        }
        ew.close();
        return;
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to write file cache", e);
      }
      FileUtils.delete(cache);
    }
  }
 
  private boolean readCache() {
    try (SimpleDeserializer reader = IOUtils.makeSimpleDeserializer(cache)) {
      for (RepoFileImpl file : reader.deserializeToIterable(root.getFile().makeDeserializer(), false)) {
        addFile(file);
      }
      return true;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to load file cache: " + getRoot(), e);
      root = ContentDirectoryImpl.makeRoot(root.getFile());
      files.clear();
      return false;
    }
  }
  
  private final void addFile(RepoFileImpl file) {
    ContentFileImpl cFile = getRoot().make(file.getParent()).makeFile(file);
    files.add(cFile);
    fileAdded(cFile);
  }
  
  protected abstract void fileAdded(ContentFileImpl file);
  
  public Collection<ContentFileImpl> getFiles() {
    if (files == null) {
      init(true, false);
    }
    return Collections.unmodifiableCollection(files);
  }
  
  public ContentDirectoryImpl getRoot() {
    if (files == null) {
      init(true, false);
    }
    return root;
  }
  
  public ContentFileImpl getFile(String path) {
    RepoFileImpl file = root.getFile().getChild(path);
    return getRoot().make(file.getParent()).makeFile(file);
  }
}
