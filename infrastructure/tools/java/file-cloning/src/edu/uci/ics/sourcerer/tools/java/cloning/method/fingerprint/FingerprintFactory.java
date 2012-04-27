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
package edu.uci.ics.sourcerer.tools.java.cloning.method.fingerprint;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FingerprintFactory {
  private NameFingerprintIndex nameIndex;
  
  public FingerprintFactory() {
    nameIndex = new NameFingerprintIndex();
  }
  
  public NameFingerprintKey getNameFingerprintKey(FingerprintFile file) {
    String[] fullFields = file.getFields();
    String[] fields = new String[fullFields.length];
    for (int i = 0; i < fields.length; i++) {
      // Reduce to the local name
      int index = fullFields[i].lastIndexOf('.');
      fields[i] = fullFields[i].substring(index + 1);
    }
    
    String[] fullMethods = file.getMethods();
    String[] methods = new String[fullMethods.length];
    for (int i = 0; i < methods.length; i++) {
      // Snip off the parameters
      int index = fullMethods[i].indexOf('(');
      methods[i] = fullMethods[i].substring(0, index);
      // Reduce to the local name
      index = methods[i].lastIndexOf('.');
      methods[i] = methods[i].substring(index + 1);
    }
    
    return new NameFingerprintKey(file.getName(), fields, methods, nameIndex);
  }
  
  public void clearPopularNames() {
    nameIndex.clearPopularNames();
  }
  
  public FingerprintKey getFullFingerprint(FingerprintFile file) {
    return null;
  }
}
