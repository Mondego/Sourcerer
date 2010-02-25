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

import edu.uci.ics.sourcerer.repo.general.AbstractProperties;
import edu.uci.ics.sourcerer.repo.general.ProjectProperties;
import edu.uci.ics.sourcerer.repo.general.RepoPath;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedProject extends Extracted {
  public static final Property<String> JAR_FILE_FILE = new StringProperty("jar-file-file", "jars.txt", "Repository Manager", "Filename for the associated jars.");
  
  private ProjectProperties properties;
   
  public ExtractedProject(RepoPath content) {
    super(content);
    properties = ProjectProperties.load(getPropertiesFile());
  }
  
  public ExtractedProject(RepoPath content, File propFile) {
    super(content);
    File exPropFile = getPropertiesFile();
    if (exPropFile.exists()) {
      properties = ProjectProperties.load(exPropFile);
    } else {
      properties = ProjectProperties.load(propFile);
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
  
  protected AbstractProperties getProperties() {
    return properties;
  }
  
  public String getDescription() {
    return properties.getDescription();
  }
  
  public String getProjectPath() {
    return content.getRelativePath();
  }
  
  public String toString() {
    return getName() + " (" + content.getRelativePath() + ")";
  }
}