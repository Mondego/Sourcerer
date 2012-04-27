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

import edu.uci.ics.sourcerer.util.io.SimpleSerializable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FingerprintFile implements SimpleSerializable {
  private String project;
  private String path;
  private String name;
  private String[] fields;
  private String[] constructors;
  private String[] methods; 
  
  protected FingerprintFile() {}
  
  protected void set(String project, String path, String name, String[] fields, String[] constructors, String[] methods) {
    this.project = project;
    this.path = path;
    this.name = name;
    this.fields = fields;
    this.constructors = constructors;
    this.methods = methods;
  }

  public String getProject() {
    return project;
  }

  public String getPath() {
    return path;
  }

  public String getName() {
    return name;
  }

  public String[] getFields() {
    return fields;
  }

  public String[] getConstructors() {
    return constructors;
  }
  
  public String[] getMethods() {
    return methods;
  }
}
