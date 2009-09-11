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
package edu.uci.ics.sourcerer.repo;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.logging.Level;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepoJar {
  private long length;
  private String md5;
  
  public RepoJar(File path) {
    length = path.length();
    md5 = getHash(path);
  }
  
  public RepoJar(long length, String hash) {
    this.length = length;
    this.md5 = hash;
  }
  
  public static String getHash(File path) {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      
      byte[] buff = new byte[1024];
      InputStream is = new FileInputStream(path);
      int size;
      while ((size = is.read(buff)) != -1) {
        digest.update(buff, 0, size);
      }
      return new BigInteger(1, digest.digest()).toString(16);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error getting md5 for " + path.getPath(), e);
      return null;
    }
  }
  
  public String getHash() {
    return md5;
  }
  
  public long getLength() {
    return length;
  }
  
  public boolean equals(Object o) {
    if (o instanceof RepoJar) {
      RepoJar other = (RepoJar) o;
      return length == other.length && md5.equals(other.md5);
    } else {
      return false;
    }
  }
  
  public int hashCode() {
    return md5.hashCode();
  }
}
