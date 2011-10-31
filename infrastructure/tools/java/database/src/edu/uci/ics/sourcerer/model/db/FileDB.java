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
//package edu.uci.ics.sourcerer.model.db;
//
//import edu.uci.ics.sourcerer.model.File;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class FileDB {
//  private Integer fileID;
//  private File type;
//  private String name;
//  private String path;
//  private String hash;
//  private Integer projectID;
//  
//  public FileDB(Integer fileID, File type, String name, String path, String hash, Integer projectID) {
//    this.fileID = fileID;
//    this.type = type;
//    this.name = name;
//    this.path = path;
//    this.hash = hash;
//    this.projectID = projectID;
//  }
//
//  public Integer getFileID() {
//    return fileID;
//  }
//
//  public File getType() {
//    return type;
//  }
//
//  public String getName() {
//    return name;
//  }
//
//  public String getPath() {
//    return path;
//  }
//
//  public String getHash() {
//    return hash;
//  }
//
//  public Integer getProjectID() {
//    return projectID;
//  }
//  
//  public String toString() {
//    return "file " + name + " (" + fileID + ")";
//  }
//}
