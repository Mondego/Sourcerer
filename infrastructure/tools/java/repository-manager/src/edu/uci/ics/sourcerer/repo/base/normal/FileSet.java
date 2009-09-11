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
package edu.uci.ics.sourcerer.repo.base.normal;

import java.io.File;
import java.util.Collection;
import java.util.Deque;

import edu.uci.ics.sourcerer.repo.RepoJar;
import edu.uci.ics.sourcerer.repo.base.AbstractFileSet;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileSet extends AbstractFileSet {
  private Repository repo;
  public FileSet(File content, Repository repo) {
    populateFileSet(content);
    this.repo = repo;
  }
  
  public String getBasePath() {
    return repo.getBaseDir().getPath();
  }
  
  private void populateFileSet(File content) {
    Deque<File> fileStack = Helper.newStack();
    fileStack.add(content);
    while (!fileStack.isEmpty()) {
      File next = fileStack.pop();
      if (next.exists()) {
        for (File file : next.listFiles()) {
          if (file.isDirectory()) {
            fileStack.push(file);
          } else if (file.getName().endsWith(".jar")) {
            addJarFile(new JarFile(file, RepoJar.getHash(file)));
          } else if (file.getName().endsWith(".java")) {
            addJavaFile(new JavaFile(repo.convertToRelativePath(file, content), file));
          }
        }
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  public Iterable<IJavaFile> convertJavaToConcrete(Collection<JavaFile> files) {
    return (Iterable<IJavaFile>)(Object)files;
  }
}
