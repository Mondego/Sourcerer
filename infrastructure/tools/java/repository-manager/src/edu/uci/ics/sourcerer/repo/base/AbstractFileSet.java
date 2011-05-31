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
package edu.uci.ics.sourcerer.repo.base;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.Iterators;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.LineFileReader;
import edu.uci.ics.sourcerer.util.io.LineFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractFileSet implements IFileSet {
  public static final Argument<String> FILE_CACHE = new StringArgument("file-cache-file", "file-cache.txt", "Cache of the file set's files.");
  public static final Argument<Boolean> CLEAR_FILE_CACHE = new BooleanArgument("clear-file-cache", false, "Clears the file caches.");
  
  private File cache;
  
  private Collection<IJarFile> jarFiles;
  
  private Collection<IDirectory> roots;
  private Map<String, RepoDir> repoMap;
  
  private Collection<IJavaFile> uniqueFiles = null;
  private Collection<IJavaFile> bestDuplicateFiles = null; 
  private int fileCount = 0;
  private int discardedDuplicateCount = 0;
  
  protected AbstractFileSet(RepoProject project) {
    cache = project.getProjectRoot().getChildFile(FILE_CACHE.getValue()); 
  }
  
  protected final void populateFileSet() {
    if (CLEAR_FILE_CACHE.getValue() || !cache.exists()) {
      cache.delete();
      buildRepoMap();
    } else {
      LineFileReader reader = null;
      try {
        reader = FileUtils.getLineFileReader(cache);
        jarFiles = reader.readNextToCollection(IJarFile.class);
        uniqueFiles = reader.readNextToCollection(IJavaFile.class);
        bestDuplicateFiles = reader.readNextToCollection(IJavaFile.class);
        fileCount = reader.readNextToInt();
        discardedDuplicateCount = reader.readNextToInt();
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to load file cache", e);
        jarFiles.clear();
        uniqueFiles = null;
        bestDuplicateFiles = null;
        buildRepoMap();
      } finally {
        FileUtils.close(reader);
      }
    }
  }
  
  protected void buildRepoMap() {
    jarFiles = Helper.newLinkedList();
    roots = Helper.newLinkedList();
    repoMap = Helper.newHashMap();
    buildRepoMapHelper();
  }
  
  protected abstract void buildRepoMapHelper();
  
  protected final void addJarFile(IJarFile file) {
    jarFiles.add(file);
  }
  
  protected final void addJavaFile(IJavaFile file) {
    fileCount++;
    String dir = FileUtils.stripFileName(file.getFile().getRelativePath());
    RepoDir repoDir = repoMap.get(dir);
    if (repoDir == null) {
      Deque<String> dirStack = Helper.newStack();
      while (repoDir == null) {
        int index = dir.lastIndexOf('/');
        if (index == -1) {
          repoDir = new RepoDir(dir);
          repoMap.put(dir, repoDir);
          roots.add(repoDir);
        } else {
          dirStack.push(dir);
          dir = dir.substring(0, index);
          repoDir = repoMap.get(dir);
        }
      }
      for (String newDir : dirStack) {
        repoDir = new RepoDir(newDir.substring(newDir.lastIndexOf('/') + 1), repoDir);
        repoMap.put(newDir, repoDir);
      }
    }
    repoDir.addJavaFile(file);
  }
  
  public final int getJarFileCount() {
    return jarFiles.size();
  }
  
  @Override
  public final Iterable<IJarFile> getJarFiles() {
    return jarFiles;
  }
  
  private void computeFiles() {
    uniqueFiles = Helper.newLinkedList();
    bestDuplicateFiles = Helper.newLinkedList();
    
    Deque<IDirectory> start = Helper.newStack();
    
    // See if trunk is a root
    for (IDirectory root : roots) {
      if (root.getName().equals("trunk")) {
        start.push(root);
        break;
      }
    }
    
    // If no trunk
    if (start.isEmpty()) {
      // Check two levels lower
      for (IDirectory root : roots) {
        for (IDirectory subRoot : root.getSubdirectories()) {
          if (subRoot.getName().equals("trunk")) {
            start.push(subRoot);
          } else {
            for (IDirectory subSubRoot : subRoot.getSubdirectories()) {
              if (subSubRoot.getName().equals("trunk")) {
                start.push(subSubRoot);
              }
            }
          }
        }
      }
    }
    
    if (start.isEmpty()) {
      start.addAll(roots);
    }
    
    Map<String, Collection<IJavaFile>> javaFiles = Helper.newHashMap();
    
    while (!start.isEmpty()) {
      IDirectory next = start.pop();
      for (IJavaFile java : next.getJavaFiles()) {
        Collection<IJavaFile> files = javaFiles.get(java.getKey());
        if (files == null) {
          files = Helper.newLinkedList();
          javaFiles.put(java.getKey(), files);
        }
        files.add(java);
      }
      for (IDirectory subDir : next.getSubdirectories()) {
        start.add(subDir);
      }
    }
  
    for (Collection<IJavaFile> files : javaFiles.values()) {
      if (files.size() == 1) {
        IJavaFile next = files.iterator().next();
        uniqueFiles.add(next);
      } else {
        IJavaFile best = null;
        int bestValue = 0;
        for (IJavaFile file : files) {
          int value = getValue(file);
          if (value > bestValue) {
            best = file;
            bestValue = value;
          }
        }
        if (best != null) {
          bestDuplicateFiles.add(best);
          discardedDuplicateCount += files.size() - 1;
        } else {
          logger.log(Level.SEVERE, "There should always be a best!");
        }
      }
    }
    
    // Store in the cache
    LineFileWriter writer = null;
    try {
      writer = FileUtils.getLineFileWriter(cache);
      writer.write(jarFiles);
      writer.write(uniqueFiles);
      writer.write(bestDuplicateFiles);
      writer.write(fileCount);
      writer.write(discardedDuplicateCount);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write cache.", e);
    } finally {
      FileUtils.close(writer);
    }
  }
  
  @Override
  public final Iterable<IDirectory> getRootDirectories() {
    if (roots == null) {
      buildRepoMap();
    }
    return roots;
  }
  
  @Override
  public final int getJavaFileCount() {
    return fileCount;
  }
  
  @Override
  public final int getFilteredJavaFileCount() {
    if (uniqueFiles == null || bestDuplicateFiles == null) {
      computeFiles();
    }
    return uniqueFiles.size() + bestDuplicateFiles.size();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public final Iterable<IJavaFile> getFilteredJavaFiles() {
    if (uniqueFiles == null || bestDuplicateFiles == null) {
      computeFiles();
    }
    return Iterators.conact(uniqueFiles, bestDuplicateFiles);
  }
  
  @Override
  public final int getUniqueJavaFileCount() {
    if (uniqueFiles == null) {
      computeFiles();
    }
    return uniqueFiles.size();
  }
  
  @Override
  public final Iterable<IJavaFile> getUniqueJavaFiles() {
    if (uniqueFiles == null) {
      computeFiles();
    }
    return uniqueFiles;
  }
  
  @Override
  public final int getBestDuplicateJavaFileCount() {
    if (bestDuplicateFiles == null) {
      computeFiles();
    }
    return bestDuplicateFiles.size();
  }
  
  @Override
  public final Iterable<IJavaFile> getBestDuplicateJavaFiles() {
    if (bestDuplicateFiles == null) {
      computeFiles();
    }
    return bestDuplicateFiles;
  }
  
  public final int getDiscardedDuplicateFileCount() {
    return discardedDuplicateCount;
  }
  
  
  private int getValue(IJavaFile file) {
    RepoDir repoDir = repoMap.get(FileUtils.stripFileName(file.getFile().getRelativePath()));
    int count = 0;
    while (repoDir != null) {
      count += repoDir.getCount();
      repoDir = repoDir.getParent();
    }
    return count;
  }
}

