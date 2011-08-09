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
public class DualFileArgument {
  private String name;
  private String description;
  private String defaultValue;
  
  private Argument<File> input;
  private Argument<File> output;
  
  public DualFileArgument(String name, String description) {
    this.name = name;
    this.description = description;
  }
  
  public DualFileArgument(String name, String defaultValue, String description) {
    this.name = name;
    this.description = description;
    this.defaultValue = defaultValue;
  }
  
  public Argument<File> asInput() {
    if (input == null) {
      input = new FileProperty(Arguments.INPUT);
    }
    return input;
  }
  
  public Argument<File> asOutput() {
    if (output == null) {
      output = new FileProperty(Arguments.OUTPUT);
    }
    return output;
  }
  
  private class FileProperty extends Argument<File> {
    private Argument<File> base;
    
    protected FileProperty(Argument<File> base) {
      super(DualFileArgument.this.name, null, DualFileArgument.this.description);
      this.base = base;
    }

    @Override
    public String getType() {
      return base.getName() + " relative path";
    }

    @Override
    public String getDefaultString() {
      return base.getName() + "/" + DualFileArgument.this.defaultValue;
    }
    
    @Override
    public boolean hasDefaultValue() {
      return DualFileArgument.this.defaultValue != null;
    }
    
    @Override
    public File getDefaultValue() {
      if (DualFileArgument.this.defaultValue == null) {
        return null;
      } else {
        return new File(base.getValue(), DualFileArgument.this.defaultValue);
      }
    }
    
    @Override
    protected File parseString(String value) {
      File file = new File(value);
      if (file.isAbsolute()) {
        return file;
      } else {
        return new File(base.getValue(), value);
      }
    }
  }
  
  @Override
  public String toString() {
    return name;
  }
}
