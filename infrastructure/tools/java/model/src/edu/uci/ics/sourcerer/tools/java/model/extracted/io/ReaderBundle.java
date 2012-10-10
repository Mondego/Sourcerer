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
package edu.uci.ics.sourcerer.tools.java.model.extracted.io;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.model.extracted.CommentEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.FileEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.MissingTypeEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.UsedJarEX;
import edu.uci.ics.sourcerer.util.CachedReference;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ReaderBundle {
  private final File input;
  private final File zip;
  
  private class ReaderReference<T extends SimpleSerializable> extends CachedReference<Collection<T>> {
    private final Class<T> klass;
    private final String entryName;
    
    private ReaderReference(Class<T> klass, Argument<String> entry) {
      this.klass = klass;
      this.entryName = entry.getValue();
    }
    
    @Override
    protected Collection<T> create() {
      return ReaderBundle.this.get(klass, entryName);
    }
    
    protected Iterable<T> getTransient() {
      Collection<T> result = getIfCached();
      if (result == null) {
        return ReaderBundle.this.getTransient(klass, entryName);
      } else {
        return result;
      }
    }
  }
  
  private ReaderReference<EntityEX> entities = new ReaderReference<EntityEX>(EntityEX.class, EntityEX.ENTITY_FILE);
  private ReaderReference<FileEX> files = new ReaderReference<FileEX>(FileEX.class, FileEX.FILE_FILE);
  private ReaderReference<LocalVariableEX> localVariables = new ReaderReference<LocalVariableEX>(LocalVariableEX.class, LocalVariableEX.LOCAL_VARIABLE_FILE);
  private ReaderReference<RelationEX> relations = new ReaderReference<RelationEX>(RelationEX.class, RelationEX.RELATION_FILE);
  private ReaderReference<ProblemEX> problems = new ReaderReference<ProblemEX>(ProblemEX.class, ProblemEX.PROBLEM_FILE);
  private ReaderReference<ImportEX> imports = new ReaderReference<ImportEX>(ImportEX.class, ImportEX.IMPORT_FILE);
  private ReaderReference<CommentEX> comments = new ReaderReference<CommentEX>(CommentEX.class, CommentEX.COMMENT_FILE);
  private ReaderReference<UsedJarEX> usedJars = new ReaderReference<UsedJarEX>(UsedJarEX.class, UsedJarEX.USED_JAR_FILE);
  private ReaderReference<MissingTypeEX> missingTypes = new ReaderReference<MissingTypeEX>(MissingTypeEX.class, MissingTypeEX.MISSING_TYPE_FILE);
  
  private ReaderBundle(File input, File zip) {
    this.input = input;
    this.zip = zip;
  }
  
  public static ReaderBundle create(File input, File zip) {
    return new ReaderBundle(input, zip); 
  }
  
  private <T extends SimpleSerializable> Collection<T> get(Class<T> klass, String fileName) {
    // Check for the uncompressed file
    File file = new File(input, fileName);
    if (file.exists()) {
      try {
        return IOUtils.deserialize(klass, file);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error reading extracted file.", e);
        return Collections.emptyList();
      }
    } else {
      // Check for the compressed file
      if (zip.exists()) {
        try {
          return IOUtils.deserialize(klass, zip, fileName);
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Error reading extracted file.", e);
          return Collections.emptyList();
        }
      } else {
        return Collections.emptyList();
      }
    }
  }
  
  private <T extends SimpleSerializable> Iterable<T> getTransient(Class<T> klass, String fileName) {
    // Check for the uncompressed file
    File file = new File(input, fileName);
    if (file.exists()) {
      try {
        return IOUtils.deserialize(klass, file, true);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error reading extracted file.", e);
        return Collections.emptyList();
      }
    } else {
      // Check for the compressed file (not transient)
      if (zip.exists()) {
        try {
          return IOUtils.deserialize(klass, zip, fileName);
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Error reading extracted file.", e);
          return Collections.emptyList();
        }
      } else {
        return Collections.emptyList();
      }
    }
  }
  
  public Collection<EntityEX> getEntities() {
    return entities.get();
  }
  
  public Iterable<EntityEX> getTransientEntities() {
    return entities.getTransient();
  }
  
  public Collection<FileEX> getFiles() {
    return files.get();
  }
  
  public Iterable<FileEX> getTransientFiles() {
    return files.getTransient();
  }
  
  public Collection<LocalVariableEX> getLocalVariables() {
    return localVariables.get();
  }
  
  public Iterable<LocalVariableEX> getTransientLocalVariables() {
    return localVariables.getTransient();
  }
  
  public Collection<RelationEX> getRelations() {
    return relations.get();
  }
  
  public Iterable<RelationEX> getTransientRelations() {
    return relations.getTransient();
  }
  
  public Collection<ProblemEX> getProblems() {
    return problems.get();
  }
  
  public Iterable<ProblemEX> getTransientProblems() {
    return problems.getTransient();
  }
  
  public Collection<ImportEX> getImports() {
    return imports.get();
  }
  
  public Iterable<ImportEX> getTransientImports() {
    return imports.getTransient();
  }
  
  public Collection<CommentEX> getComments() {
    return comments.get();
  }
  
  public Iterable<CommentEX> getTransientComments() {
    return comments.getTransient();
  }
  
  public Collection<UsedJarEX> getUsedJars() {
    return usedJars.get();
  }
  
  public Iterable<UsedJarEX> getTransientUsedJars() {
    return usedJars.getTransient();
  }
  
  public Collection<MissingTypeEX> getMissingTypes() {
    return missingTypes.get();
  }
  
  public Iterable<MissingTypeEX> getTransientMissingTypes() {
    return missingTypes.getTransient();
  }
}
