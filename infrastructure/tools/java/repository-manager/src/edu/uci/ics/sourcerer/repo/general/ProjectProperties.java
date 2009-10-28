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
package edu.uci.ics.sourcerer.repo.general;

import java.io.File;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ProjectProperties extends AbstractProperties {
  private static final String NAME = "name";
  private static final String EXTRACTED = "extracted";
  
  // Base properties
  private String name;
  
  // Extraction properties
  private boolean extracted;
  
  private ProjectProperties() {}
  
  public static ProjectProperties load(File file) {
    ProjectProperties props = new ProjectProperties();
    props.loadProperties(file);

    props.name = props.properties.getProperty(NAME);
      
    props.extracted = "true".equals(props.properties.getProperty(EXTRACTED));
    
    return props;
  }
  
  public void reportExtraction(File file) {
    extracted = true;
    
    properties.setProperty(EXTRACTED, Boolean.toString(extracted));
    
    write(file);
  }
  
  public String getName() {
    return name;
  }

  public boolean extracted() {
    return extracted;
  }
}
