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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.base.AbstractJavaFile;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaFile extends AbstractJavaFile {
  private String relativePath;
  private File file;
  private String pkg;
  private boolean packageComputed = false;
  
  public JavaFile(File file) {
    this.file = file;
  }
  
  public JavaFile(String relativePath, File file) {
    this.relativePath = relativePath;
    this.file = file;
  }
  
  private void computePackage() {
    if (!packageComputed) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = br.readLine()) != null) {
          line = line.trim();
          if (line.startsWith("package")) {
            int semi = line.indexOf(';');
            while (semi == -1) {
              String newLine = br.readLine();
              if (newLine == null) {
                newLine = ";";
              }
              line += newLine.trim();
              semi = line.indexOf(';');
            }
            pkg = line.substring(8, line.indexOf(';')).trim();
            break;
          }
        }
        br.close();
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to extract package for file!", e);
      }
      packageComputed = true;
    }
  }
  
  @Override
  public String getPackage() {
    if (!packageComputed) {
      computePackage();
    }
    return pkg;
  }
  
  @Override
  public String getName() {
    return file.getName();
  }
  
  @Override
  public String getProjectRelativePath() {
    return relativePath;
  }
  
  @Override
  public String getPath() {
    return file.getAbsolutePath();
  }
  
  @Override
  public File getFile() {
    return file;
  }
}
