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

import java.util.Collection;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.cloning.method.fqn.FqnClusterer;
import edu.uci.ics.sourcerer.tools.java.cloning.method.fqn.FqnFile;
import edu.uci.ics.sourcerer.tools.java.cloning.method.hash.HashedFile;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class KeyFactory {
  private Map<String, SimpleKey> hashKeys = Helper.newHashMap();
  private Map<String, SimpleKey> fqnKeys = Helper.newHashMap();
  
  public KeyFactory() {}
    
  public SimpleKey getHashKey(HashedFile file) {
    String hash = file.getMd5();
    SimpleKey key = hashKeys.get(hash);
    if (key == null) {
      key = new SimpleKey(hash);
      hashKeys.put(hash, key);
    }
    return key;
  }
  
  public SimpleKey getFqnKey(FqnFile file) {
    String fqn = file.getFqn();
    SimpleKey key = fqnKeys.get(fqn);
    if (key == null) {
      Confidence confidence = Confidence.HIGH;
      if (fqn.startsWith("default.")) {
        confidence = Confidence.LOW;
      } else {
        int dotCount = 0;
        for (int index = fqn.indexOf('.'); index != -1; index = fqn.indexOf('.', index + 1)) {
          dotCount++;
        }
        if (dotCount < FqnClusterer.MINIMUM_FQN_DOTS.getValue()) {
          confidence = Confidence.MEDIUM;
        }
      }
      key = new SimpleKey(fqn, confidence);
      fqnKeys.put(fqn, key);
    }
    return key;
  }
  
  public Collection<SimpleKey> getHashKeys() {
    return hashKeys.values();
  }
  
  public Collection<SimpleKey> getFqnKeys() {
    return fqnKeys.values();
  }
}
