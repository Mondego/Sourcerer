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
package edu.uci.ics.sourcerer.tools.java.repo.model;

import edu.uci.ics.sourcerer.tools.core.repo.model.RepoFile;
import edu.uci.ics.sourcerer.util.io.properties.AbstractProperties;
import edu.uci.ics.sourcerer.util.io.properties.EnumProperty;
import edu.uci.ics.sourcerer.util.io.properties.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarProperties extends AbstractProperties {
  public Property<String> HASH = new StringProperty("hash", this);
  public Property<String> NAME = new StringProperty("name", this);
  public Property<String> GROUP = new StringProperty("group", this);
  public Property<String> VERSION = new StringProperty("version", this);
  public Property<JarSource> SOURCE = new EnumProperty<JarSource>("source", JarSource.class, this); 
  
  public JarProperties(RepoFile file) {
    super(file.toFile());
  }
}
