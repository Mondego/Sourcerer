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
package edu.uci.ics.sourcerer.repo.extracted.io;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.EntityExJarParser;
import edu.uci.ics.sourcerer.model.extracted.EntityExParser;
import edu.uci.ics.sourcerer.model.extracted.FileEX;
import edu.uci.ics.sourcerer.model.extracted.FileExParser;
import edu.uci.ics.sourcerer.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.model.extracted.ImportExParser;
import edu.uci.ics.sourcerer.model.extracted.JarEX;
import edu.uci.ics.sourcerer.model.extracted.JarExParser;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableExJarParser;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableExParser;
import edu.uci.ics.sourcerer.model.extracted.ModelEX;
import edu.uci.ics.sourcerer.model.extracted.ModelExParser;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.model.extracted.ProblemExParser;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.model.extracted.RelationExJarParser;
import edu.uci.ics.sourcerer.model.extracted.RelationExParser;
import edu.uci.ics.sourcerer.repo.extracted.Extracted;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedProject;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedReader <T extends ModelEX> implements Iterable<T>, Iterator<T> {
  private ModelExParser<T> parser;
  
  private BufferedReader input;
  
  private T next = null;
  
  private ExtractedReader(ModelExParser<T> parser, File entityFile) throws IOException {
    this.parser = parser;
    this.input = new BufferedReader(new FileReader(entityFile));
  }
  
  private ExtractedReader(ModelExParser<T> parser, InputStream entityInputStream) throws IOException {
    this.parser = parser;
    this.input = new BufferedReader(new InputStreamReader(entityInputStream));
  }
  
  public Iterator<T> iterator() {
    return this;
  }

  @Override
  public boolean hasNext() {
    if (next == null) {
      if (input == null) {
        return false;
      } else {
        try {
          String line = input.readLine();
          if (line == null) {
            input.close();
            input = null;
            return false;
          } else {
            next = parser.parseLine(line);
            if (next == null) {
              return hasNext();
            } else {
              return true;
            }
          }
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Unable to read line.", e);
          input = null;
          return false;
        }
      }
    } else {
      return true;
    }
  }
  
  @Override
  public T next() {
    if (hasNext()) {
      T entity = next;
      next = null;
      return entity;
    } else { 
      throw new NoSuchElementException();
    }
  }
  
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
  
  public static ExtractedReader<EntityEX> getJarEntityReader(Extracted jar) {
    try {
      return new ExtractedReader<EntityEX>(EntityExJarParser.getParser(), jar.getEntityInputStream());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to create jar entity reader", e);
      return null;
    }
  }
  
  public static ExtractedReader<RelationEX> getJarRelationReader(Extracted jar) {
    try {
      return new ExtractedReader<RelationEX>(RelationExJarParser.getParser(), jar.getRelationInputStream());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to create jar relation reader", e);
      return null;
    }
  }
  
  public static ExtractedReader<LocalVariableEX> getJarLocalVariableReader(Extracted jar) {
    try {
      return new ExtractedReader<LocalVariableEX>(LocalVariableExJarParser.getParser(), jar.getLocalVariableInputStream());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to create jar local variable reader", e);
      return null;
    }
  }
  
  public static ExtractedReader<FileEX> getFileReader(ExtractedProject project) {
    try {
      return new ExtractedReader<FileEX>(FileExParser.getParser(), project.getFileInputStream());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to create file reader", e);
      return null;
    }
  }
  
  public static ExtractedReader<ProblemEX> getProblemReader(ExtractedProject project) {
    try {
      return new ExtractedReader<ProblemEX>(ProblemExParser.getParser(), project.getProblemInputStream());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to create problem reader", e);
      return null;
    }
  }
  
  public static ExtractedReader<EntityEX> getEntityReader(ExtractedProject project) {
    try {
      return new ExtractedReader<EntityEX>(EntityExParser.getParser(), project.getEntityInputStream());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to create entity reader", e);
      return null;
    }
  }
  
  public static ExtractedReader<JarEX> getJarReader(ExtractedProject project) {
    try {
      return new ExtractedReader<JarEX>(JarExParser.getParser(), project.getJarInputStream());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to create jar reader", e);
      return null;
    }
  }
  
  public static ExtractedReader<ImportEX> getImportReader(ExtractedProject project) {
    try {
      return new ExtractedReader<ImportEX>(ImportExParser.getParser(), project.getImportInputStream());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to create import reader", e);
      return null;
    }
  }
  
  public static ExtractedReader<RelationEX> getRelationReader(ExtractedProject project) {
    try {
      return new ExtractedReader<RelationEX>(RelationExParser.getParser(), project.getRelationInputStream());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to create relation reader", e);
      return null;
    }
  }
  
  public static ExtractedReader<LocalVariableEX> getLocalVariableReader(ExtractedProject project) {
    try {
      return new ExtractedReader<LocalVariableEX>(LocalVariableExParser.getParser(), project.getLocalVariableInputStream());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to create local variable reader", e);
      return null;
    }
  }
}
