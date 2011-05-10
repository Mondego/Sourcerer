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
package edu.uci.ics.sourcerer.util.io.properties;

import java.io.File;

import edu.uci.ics.sourcerer.util.io.Properties;
import edu.uci.ics.sourcerer.util.io.Property;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class IOFilePropertyFactory {
  private String name;
  private String description;
  private String defaultValue;
  
  private Property<File> input;
  private Property<File> output;
  
  public IOFilePropertyFactory(String name, String description) {
    this.name = name;
    this.description = description;
  }
  
  public IOFilePropertyFactory(String name, String defaultValue, String description) {
    this.name = name;
    this.description = description;
    this.defaultValue = defaultValue;
  }
  
  public Property<File> asInput() {
    if (input == null) {
      input = new FileProperty(Properties.INPUT);
    }
    return input;
  }
  
  public Property<File> asOutput() {
    if (output == null) {
      output = new FileProperty(Properties.OUTPUT);
    }
    return output;
  }
  
  private class FileProperty extends Property<File> {
    private Property<File> base;
    
    protected FileProperty(Property<File> base) {
      super(IOFilePropertyFactory.this.name, null, IOFilePropertyFactory.this.description);
      this.base = base;
    }

    @Override
    public String getType() {
      return base.getName() + " relative path";
    }

    @Override
    public String getDefaultString() {
      return base.getName() + "/" + IOFilePropertyFactory.this.defaultValue;
    }
    
    @Override
    public boolean hasDefaultValue() {
      return IOFilePropertyFactory.this.defaultValue != null;
    }
    
    @Override
    public File getDefaultValue() {
      if (IOFilePropertyFactory.this.defaultValue == null) {
        return null;
      } else {
        return new File(base.getValue(), IOFilePropertyFactory.this.defaultValue);
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
}
