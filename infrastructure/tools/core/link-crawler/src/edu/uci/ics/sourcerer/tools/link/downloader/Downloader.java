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
package edu.uci.ics.sourcerer.tools.link.downloader;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.util.logging.Level;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Downloader {
  public enum Type {
    SVN;
  }
  
  public static boolean download(Type type, String url, File file) {
    if (type == Type.SVN) {
//      return Subversion.download(url, file);
      return Subversion.checkout(url, file);
    } else {
      logger.log(Level.SEVERE, "Unable to identify form of url: " + url);
      return false;
    }
  }
}
