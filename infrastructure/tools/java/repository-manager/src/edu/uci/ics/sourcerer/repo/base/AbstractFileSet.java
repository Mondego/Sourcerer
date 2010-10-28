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
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.AbstractRepository;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractFileSet implements IFileSet {
  protected AbstractRepository repo;
  
  private Collection<IJarFile> jarFiles;
  
  private Collection<RepoDir> roots;
  private Map<String, RepoDir> repoMap;
  
  private Collection<IJavaFile> uniqueFiles = null;
  private Collection<IJavaFile> bestDuplicateFiles = null; 
  
  protected AbstractFileSet(AbstractRepository repo) {
    this.repo = repo;
    jarFiles = Helper.newLinkedList();
    roots = Helper.newLinkedList();
    repoMap = Helper.newHashMap();
  }
  
  protected final void addJarFile(IJarFile file) {
    jarFiles.add(file);
  }
  
  protected final void addJavaFile(IJavaFile file) {
    String dir = file.getProjectRelativePath();
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
    
    Deque<RepoDir> start = Helper.newStack();
    
    // See if trunk is a root
    for (RepoDir root : roots) {
      if (root.getName().equals("trunk")) {
        start.push(root);
        break;
      }
    }
    
    // If no trunk
    if (start.isEmpty()) {
      // Check two levels lower
      for (RepoDir root : roots) {
        for (RepoDir subRoot : root.getSubdirs()) {
          if (subRoot.getName().equals("trunk")) {
            start.push(subRoot);
          } else {
            for (RepoDir subSubRoot : subRoot.getSubdirs()) {
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
      RepoDir next = start.pop();
      for (IJavaFile java : next.getJavaFiles()) {
        Collection<IJavaFile> files = javaFiles.get(java.getKey());
        if (files == null) {
          files = Helper.newLinkedList();
          javaFiles.put(java.getKey(), files);
        }
        files.add(java);
      }
      start.addAll(next.getSubdirs());
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
        } else {
          logger.log(Level.SEVERE, "There should always be a best!");
        }
      }
    }
  }
  
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
  
  private int getValue(IJavaFile file) {
    RepoDir repoDir = repoMap.get(file.getProjectRelativePath());
    int count = 0;
    while (repoDir != null) {
      count += repoDir.getCount();
      repoDir = repoDir.getParent();
    }
    return count;
  }

  @Override
  public String convertToRelativePath(String path) {
    return convertToRelativePath(path, repo.getBaseDir().getPath());
  }
  
  @Override
  public String convertToRelativePath(File file, File base) {
    return convertToRelativePath(file.getAbsolutePath(), base.getAbsolutePath());
  }
  
  @Override
  public String convertToRelativePath(String path, String base) {
    path = path.replace('\\', '/');
    base = base.replace('\\', '/');
    if (base == null) {
      return path.replace(' ', '*');
    } else {
      if (path.startsWith(base)) {
        return path.substring(base.length()).replace(' ', '*');
      } else {
        logger.severe("Unable to convert " + path + " to relative path (" + base + ")");
        return path.replace(' ', '*');
      }
    }
  }
  
  private class RepoDir {
    private String name;
    private RepoDir parent;
    private Collection<RepoDir> subDirs;
    private Collection<IJavaFile> javaFiles;
    
    public RepoDir(String name) {
      this.name = name;
      subDirs = Helper.newLinkedList();
      javaFiles = Helper.newLinkedList();
    }
    
    public RepoDir(String name, RepoDir parent) {
      this(name);
      this.parent = parent;
      parent.subDirs.add(this);
    }
    
    public String getName() {
      return name;
    }
    
    public Collection<RepoDir> getSubdirs() {
      return subDirs;
    }
    
    public void addJavaFile(IJavaFile file) {
      javaFiles.add(file);
    }
    
    public Collection<IJavaFile> getJavaFiles() {
      return javaFiles;
    }
    
    public int getCount() {
      return javaFiles.size();
    }
    
    public RepoDir getParent() {
      return parent;
    }
  }
}

