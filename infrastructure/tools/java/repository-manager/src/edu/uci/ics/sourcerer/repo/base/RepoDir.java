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

import java.util.Collection;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepoDir implements IDirectory {
  private String name;
  private RepoDir parent;
  private Collection<IDirectory> subDirs;
  private Collection<IJavaFile> javaFiles;
  
  protected RepoDir(String name) {
    this.name = name;
    subDirs = Helper.newLinkedList();
    javaFiles = Helper.newLinkedList();
  }
  
  protected RepoDir(String name, RepoDir parent) {
    this(name);
    this.parent = parent;
    parent.subDirs.add(this);
  }
  
  public String getName() {
    return name;
  }
  
  public Iterable<IDirectory> getSubdirectories() {
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
