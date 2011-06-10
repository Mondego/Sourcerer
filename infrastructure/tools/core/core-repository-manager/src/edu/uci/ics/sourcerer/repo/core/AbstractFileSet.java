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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.LineFileReader;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractFileSet {
  public static final Argument<String> FILE_CACHE = new StringArgument("file-cache-file", "file-cache.txt", "Cache of the file set's files.");
  public static final Argument<Boolean> CLEAR_FILE_CACHE = new BooleanArgument("clear-file-cache", false, "Clears the file caches.");
  
  private final File cache;
  private ContentDirectory root;
  private Collection<ContentFile> files;
  
  public AbstractFileSet(SourceProject project) {
    cache = project.getLocation().getProjectRoot().getChildFile(FILE_CACHE.getValue());
    root = ContentDirectory.makeRoot(project.getContentFile());
    populateFileSet();
  }
  
  private final void populateFileSet() {
    if (CLEAR_FILE_CACHE.getValue() || !cache.exists()) {
      populateFileSetHelper();
    } else {
      readCache();
    }
  }
  
  protected abstract void populateFileSetHelper();

  private final void readCache() {
    LineFileReader reader = null;
    try {
      reader = FileUtils.getLineFileReader(cache);
      for (RepoFile file : reader.readNextToIterable(RepoFile.class)) {
        root.make(file);
      }
      for (RepoFile file : reader.readNextToIterable(RepoFile.class)) {
        addFile(file);
      }
      readCacheHelper(reader);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to load file cache", e);
      FileUtils.close(reader);
      populateFileSetHelper();
    } finally {
      FileUtils.close(reader);
    }
  }
  
  protected void readCacheHelper(LineFileReader reader) throws IOException {
    
  }
  
  private final void addFile(RepoFile file) {
    ContentFile cFile = createFile(file);
    
  }

  protected abstract ContentFile createFile(RepoFile file);
}
