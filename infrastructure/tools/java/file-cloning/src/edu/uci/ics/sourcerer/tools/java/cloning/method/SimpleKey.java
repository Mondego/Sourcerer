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

import java.util.ArrayList;
import java.util.Collection;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class SimpleKey implements Key {
  private String key;
  private ArrayList<File> files;
  private Confidence confidence;
  
  private Collection<KeyMatch> matches;
  
  protected SimpleKey(String key) {
    this(key, Confidence.HIGH);
  }
  
  protected SimpleKey(String key, Confidence confidence) {
    this.key = key;
    this.confidence = confidence;
    files = Helper.newArrayList();
  }
  
  public String getKey() {
    return key;
  }
  
  @Override
  public void addFile(File file) {
    files.add(file);
  }
    
  public ArrayList<File> getFiles() {
    return files;
  }
  
  @Override
  public Collection<KeyMatch> getMatches() {
    if (matches == null) {
      matches = Helper.newArrayList(files.size());
      for (File file : files) {
        matches.add(new KeyMatch(file, confidence));
      }
    }
    return matches;
  }
  
  @Override
  public boolean isUnique(Confidence confidence) {
    if (files.size() == 1) {
      return true;
    } else {
      return confidence.compareTo(this.confidence) > 0;
    }
  }
  
  public void setConfidence(Confidence confidence) {
    this.confidence = confidence;
  }
  
  public Confidence getConfidence() {
    return confidence;
  }
}
