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
package edu.uci.ics.sourcerer.repo.extracted;

import java.io.File;

import edu.uci.ics.sourcerer.repo.general.AbstractBinaryProperties;
import edu.uci.ics.sourcerer.repo.general.ExtractedJarProperties;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedJar extends ExtractedBinary {
  private ExtractedJarProperties properties;
  
  public ExtractedJar(RepoFileImpl content) {
    super(content);
    properties = ExtractedJarProperties.loadProperties(getPropertiesFile());
  }
  
  public ExtractedJar(RepoFileImpl content, File propFile) {
    super(content);
    properties = ExtractedJarProperties.loadProperties(propFile);
    File exPropFile = getPropertiesFile();
    if (exPropFile.exists()) {
      properties.load(exPropFile);
      properties.save(exPropFile);
    }
  }

  public void reportSuccessfulExtraction(int fromBinary, int binaryExceptions, int fromSource, int sourceExceptions, int firstOrderJars, int jars) {
    properties.setExtracted(true);
    properties.setSourceSkipped(false);
    properties.setMissingTypes(false);
    properties.setFromBinary(fromBinary);
    properties.setBinaryExceptions(binaryExceptions);
    properties.setFromSource(fromSource);
    properties.setSourceExceptions(sourceExceptions);
    properties.setFirstOrderJars(firstOrderJars);
    properties.setJars(jars);
    
    properties.save(getPropertiesFile());
  }
  
  public void reportBinaryExtraction(int fromBinary, int binaryExceptions, boolean sourceSkipped) {
    properties.setExtracted(true);
    properties.setSourceSkipped(sourceSkipped);
    properties.setMissingTypes(false);
    properties.setFromBinary(fromBinary);
    properties.setBinaryExceptions(binaryExceptions);
    properties.setFromSource(0);
    properties.setSourceExceptions(0);
    properties.setFirstOrderJars(0);
    properties.setJars(0);
    
    properties.save(getPropertiesFile());
  }
  
  public void reportForcedExtraction(int fromBinary, int binaryExceptions, int fromSource, int sourceExceptions, int firstOrderJars, int jars) {
    properties.setExtracted(true);
    properties.setSourceSkipped(false);
    properties.setMissingTypes(true);
    properties.setFromBinary(fromBinary);
    properties.setBinaryExceptions(binaryExceptions);
    properties.setFromSource(fromSource);
    properties.setSourceExceptions(sourceExceptions);
    properties.setFirstOrderJars(firstOrderJars);
    properties.setJars(jars);
    
    properties.save(getPropertiesFile());
  }

  public void reportMissingTypeExtraction() {
    properties.setExtracted(false);
    properties.setSourceSkipped(false);
    properties.setMissingTypes(true);
    properties.setFromBinary(0);
    properties.setBinaryExceptions(0);
    properties.setFromSource(0);
    properties.setSourceExceptions(0);
    properties.setFirstOrderJars(0);
    properties.setJars(0);
    
    properties.save(getPropertiesFile());
  }
  
  protected AbstractBinaryProperties getBinaryProperties() {
    return properties;
  }
  
  public String getGroup() {
    return properties.getGroup();
  }
  
  public String getVersion() {
    return properties.getVersion();
  }
  
  @Override
  public String getHash() {
    return properties.getHash();
  }
}
