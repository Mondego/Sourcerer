// Sourcerer: an infrastructure for large-scale source code analysis.
// Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
package edu.uci.ics.sourcerer.repo.base;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaFile implements IVirtualJavaFile, IJavaFile {
  private String dir;
  private File file;
  private String pkg;
  public JavaFile(String dir, File file) {
    this.dir = dir;
    this.file = file;
    
    try {
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line = null;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.startsWith("package")) {
          int semi = line.indexOf(';');
          while (semi == -1) {
            line += br.readLine().trim();
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
  }
  
  public JavaFile(String dir, String pkg, File file) {
    this.dir = dir;
    this.pkg = pkg;
    this.file = file;
  }
  
  @Override
  public String getDir() {
    return dir;
  }
  
  public String getPackage() {
    return pkg;
  }
  
  public String getName() {
    return file.getName();
  }
  

  @Override
  public String getKey() {
    return pkg + file.getName();
  }

  @Override
  public String getPath() {
    return file.getPath();
  }
}
