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
package edu.uci.ics.sourcerer.clusterer.cloning.method.hash;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class HashingMatcher {
  private String md5;
  private String sha;
  private long length;

  protected HashingMatcher() {}
  
  protected void setValues(String md5, String sha, long length) {
    this.md5 = md5;
    this.sha = sha;
    this.length = length;
  }
  
  public HashingMatcher copy() {
    HashingMatcher retval = new HashingMatcher();
    retval.setValues(md5, sha, length);
    return retval;
  }
  
  public String getMD5() {
    return md5;
  }
  
  public long getLength() {
    return length;
  }
  
  public int hashCode() {
    return md5.hashCode();
  }
  
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof HashingMatcher) {
      HashingMatcher other = (HashingMatcher) o;
      return md5.equals(other.md5) && sha.equals(other.sha) && length == other.length;
    } else {
      return false;
    }
  }
}
