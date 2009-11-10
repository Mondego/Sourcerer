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
public class JarProperties extends AbstractProperties {
  private static final String NAME = "name";
  private static final String GROUP = "group";
  private static final String VERSION = "version";
  private static final String HASH = "hash";
  private static final String EXTRACTED = "extracted";
  private static final String FROM_BINARY = "fromBinary";
  private static final String FROM_SOURCE = "fromSource";
  private static final String SOURCE_ERROR = "sourceError";
  
  // Base properties
  private String name;
  private String group;
  private String version;
  private String hash;
  
  // Extraction properties
  private boolean extracted;
  private int fromBinary;
  private int fromSource;
  private int sourceError;
  
  private JarProperties() {}
  
  public static JarProperties load(File file) {
    JarProperties props = new JarProperties();
    props.loadProperties(file);

    props.name = props.properties.getProperty(NAME);
    props.group = props.properties.getProperty(GROUP);
    props.version = props.properties.getProperty(VERSION);
    props.hash = props.properties.getProperty(HASH);
    
    props.extracted = "true".equals(props.properties.getProperty(EXTRACTED));
    props.fromBinary = props.readIntProperty(FROM_BINARY);
    props.fromSource = props.readIntProperty(FROM_SOURCE);
    props.sourceError = props.readIntProperty(SOURCE_ERROR);
      
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
  
  public void reportExtraction(File file, int fromBinary, int fromSource, int sourceError) {
    this.extracted = true;
    this.fromBinary = fromBinary;
    this.fromSource = fromSource;
    this.sourceError = sourceError;
    
    properties.setProperty(EXTRACTED, Boolean.toString(extracted));
    properties.setProperty(FROM_BINARY, Integer.toString(fromBinary));
    properties.setProperty(FROM_SOURCE, Integer.toString(fromSource));
    properties. setProperty(SOURCE_ERROR, Integer.toString(sourceError));
    
    write(file);
  }
  
  public String getName() {
    return name;
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

  public boolean extracted() {
    return extracted;
  }

  public int getExtractedFromBinary() {
    if (extracted) {
      return fromBinary;
    } else {
      throw new IllegalStateException("This library has not been extracted yet.");
    }
  }

  public int getExtractedFromSource() {
    if (extracted) {
      return fromSource;
    } else {
      throw new IllegalStateException("This library has not been extracted yet.");
    }
  }
  
  public int getSourceFilesWithErrors() {
    if (extracted) {
      return sourceError;
    } else {
      throw new IllegalStateException("This library has not been extracted yet.");
    }
  }
}
