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
package edu.uci.ics.sourcerer.util.io.arguments;

import java.io.File;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RelativeFileArgument extends FileArgument {
  private final String defaultRelativePath;
  private final Argument<File> parent;
  
  public RelativeFileArgument(String name, Argument<File> parent, String description) {
    super(name, description);
    this.parent = parent;
    defaultRelativePath = null;
  }
  
  public RelativeFileArgument(String name, String defaultRelativePath, Argument<File> parent, String description) {
    super(name, null, description);
    this.parent = parent;
    this.defaultRelativePath = defaultRelativePath;
  }
  
  public synchronized void setValue(String value) {
    if (value == null) {
      super.setValue(null);
    } else {
      super.setValue(parseString(value));
    }
  }
  
  @Override
  public String getType() {
    return "relative path";
  }
  
  @Override
  public boolean hasDefaultValue() {
    return defaultRelativePath != null;
  }
  
  @Override
  public File getDefaultValue() {
    if (defaultRelativePath == null) {
      return null;
    } else {
      return new File(parent.getValue(), defaultRelativePath);
    }
  }
  
  @Override
  public String getDefaultString() {
    if (defaultRelativePath == null) {
      return null;
    } else {
      return getDefaultValue().getPath();
    }
  }
  
  @Override
  protected File parseString(String value) {
    return new File(parent.getValue(), value);
  }
}
