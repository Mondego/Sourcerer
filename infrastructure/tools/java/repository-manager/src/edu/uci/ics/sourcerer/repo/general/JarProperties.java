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
public class JarProperties extends AbstractBinaryProperties {
  private static final String GROUP = "group";
  private static final String VERSION = "version";
  private static final String HASH = "hash";
  
  // Base properties
  private String group;
  private String version;
  private String hash;
 
  private JarProperties() {}
  
  public static JarProperties load(File file) {
    JarProperties props = new JarProperties();
    props.loadProperties(file);

    props.group = props.properties.getProperty(GROUP);
    props.version = props.properties.getProperty(VERSION);
    props.hash = props.properties.getProperty(HASH);
    
    return props;
  }

  public static void create(File file, String name, String group, String version, String hash) {
    Properties properties = new Properties();
    
    properties.setProperty(NAME, name);
    properties.setProperty(GROUP, group);
    properties.setProperty(VERSION, version);
    properties.setProperty(HASH, hash);
    
    write(file, properties);
  }
  
  public static void create(File file, String name, String hash) {
    Properties properties = new Properties();
    
    properties.setProperty(NAME, name);
    properties.setProperty(HASH, hash);
    
    write(file, properties);
  }
  
  public void reportSuccessfulExtraction(File file, int fromBinary, int binaryExceptions, int fromSource, int sourceExceptions) {
    this.extracted = true;
    this.fromBinary = fromBinary;
    this.binaryExceptions = binaryExceptions;
    this.fromSource = fromSource;
    this.sourceExceptions = sourceExceptions;
    
    
    properties.setProperty(EXTRACTED, Boolean.toString(extracted));
    properties.setProperty(FROM_BINARY, Integer.toString(fromBinary));
    properties.setProperty(BINARY_EXCEPTIONS, Integer.toString(binaryExceptions));
    properties.setProperty(FROM_SOURCE, Integer.toString(fromSource));
    properties.setProperty(SOURCE_EXCEPTIONS, Integer.toString(sourceExceptions));
    
    write(file);
  }
  
  public void reportMissingTypeExtraction(File file) {
    this.missingTypes = true;
    
    properties.setProperty(MISSING_TYPES, Boolean.toString(missingTypes));
    
    write(file);
  }
  
  public String getGroup() {
    return group;
  }

  public String getVersion() {
    return version;
  }

  public String getHash() {
    return hash;
  }
}
