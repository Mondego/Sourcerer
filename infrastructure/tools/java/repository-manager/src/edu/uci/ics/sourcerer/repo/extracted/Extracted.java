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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class Extracted {
  protected File content;

  public Extracted(File content) {
    this.content = content;
  }

  protected InputStream getInputStream(Property property) throws IOException {
    PropertyManager properties = PropertyManager.getProperties();
    File file = new File(content, properties.getValue(property));
    return new FileInputStream(file);
  }

  public InputStream getEntityInputStream() throws IOException {
    return getInputStream(Property.ENTITY_FILE); 
  }
  
  public InputStream getRelationInputStream() throws IOException {
    return getInputStream(Property.RELATION_FILE);
  }
  
  public InputStream getLocalVariableInputStream() throws IOException {
    return getInputStream(Property.LOCAL_VARIABLE_FILE);
  }
}