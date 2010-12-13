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
///**
// *
// */
//package edu.uci.ics.sourcerer.clusterer.dir.old;
//
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.NoSuchElementException;
//import java.util.Set;
//import java.util.logging.Level;
//
//import edu.uci.ics.sourcerer.util.Helper;
//import edu.uci.ics.sourcerer.util.io.FileUtils;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class Directory {
//  private String project;
//  private String path;
//  private DirectoryMatchedFile[] files;
//  
//  private int matched30 = 0;
//  private int matched50 = 0;
//  private int matched80 = 0;
//  
//  protected Directory(String project, String path, DirectoryMatchedFile[] files) {
//    this.project = project;
//    this.path = path;
//    this.files = files;
//  }
//  
//  private Directory(String project, String path, int matches30, int matches50, int matches80) {
//    this.project = project;
//    this.path = path;
//    this.matched30 = matches30;
//    this.matched50 = matches50;
//    this.matched80 = matches80;
//  }
//  
//  public String getProject() {
//    return project;
//  }
//  
//  public String getPath() {
//    return path;
//  }
//  
//  public DirectoryMatchedFile[] getFiles() {
//    return files;
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
//  public String toMatchedDirLine() {
//    return project + " " + path + " " + matched30 + " " + matched50 + " " + matched80;
//  }
//  
//  public static Iterable<Directory> loadMatchedDirectories(final File file) {
//    return new Iterable<Directory>() {
//      
//      @Override
//      public Iterator<Directory> iterator() {
//        try {
//          final BufferedReader br = new BufferedReader(new FileReader(file));
//          return new Iterator<Directory>() {
//            String nextLine = null;
//            
//            @Override
//            public void remove() {
//              throw new UnsupportedOperationException();
//            }
//            
//            @Override
//            public Directory next() {
//              if (hasNext()) {
//                String[] parts = nextLine.split(" ");
//                String oldLine = nextLine;
//                nextLine = null;
//                try {
//                  if (parts.length == 5) {
//                    return new Directory(parts[0], parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
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
//                  logger.log(Level.SEVERE,  "Error reading matched directories file: " + file.getPath(), e);
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
//          return Collections.<Directory>emptySet().iterator();
//        }
//      }
//    };
//  }
//  
//  public void compare(Directory other, Set<String> ignore) {
//    if (!project.equals(other.project)) {
//      Collection<DirectoryMatchedFile> myMatches = Helper.newLinkedList();
//      Collection<DirectoryMatchedFile> otherMatches = Helper.newLinkedList();
//      int i = 0, j = 0;
//      while (i < files.length && j < other.files.length) {
//        if (ignore.contains(files[i])) {
//          i++;
//          continue;
//        }
//        if (ignore.contains(other.files[j])) {
//          j++;
//          continue;
//        }
//        int comp = files[i].getName().compareTo(other.files[j].getName());
//        if (comp == 0) {
//          myMatches.add(files[i]);
//          otherMatches.add(other.files[j]);
//          i++;
//          j++;
//        } else if (comp < 0) {
//          i++;
//        } else {
//          j++;
//        }
//      }
//      if (myMatches.size() < DirectoryClusterer.MINIMUM_DIR_SIZE.getValue()) {
//        return;
//      }
//      
//      double percent = ((double) myMatches.size()) / ((double) Math.min(files.length, other.files.length));
//      if (percent >= .8) {
//        matched30++;
//        matched50++;
//        matched80++;
//        other.matched30++;
//        other.matched50++;
//        other.matched80++;
//        for (DirectoryMatchedFile file : myMatches) {
//          file.increment80();
//          file.increment50();
//          file.increment30();
//        }
//        for (DirectoryMatchedFile file : otherMatches) {
//          file.increment80();
//          file.increment50();
//          file.increment30();
//        }
//      } else if (percent >= .5) {
//        matched30++;
//        matched50++;
//        other.matched30++;
//        other.matched50++;
//        for (DirectoryMatchedFile file : myMatches) {
//          file.increment50();
//          file.increment30();
//        }
//        for (DirectoryMatchedFile file : otherMatches) {
//          file.increment50();
//          file.increment30();
//        }
//      } else if (percent >= .3) {
//        matched30++;
//        other.matched30++;
//        for (DirectoryMatchedFile file : myMatches) {
//          file.increment30();
//        }
//        for (DirectoryMatchedFile file : otherMatches) {
//          file.increment30();
//        }
//      }
//    }
//  }
//  
//  public Collection<String> matches(Directory other, Set<String> ignore, double threshold) {
//    if (!project.equals(other.project)) {
//      Collection<String> matching = Helper.newLinkedList();
//      int i = 0, j = 0;
//      while (i < files.length && j < other.files.length) {
//        if (ignore.contains(files[i])) {
//          i++;
//          continue;
//        }
//        if (ignore.contains(other.files[j])) {
//          j++;
//          continue;
//        }
//        int comp = files[i].getName().compareTo(other.files[j].getName());
//        if (comp == 0) {
//          matching.add(files[i].getName());
//          i++;
//          j++;
//        } else if (comp < 0) {
//          i++;
//        } else {
//          j++;
//        }
//      }
//      if (matching.size() < DirectoryClusterer.MINIMUM_DIR_SIZE.getValue()) {
//        return Collections.emptyList();
//      }
//      
//      double percent = ((double) matching.size()) / ((double) Math.min(files.length, other.files.length));
//      if (percent >= threshold) {
//        return matching;
//      } else {
//        return Collections.emptyList();
//      }
//    } else {
//      return Collections.emptyList();
//    }
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
//  @Override
//  public String toString() {
//    return path;
//  }
//}
