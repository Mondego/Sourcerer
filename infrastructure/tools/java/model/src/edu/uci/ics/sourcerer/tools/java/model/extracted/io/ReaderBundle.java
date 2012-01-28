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
import edu.uci.ics.sourcerer.tools.java.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.UsedJarEX;
import edu.uci.ics.sourcerer.util.CachedReference;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ReaderBundle {
  private final File input;
  
  private CachedReference<Collection<EntityEX>> entities = new CachedReference<Collection<EntityEX>>() {
    @Override
    protected Collection<EntityEX> make() {
      return ReaderBundle.this.get(EntityEX.class, new File(input, EntityEX.ENTITY_FILE.getValue()));
    }
  };
  private CachedReference<Collection<FileEX>> files = new CachedReference<Collection<FileEX>>() {
    @Override
    protected Collection<FileEX> make() {
      return ReaderBundle.this.get(FileEX.class, new File(input, FileEX.FILE_FILE.getValue()));
    }
  };
  private CachedReference<Collection<LocalVariableEX>> localVariables = new CachedReference<Collection<LocalVariableEX>>() {
    @Override
    protected Collection<LocalVariableEX> make() {
      return ReaderBundle.this.get(LocalVariableEX.class, new File(input, LocalVariableEX.LOCAL_VARIABLE_FILE.getValue()));
    }
  };
  private CachedReference<Collection<RelationEX>> relations = new CachedReference<Collection<RelationEX>>() {
    @Override
    protected Collection<RelationEX> make() {
      return ReaderBundle.this.get(RelationEX.class, new File(input, RelationEX.RELATION_FILE.getValue()));
    }
  };
  private CachedReference<Collection<ProblemEX>> problems = new CachedReference<Collection<ProblemEX>>() {
    @Override
    protected Collection<ProblemEX> make() {
      return ReaderBundle.this.get(ProblemEX.class, new File(input, ProblemEX.PROBLEM_FILE.getValue()));
    }
  };
  private CachedReference<Collection<ImportEX>> imports = new CachedReference<Collection<ImportEX>>() {
    @Override
    protected Collection<ImportEX> make() {
      return ReaderBundle.this.get(ImportEX.class, new File(input, ImportEX.IMPORT_FILE.getValue()));
    }
  };
  private CachedReference<Collection<CommentEX>> comments = new CachedReference<Collection<CommentEX>>() {
    @Override
    protected Collection<CommentEX> make() {
      return ReaderBundle.this.get(CommentEX.class, new File(input, CommentEX.COMMENT_FILE.getValue()));
    }
  };
  private CachedReference<Collection<UsedJarEX>> usedJars = new CachedReference<Collection<UsedJarEX>>() {
    @Override
    protected Collection<UsedJarEX> make() {
      return ReaderBundle.this.get(UsedJarEX.class, new File(input, UsedJarEX.USED_JAR_FILE.getValue()));
    }
  };
  
  public ReaderBundle(File input) {
    this.input = input;
  }
  
  private <T extends SimpleSerializable> Collection<T> get(Class<T> klass, File file) {
    if (file.exists()) {
      try {
        return IOUtils.deserialize(klass, file);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error reading extracted file.", e);
        return Collections.emptyList();
      }
    } else {
      return Collections.emptyList();
    }
  }
  
  private <T extends SimpleSerializable> Iterable<T> getTransient(Class<T> klass, File file) {
    if (file.exists()) {
      try {
        return IOUtils.deserialize(klass, file, true);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error reading extracted file.", e);
        return Collections.emptyList();
      }
    } else {
      return Collections.emptyList();
    }
  }
  
  public Collection<EntityEX> getEntities() {
    return entities.get();
  }
  
  public Iterable<EntityEX> getTransientEntities() {
    Iterable<EntityEX> result = entities.getIfCached();
    if (result == null) {
      result = getTransient(EntityEX.class, new File(input, EntityEX.ENTITY_FILE.getValue()));
    }
    return result;
  }
  
  public Collection<FileEX> getFiles() {
    return files.get();
  }
  
  public Iterable<FileEX> getTransientFiles() {
    Iterable<FileEX> result = files.getIfCached();
    if (result == null) {
      result = getTransient(FileEX.class, new File(input, FileEX.FILE_FILE.getValue()));
    }
    return result;
  }
  
  public Collection<LocalVariableEX> getLocalVariables() {
    return localVariables.get();
  }
  
  public Iterable<LocalVariableEX> getTransientLocalVariables() {
    Iterable<LocalVariableEX> result = localVariables.getIfCached();
    if (result == null) {
      result = getTransient(LocalVariableEX.class, new File(input, LocalVariableEX.LOCAL_VARIABLE_FILE.getValue()));
    }
    return result;
  }
  
  public Collection<RelationEX> getRelations() {
    return relations.get();
  }
  
  public Iterable<RelationEX> getTransientRelations() {
    Iterable<RelationEX> result = relations.getIfCached();
    if (result == null) {
      result = getTransient(RelationEX.class, new File(input, RelationEX.RELATION_FILE.getValue()));
    }
    return result;
  }
  
  public Collection<ProblemEX> getProblems() {
    return problems.get();
  }
  
  public Iterable<ProblemEX> getTransientProblems() {
    Iterable<ProblemEX> result = problems.getIfCached();
    if (result == null) {
      result = getTransient(ProblemEX.class, new File(input, ProblemEX.PROBLEM_FILE.getValue()));
    }
    return result;
  }
  
  public Collection<ImportEX> getImports() {
    return imports.get();
  }
  
  public Iterable<ImportEX> getTransientImports() {
    Iterable<ImportEX> result = imports.getIfCached();
    if (result == null) {
      result = getTransient(ImportEX.class, new File(input, ImportEX.IMPORT_FILE.getValue()));
    }
    return result;
  }
  
  public Collection<CommentEX> getComments() {
    return comments.get();
  }
  
  public Iterable<CommentEX> getTransientComments() {
    Iterable<CommentEX> result = comments.getIfCached();
    if (result == null) {
      result = getTransient(CommentEX.class, new File(input, CommentEX.COMMENT_FILE.getValue()));
    }
    return result;
  }
  
  public Collection<UsedJarEX> getUsedJars() {
    return usedJars.get();
  }
  
  public Iterable<UsedJarEX> getTransientUsedJars() {
    Iterable<UsedJarEX> result = usedJars.getIfCached();
    if (result == null) {
      result = getTransient(UsedJarEX.class, new File(input, UsedJarEX.USED_JAR_FILE.getValue()));
    }
    return result;
  }
}
