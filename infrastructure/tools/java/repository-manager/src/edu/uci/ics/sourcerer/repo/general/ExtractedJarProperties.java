///* 
// * Sourcerer: an infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
//
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.uci.ics.sourcerer.repo.general;
//
//import java.io.File;
//import java.util.Properties;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class ExtractedJarProperties extends AbstractBinaryProperties {
//  private static final String GROUP = "group";
//  private static final String VERSION = "version";
//  private static final String HASH = "hash";
//  
//  // Base properties
//  private String group;
//  private String version;
//  private String hash;
// 
//  private ExtractedJarProperties() {}
//  
//  public static ExtractedJarProperties loadProperties(File file) {
//    ExtractedJarProperties props = new ExtractedJarProperties();
//    props.load(file);
//    return props;
//  }
//  
//  @Override
//  public void load(File file) {
//    super.load(file);
//    
//    group = properties.getProperty(GROUP);
//    version = properties.getProperty(VERSION);
//    hash = properties.getProperty(HASH);
//  }
//
//  public static void create(File file, String name, String group, String version, String hash) {
//    Properties properties = new Properties();
//    
//    properties.setProperty(NAME, name);
//    properties.setProperty(GROUP, group);
//    properties.setProperty(VERSION, version);
//    properties.setProperty(HASH, hash);
//    
//    write(file, properties);
//  }
//  
//  public static void create(File file, String name, String hash) {
//    Properties properties = new Properties();
//    
//    properties.setProperty(NAME, name);
//    properties.setProperty(HASH, hash);
//    
//    write(file, properties);
//  }
//  
//  @Override
//  public void save(File file) {
//    set(GROUP, group);
//    set(VERSION, version);
//    set(HASH, hash);
//    
//    super.save(file);
//  }
//  
//  public void setGroup(String group) {
//    this.group = group;
//  }
//
//  public void setVersion(String version) {
//    this.version = version;
//  }
//
//  public void setHash(String hash) {
//    this.hash = hash;
//  }
//
//  public String getGroup() {
//    return group;
//  }
//
//  public String getVersion() {
//    return version;
//  }
//
//  public String getHash() {
//    return hash;
//  }
//}
