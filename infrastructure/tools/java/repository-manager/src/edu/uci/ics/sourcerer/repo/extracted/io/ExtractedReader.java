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
//package edu.uci.ics.sourcerer.repo.extracted.io;
//
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.Iterator;
//import java.util.NoSuchElementException;
//import java.util.logging.Level;
//
//import edu.uci.ics.sourcerer.tools.java.model.extracted.ModelEX;
//import edu.uci.ics.sourcerer.tools.java.model.extracted.ModelExParser;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class ExtractedReader <T extends ModelEX> implements Iterable<T>, Iterator<T> {
//  private ModelExParser<T> parser;
//  
//  private BufferedReader input;
//  
//  private T next = null;
//  
//  private ExtractedReader(ModelExParser<T> parser, File file) throws IOException {
//    this.parser = parser;
//    if (file.exists()) {
//      this.input = new BufferedReader(new FileReader(file));
//    }
//  }
//  
//  public static <T extends ModelEX> ExtractedReader<T> getExtractedReader(ModelExParser<T> parser, File file) {
//    try {
//      return new ExtractedReader<T>(parser, file);
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Unable to create reader", e);
//      return null;
//    }
//  }
//  
//  public int getCount() {
//    try {
//      int count = 0;
//      for (String line = input.readLine(); line != null; line = input.readLine()) {
//        count++;
//      }
//      input.close();
//      input = null;
//      return count;
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error getting count", e);
//      return -1;
//    }
//  }
//  
//  public Iterator<T> iterator() {
//    return this;
//  }
//
//  @Override
//  public boolean hasNext() {
//    while (true) {
//      if (next == null) {
//        if (input == null) {
//          return false;
//        } else {
//          try {
//            String line = input.readLine();
//            if (line == null) {
//              input.close();
//              input = null;
//              return false;
//            } else {
//              next = parser.parseLine(line);
//              if (next != null) {
//                return true;
//              }
//            }
//          } catch (IOException e) {
//            logger.log(Level.SEVERE, "Unable to read line.", e);
//            input = null;
//            return false;
//          }
//        }
//      } else {
//        return true;
//      }
//    }
//  }
//  
//  @Override
//  public T next() {
//    if (hasNext()) {
//      T entity = next;
//      next = null;
//      return entity;
//    } else { 
//      throw new NoSuchElementException();
//    }
//  }
//  
//  @Override
//  public void remove() {
//    throw new UnsupportedOperationException();
//  }
//}
