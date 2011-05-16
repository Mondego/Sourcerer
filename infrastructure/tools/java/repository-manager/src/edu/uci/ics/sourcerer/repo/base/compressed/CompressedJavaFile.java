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
package edu.uci.ics.sourcerer.repo.base.compressed;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.base.AbstractJavaFile;
import edu.uci.ics.sourcerer.repo.base.compressed.CompressedFileSet.CompressedRepoFile;
import edu.uci.ics.sourcerer.repo.core.RepoFile;
import edu.uci.ics.sourcerer.util.io.LWField;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CompressedJavaFile extends AbstractJavaFile {
  @LWField
  private String pkg;
  @LWField
  private CompressedRepoFile file;
  
  protected CompressedJavaFile() {}
  
  protected CompressedJavaFile(CompressedRepoFile file, InputStream is) {
    this.file = file;
    
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
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
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to extract package for file!", e);
    }
  }
  
  @Override
  public String getPackage() {
    return pkg;
  }
  
  @Override
  public RepoFile getFile() {
    return file;
  }
  
  @Override
  public String toString() {
    return file.toString();
  }
}
