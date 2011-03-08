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
import java.util.Deque;

import edu.uci.ics.sourcerer.repo.base.AbstractFileSet;
import edu.uci.ics.sourcerer.repo.base.JarFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.general.RepoFile;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FieldConverter;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileSet extends AbstractFileSet {
  private RepoFile content;
  
  static {
    FieldConverter.registerConverterHelper(RepoFile.class, new FieldConverter.FieldConverterHelper() {
      @Override
      protected Object makeFromString(String value) throws IllegalAccessException {
        return RepoFile.makeFromString(value);
      }
    });
  }
  
  public FileSet(RepoProject project) {
    super(project);
    this.content = project.getContent().makeRoot();
    populateFileSet();
  }
  
  @Override
  protected void buildRepoMapHelper() {
    Deque<File> fileStack = Helper.newStack();
    fileStack.add(content.toFile());
    String basePath = content.toFile().getPath();
    while (!fileStack.isEmpty()) {
      File next = fileStack.pop();
      if (next.exists()) {
        for (File file : next.listFiles()) {
          if (file.isDirectory() && !file.getName().startsWith(".")) {
            fileStack.push(file);
          } else if (file.getName().endsWith(".jar")) {
            addJarFile(new JarFile(FileUtils.computeHash(file), content.getChild(FileUtils.convertToRelativePath(basePath, file.getPath()))));
          } else if (file.getName().endsWith(".java")) {
            addJavaFile(new JavaFile(content.getChild(FileUtils.convertToRelativePath(basePath, file.getPath()))));
          }
        }
      }
    }
  }
  
  @Override
  public String convertToRelativePath(String path) {
    return FileUtils.convertToRelativePath(content.toFile().getAbsolutePath(), path);
  }
}
