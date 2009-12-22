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
import java.util.Properties;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LibraryProperties extends AbstractBinaryProperties {
  private LibraryProperties() {}
  
  public static LibraryProperties load(File file) {
    LibraryProperties props = new LibraryProperties();
    props.loadProperties(file);
    return props;
  }
  
  public static void create(File file, String name, int fromBinary, int binaryExceptions, int fromSource, int sourceExceptions) {
    Properties properties = new Properties();
    
    properties.setProperty(NAME, name);
    properties.setProperty(EXTRACTED, "true");
    properties.setProperty(FROM_BINARY, Integer.toString(fromBinary));
    properties.setProperty(BINARY_EXCEPTIONS, Integer.toString(binaryExceptions));
    properties.setProperty(FROM_SOURCE, Integer.toString(fromSource));
    properties.setProperty(SOURCE_EXCEPTIONS, Integer.toString(sourceExceptions));
    
    write(file, properties);
  }
  
  public void save(File file) {
    super.save(file);
  }
}
