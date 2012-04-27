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
package edu.uci.ics.sourcerer.tools.java.cloning.pairwise;

import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.cloning.method.Confidence;
import edu.uci.ics.sourcerer.tools.java.cloning.method.DetectionMethod;
import edu.uci.ics.sourcerer.tools.java.cloning.method.File;
import edu.uci.ics.sourcerer.util.Helper;

public class MatchStatus {
  private File file;
  private Map<DetectionMethod, Confidence> map;
  
  public MatchStatus(File file) {
    this.file = file;
    this.map = Helper.newEnumMap(DetectionMethod.class);
  }

  public Confidence get(DetectionMethod method) {
    return map.get(method);
  }
  
  public void set(DetectionMethod method, Confidence confidence) {
    map.put(method, confidence);
  }
  
  public Confidence getHash() {
    return map.get(DetectionMethod.HASH); 
  }

  public void setHash(Confidence hash) {
    map.put(DetectionMethod.HASH, hash);
  }

  public Confidence getFqn() {
    return map.get(DetectionMethod.FQN);
  }

  public void setFqn(Confidence fqn) {
    map.put(DetectionMethod.FQN, fqn);
  }

  public Confidence getFingerprint() {
    return map.get(DetectionMethod.FINGERPRINT);
  }

  public void setFingerprint(Confidence fingerprint) {
    map.put(DetectionMethod.FINGERPRINT, fingerprint);
  }

  public Confidence getCombined() {
    return map.get(DetectionMethod.COMBINED);
  }

  public void setCombined(Confidence combined) {
    map.put(DetectionMethod.COMBINED, combined);
  }

  public File getFile() {
    return file;
  }
}