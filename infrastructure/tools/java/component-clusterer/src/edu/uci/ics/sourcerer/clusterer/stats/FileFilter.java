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
package edu.uci.ics.sourcerer.clusterer.stats;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileFilter implements Filter {
  private Set<String> files;
  
  public FileFilter() {
    files = Helper.newHashSet();
  }
  
  public void addFile(String project, String path) {
    if (path.startsWith("/")) {
      files.add(project + ":" + path);
    } else {
      files.add(project + ":/" + path);
    }
  }
  
  @Override
  public boolean pass(String project, String path) {
    if (path.startsWith("/")) {
      return files.contains(project + ":" + path);
    } else {
      return files.contains(project + ":/" + path);
    }
  }
  
  public static FileFilter loadFilter() {
    BufferedReader br = null;
    try {
      br = FileUtils.getBufferedReader(AbstractRepository.FILTERED_FILES_FILE);
      
      FileFilter filter = new FileFilter();
      
      for(String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        if (parts.length == 2) {
          filter.addFile(parts[0], parts[1]);
        } else {
          logger.log(Level.SEVERE, "Invalid line: " + line);
        }
      }
      return filter;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to read file listing.", e);
      return null;
    } finally {
      FileUtils.close(br);
    }
  }
}
