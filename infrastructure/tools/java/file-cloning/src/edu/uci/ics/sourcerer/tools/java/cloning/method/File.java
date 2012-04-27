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
package edu.uci.ics.sourcerer.tools.java.cloning.method;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class File {
  private Project project;
  private String path;
  
  private Key hashKey;
  private Key fqnKey;
  private Key fingerprintKey;
  private Key combinedKey;
  private Key dirKey;
  
  protected File(Project project, String path) {
    this.project = project;
    this.path = path;
  }
  
  public Project getProject() {
    return project;
  }
  
  public String getPath() {
    return path;
  }
  
  public void setHashKey(Key hashKey) {
    if (this.hashKey != null) {
      throw new IllegalStateException("Hash key may not be changed.");
    }
    this.hashKey = hashKey;
    hashKey.addFile(this);
  }
  
  public Key getHashKey() {
    return hashKey;
  }
  
  public boolean hasHashKey() {
    return hashKey != null;
  }
  
  public void setFqnKey(Key fqnKey) {
    if (this.fqnKey != null) {
      throw new IllegalStateException("Fqn key may not be changed.");
    }
    this.fqnKey = fqnKey;
    fqnKey.addFile(this);
  }
  
  public Key getFqnKey() {
    return fqnKey;
  }
  
  public boolean hasFqnKey() {
    return fqnKey != null;
  }
  
  public void setFingerprintKey(Key fingerprintKey) {
    if (this.fingerprintKey != null) {
      throw new IllegalStateException("Fingerprint may not be changed.");
    }
    this.fingerprintKey = fingerprintKey;
    fingerprintKey.addFile(this);
  }
  
  public Key getFingerprintKey() {
    return fingerprintKey;
  }
  
  public boolean hasFingerprintKey() {
    return fingerprintKey != null;
  }
  
  public void setCombinedKey(Key combinedKey) {
    if (this.combinedKey != null) {
      throw new IllegalStateException("Combined key may not be changed.");
    }
    this.combinedKey = combinedKey;
    combinedKey.addFile(this);
  }
  
  public Key getCombinedKey() {
    return combinedKey;
  }
  
  public boolean hasCombinedKey() {
    return combinedKey != null;
  }
  
  public void setDirKey(Key dirKey) {
    if (this.dirKey != null) {
      throw new IllegalStateException("Dir key may not be changed.");
    }
    this.dirKey = dirKey;
    dirKey.addFile(this);
  }
  
  public Key getDirKey() {
    return dirKey;
  }
  
  public boolean hasDirKey() {
    return dirKey != null;
  }

  public boolean hasAllKeys() {
    return hashKey != null && fqnKey != null && fingerprintKey != null;
  }
  
  public String toString() {
    return project + ":" + path;
  }
}
