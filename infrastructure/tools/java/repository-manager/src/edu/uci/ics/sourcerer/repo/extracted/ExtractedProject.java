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

import edu.uci.ics.sourcerer.repo.core.RepoFile;
import edu.uci.ics.sourcerer.repo.general.AbstractExtractedProperties;
import edu.uci.ics.sourcerer.repo.general.ExtractedProjectProperties;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedProject extends Extracted {
  public static final Property<String> JAR_FILE_FILE = new StringProperty("jar-file-file", "jars.txt", "Filename for the associated jars.");
  
  private ExtractedProjectProperties properties;
   
  public ExtractedProject(RepoFile content) {
    super(content);
    properties = ExtractedProjectProperties.loadProperties(getPropertiesFile());
  }
  
  public ExtractedProject(RepoFile content, File propFile) {
    super(content);
    properties = ExtractedProjectProperties.loadProperties(propFile);
    File exPropFile = getPropertiesFile();
    if (exPropFile.exists()) {
      properties.load(exPropFile);
      properties.save(exPropFile);
    }
  }
  
  public void reportSuccessfulExtraction(int fromSource, int sourceExceptions, int jars) {
    properties.setExtracted(true);
    properties.setMissingTypes(false);
    properties.setFromSource(fromSource);
    properties.setSourceExceptions(sourceExceptions);
    properties.setJars(jars);
    
    properties.save(getPropertiesFile());
  }
  
  public void reportForcedExtraction(int fromSource, int sourceExceptions, int jars) {
    properties.setExtracted(true);
    properties.setMissingTypes(true);
    properties.setFromSource(fromSource);
    properties.setSourceExceptions(sourceExceptions);
    properties.setJars(jars);
    
    properties.save(getPropertiesFile());
  }
  
  public void reportMissingTypeExtraction() {
    properties.setExtracted(false);
    properties.setMissingTypes(true);
    properties.setFromSource(0);
    properties.setSourceExceptions(0);
    properties.setJars(0);
    
    properties.save(getPropertiesFile());
  }
  
  protected AbstractExtractedProperties getProperties() {
    return properties;
  }
  
  public boolean shouldVerify() {
    return properties.shouldVerify();
  }
  
  public String getDescription() {
    return properties.getDescription();
  }
  
  public String toString() {
    return getName() + " (" + content.getRelativePath() + ")";
  }
}