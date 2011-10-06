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
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.FileSet;
import edu.uci.ics.sourcerer.util.Helper;
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
  public static final Argument<String> FILE_CACHE = new StringArgument("file-cache-file", "file-cache.txt", "Cache of the file set's files.").permit();
  
  private final File cache;
  private ContentDirectoryImpl root;
  private Collection<ContentFileImpl> files;
  
  protected AbstractFileSet(AbstractSourceProject<?, ?> project) {
    cache = project.getLocation().getProjectRoot().getChildFile(FILE_CACHE.getValue());
    root = ContentDirectoryImpl.makeRoot(project.getContentFile());
    
  }

  public final void init(boolean loadNow, boolean clearCache) {
    if (clearCache) {
      cache.delete();
    }
    root = ContentDirectoryImpl.makeRoot(root.getFile());
    if (loadNow) {
      if (files == null) {
        files = Helper.newArrayList();
      } else {
        files.clear();
      }
      initHelper(loadNow);
      populateFileSet();
    } else {
      files = null;
      initHelper(loadNow);
    }
  }
  
  protected abstract void initHelper(boolean loadNow);
  
  private final void populateFileSet() {
    if (AbstractRepository.CLEAR_CACHES.getValue() || !cache.exists() || !readCache()) {
      Deque<RepoFileImpl> stack = Helper.newStack();
      stack.push(getRoot().getFile());
        
      while (!stack.isEmpty()) {
        RepoFileImpl dir = stack.pop();
        for (RepoFileImpl child : dir.getChildren()) {
          if (child.isDirectory()) {
            stack.push(child);
          } else {
            addFile(child);
          }
        }
      }
      populateFileSetHelper();
      
      SimpleSerializer writer = null;
      try {
        cache.getParentFile().mkdirs();
        writer = IOUtils.makeSimpleSerializer(cache);
        EntryWriter<RepoFileImpl> ew = writer.getEntryWriter(RepoFileImpl.class);
        for (ContentFileImpl file : files) {
          ew.write(file.getFile());
        }
        ew.close();
        writeExtendedCache(writer);
        return;
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to write file cache", e);
      } finally {
        IOUtils.close(writer);
      }
      FileUtils.delete(cache);
    }
  }
  
  protected abstract void populateFileSetHelper();
  
  protected abstract void writeExtendedCache(SimpleSerializer serializer) throws IOException;
  
  private final boolean readCache() {
    SimpleDeserializer reader = null;
    try {
      reader = IOUtils.makeSimpleDeserializer(cache);
      for (RepoFileImpl file : reader.deserializeToIterable(root.getFile().makeDeserializer(), false)) {
        addFile(file);
      }
      readExtendedCache(reader);
      return true;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to load file cache", e);
      root = ContentDirectoryImpl.makeRoot(root.getFile());
      files.clear();
      return false;
    } finally {
      IOUtils.close(reader);
    }
  }
  
  protected abstract void readExtendedCache(SimpleDeserializer deserializer) throws IOException;
  
  private final void addFile(RepoFileImpl file) {
    ContentFileImpl cFile = getRoot().make(file.getParent()).makeFile(file);
    files.add(cFile);
    fileAdded(cFile);
  }
  
  protected abstract void fileAdded(ContentFileImpl file);

//  protected ContentDirectoryImpl makeRoot(RepoFileImpl file) {
//    return ContentDirectoryImpl.makeRoot(file);
//  }
  
//  protected ContentFileImpl createFile(RepoFileImpl file) {
//    return new ContentFileImpl(getRoot().make(file.getParent()), file);
//  }
  
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
}
