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
//package edu.uci.ics.sourcerer.clusterer.dir.old;
//
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.NoSuchElementException;
//import java.util.logging.Level;
//
//import edu.uci.ics.sourcerer.clusterer.stats.MatchedFile;
//import edu.uci.ics.sourcerer.util.io.FileUtils;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class DirectoryMatchedFile extends MatchedFile {
//  private String name;
//  private int matched30 = 0;
//  private int matched50 = 0;
//  private int matched80 = 0;
//  
//  public DirectoryMatchedFile(String name) {
//    this.name = name;
//  }
//  
//  private DirectoryMatchedFile(String name, int matched30, int matched50, int matched80) {
//    this.name = name;
//    this.matched30 = matched30;
//    this.matched50 = matched50;
//    this.matched80 = matched80;
//  }
//  
//  public String getName() {
//    return name;
//  }
//  
//  protected void increment30() {
//    matched30++;
//  }
//  
//  protected void increment50() {
//    matched30++;
//    matched50++;
//  }
//  
//  protected void increment80() {
//    matched30++;
//    matched50++;
//    matched80++;
//  }
//  
//  public boolean matched30() {
//    return matched30 > 0;
//  }
//  
//  public boolean matched50() {
//    return matched50 > 0;
//  }
//  
//  public boolean matched80() {
//    return matched80 > 0;
//  }
//  
//  public int get30() {
//    return matched30;
//  }
//  
//  public int get50() {
//    return matched50;
//  }
//  
//  public int get80() {
//    return matched80;
//  }
//  
//  public String toCopiedFileLine() {
//    return name + " " + matched30 + " " + matched50 + " " + matched80;
//  }
//  
//  public static Iterable<DirectoryMatchedFile> loadMatchedFiles(final File file) {
//    return new Iterable<DirectoryMatchedFile>() {
//      @Override
//      public Iterator<DirectoryMatchedFile> iterator() {
//        try {
//          final BufferedReader br = new BufferedReader(new FileReader(file));
//          return new Iterator<DirectoryMatchedFile>() {
//            String nextLine = null;
//            
//            @Override
//            public void remove() {
//              throw new UnsupportedOperationException();
//            }
//            
//            @Override
//            public DirectoryMatchedFile next() {
//              if (hasNext()) {
//                String[] parts = nextLine.split(" ");
//                String oldLine = nextLine;
//                nextLine = null;
//                try {
//                  if (parts.length == 6) {
//                    return new DirectoryMatchedFile(parts[2], Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
//                  } else {
//                    logger.log(Level.SEVERE, "Unable to parse line: " + oldLine);
//                    return next();
//                  }
//                } catch (NumberFormatException e) {
//                  logger.log(Level.SEVERE, "Unable to parse line: " + oldLine, e);
//                  return next();
//                }
//              } else {
//                throw new NoSuchElementException();
//              }
//            }
//            
//            @Override
//            public boolean hasNext() {
//              if (nextLine == null) {
//                try {
//                  nextLine = br.readLine();
//                } catch (IOException e) {
//                  logger.log(Level.SEVERE,  "Error reading copied files file: " + file.getPath(), e);
//                  FileUtils.close(br);
//                  return false;
//                }
//                if (nextLine == null) {
//                  FileUtils.close(br);
//                  return false;
//                } else {
//                  return true;
//                }
//              } else {
//                return true;
//              }
//            }
//          };
//        } catch (IOException e) {
//          logger.log(Level.SEVERE, "Error reading matched directories file: " + file.getPath(), e);
//          return Collections.<DirectoryMatchedFile>emptySet().iterator();
//        }
//      }
//    };
//  }
//}
