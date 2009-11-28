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
package edu.uci.ics.sourcerer.extractor.io;

import java.io.File;

import edu.uci.ics.sourcerer.extractor.io.dummy.DummyCommentWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyEntityWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyFileWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyImportWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyJarEntityWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyUsedJarWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyJarRelationWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyLocalVariableWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyMissingTypeWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyProblemWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyRelationWriter;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.ClassProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class WriterBundle implements IWriterBundle {
  public static final Property<Class<?>> IMPORT_WRITER = new ClassProperty("import-writer", DummyImportWriter.class, "Extractor Output", "Import writer.");
  public static final Property<Class<?>> PROBLEM_WRITER = new ClassProperty("problem-writer", DummyProblemWriter.class, "Extractor Output", "Problem writer.");
  public static final Property<Class<?>> ENTITY_WRITER = new ClassProperty("entity-writer", DummyEntityWriter.class, "Extractor Output", "Entity writer.");
  public static final Property<Class<?>> JAR_ENTITY_WRITER = new ClassProperty("jar-entity-writer", DummyJarEntityWriter.class, "Extractor Output", "Jar entity writer.");
  public static final Property<Class<?>> LOCAL_VARIABLE_WRITER = new ClassProperty("local-variable-writer", DummyLocalVariableWriter.class, "Extractor Output", "Local variable writer.");
  public static final Property<Class<?>> RELATION_WRITER = new ClassProperty("relation-writer", DummyRelationWriter.class, "Extractor Output", "Relation writer.");
  public static final Property<Class<?>> JAR_RELATION_WRITER = new ClassProperty("jar-relation-writer", DummyJarRelationWriter.class, "Extractor Output", "Jar relation writer.");
  public static final Property<Class<?>> COMMENT_WRITER = new ClassProperty("comment-writer", DummyCommentWriter.class, "Extractor Output", "Comment writer.");
  public static final Property<Class<?>> FILE_WRITER = new ClassProperty("file-writer", DummyFileWriter.class, "Extractor Output", "File writer.");
  public static final Property<Class<?>> USED_JAR_WRITER = new ClassProperty("used-jar-writer", DummyUsedJarWriter.class, "Extractor Output", "Jar file writer.");
  public static final Property<Class<?>> MISSING_TYPE_WRITER = new ClassProperty("missing-class-writer", DummyMissingTypeWriter.class, "Extractor Output", "Missing type writer.");
  
  private IImportWriter importWriter;
  private IProblemWriter problemWriter;
  private IEntityWriter entityWriter;
  private IJarEntityWriter jarEntityWriter;
  private ILocalVariableWriter localVariableWriter;
  private IRelationWriter relationWriter;
  private IJarRelationWriter jarRelationWriter;
  private ICommentWriter commentWriter;
  private IFileWriter fileWriter;
  private IUsedJarWriter usedJarWriter;
  private IMissingTypeWriter missingTypeWriter;
  
  private Repository input;
  
  private File output;

  public WriterBundle(File output) {
    this.output = output;
    output.mkdirs();
  }
  
  public WriterBundle(File output, Repository input) {
    this(output);
    this.input = input;
  }

  public IImportWriter getImportWriter() {
    if (importWriter == null) {
      importWriter = WriterFactory.createWriter(output, input, IMPORT_WRITER);
    }
    return importWriter;
  }
  
  public IProblemWriter getProblemWriter() {
    if (problemWriter == null) {
      problemWriter = WriterFactory.createWriter(output, input, PROBLEM_WRITER);
    }
    return problemWriter;
  }
  
  public IEntityWriter getEntityWriter() {
    if (entityWriter == null) {
      entityWriter = WriterFactory.createWriter(output, input, ENTITY_WRITER);
    }
    return entityWriter;
  }
  
  public IJarEntityWriter getJarEntityWriter() {
    if (jarEntityWriter == null) {
      jarEntityWriter = WriterFactory.createWriter(output, input, JAR_ENTITY_WRITER);
    }
    return jarEntityWriter;
  }
  
  public ILocalVariableWriter getLocalVariableWriter() {
    if (localVariableWriter == null) {
      localVariableWriter = WriterFactory.createWriter(output, input, LOCAL_VARIABLE_WRITER);
    }
    return localVariableWriter;
  }
  
  public IRelationWriter getRelationWriter() {
    if (relationWriter == null) {
      relationWriter = WriterFactory.createWriter(output, input, RELATION_WRITER);
    }
    return relationWriter;
  }
  
  public IJarRelationWriter getJarRelationWriter() {
    if (jarRelationWriter == null) {
      jarRelationWriter = WriterFactory.createWriter(output, input, JAR_RELATION_WRITER);
    }
    return jarRelationWriter;
  }
  
  public ICommentWriter getCommentWriter() {
    if (commentWriter == null) {
      commentWriter = WriterFactory.createWriter(output, input, COMMENT_WRITER);
    }
    return commentWriter;
  }
  
  public IFileWriter getFileWriter() {
    if (fileWriter == null) {
      fileWriter = WriterFactory.createWriter(output, input, FILE_WRITER);
    }
    return fileWriter;
  }
  
  public IUsedJarWriter getUsedJarWriter() {
    if (usedJarWriter == null) {
      usedJarWriter = WriterFactory.createWriter(output, input, USED_JAR_WRITER);
    }
    return usedJarWriter;
  }
  
  public IMissingTypeWriter getMissingTypeWriter() {
    if (missingTypeWriter == null) {
      missingTypeWriter = WriterFactory.createWriter(output, input, MISSING_TYPE_WRITER);
    }
    return missingTypeWriter;
  }

  public void close() {
    close(importWriter);
    close(problemWriter);
    close(entityWriter);
    close(jarEntityWriter);
    close(localVariableWriter);
    close(relationWriter);
    close(jarRelationWriter);
    close(commentWriter);
    close(fileWriter);
    close(usedJarWriter);
    close(missingTypeWriter);
  }
  
  private void close(IExtractorWriter writer) {
    if (writer != null) {
      writer.close();
    }
  }
}