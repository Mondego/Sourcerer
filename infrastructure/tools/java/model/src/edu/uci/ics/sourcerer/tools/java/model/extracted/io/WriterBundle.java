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

import java.io.Closeable;
import java.io.File;

import edu.uci.ics.sourcerer.tools.java.model.extracted.dummy.DummyCommentWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.dummy.DummyEntityWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.dummy.DummyFileWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.dummy.DummyImportWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.dummy.DummyLocalVariableWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.dummy.DummyMissingTypeWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.dummy.DummyProblemWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.dummy.DummyRelationWriter;
import edu.uci.ics.sourcerer.tools.java.model.extracted.dummy.DummyUsedJarWriter;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.ClassArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class WriterBundle implements Closeable {
  public static final Argument<Class<?>> IMPORT_WRITER = new ClassArgument("import-writer", DummyImportWriter.class, "Import writer.").permit();
  public static final Argument<Class<?>> PROBLEM_WRITER = new ClassArgument("problem-writer", DummyProblemWriter.class, "Problem writer.").permit();
  public static final Argument<Class<?>> ENTITY_WRITER = new ClassArgument("entity-writer", DummyEntityWriter.class, "Entity writer.").permit();
  public static final Argument<Class<?>> LOCAL_VARIABLE_WRITER = new ClassArgument("local-variable-writer", DummyLocalVariableWriter.class, "Local variable writer.").permit();
  public static final Argument<Class<?>> RELATION_WRITER = new ClassArgument("relation-writer", DummyRelationWriter.class, "Relation writer.").permit();
  public static final Argument<Class<?>> COMMENT_WRITER = new ClassArgument("comment-writer", DummyCommentWriter.class, "Comment writer.").permit();
  public static final Argument<Class<?>> FILE_WRITER = new ClassArgument("file-writer", DummyFileWriter.class, "File writer.").permit();
  public static final Argument<Class<?>> USED_JAR_WRITER = new ClassArgument("used-jar-writer", DummyUsedJarWriter.class, "Jar file writer.").permit();
  public static final Argument<Class<?>> MISSING_TYPE_WRITER = new ClassArgument("missing-class-writer", DummyMissingTypeWriter.class, "Missing type writer.").permit();
  
  private ImportWriter importWriter;
  private ProblemWriter problemWriter;
  private EntityWriter entityWriter;
  private LocalVariableWriter localVariableWriter;
  private RelationWriter relationWriter;
  private CommentWriter commentWriter;
  private FileWriter fileWriter;
  private UsedJarWriter usedJarWriter;
  private MissingTypeWriter missingTypeWriter;
  
  private final File output;

  public WriterBundle() {
    output = null;
  }
  
  public WriterBundle(File output) {
    this.output = output;
    output.mkdirs();
  }
  
  public File getOutput() {
    return output;
  }

  public ImportWriter getImportWriter() {
    if (importWriter == null) {
      importWriter = WriterFactory.createWriter(output, IMPORT_WRITER);
    }
    return importWriter;
  }
  
  public ProblemWriter getProblemWriter() {
    if (problemWriter == null) {
      problemWriter = WriterFactory.createWriter(output, PROBLEM_WRITER);
    }
    return problemWriter;
  }
  
  public EntityWriter getEntityWriter() {
    if (entityWriter == null) {
      entityWriter = WriterFactory.createWriter(output, ENTITY_WRITER);
    }
    return entityWriter;
  }
  
  public LocalVariableWriter getLocalVariableWriter() {
    if (localVariableWriter == null) {
      localVariableWriter = WriterFactory.createWriter(output, LOCAL_VARIABLE_WRITER);
    }
    return localVariableWriter;
  }
  
  public RelationWriter getRelationWriter() {
    if (relationWriter == null) {
      relationWriter = WriterFactory.createWriter(output, RELATION_WRITER);
    }
    return relationWriter;
  }
  
  public CommentWriter getCommentWriter() {
    if (commentWriter == null) {
      commentWriter = WriterFactory.createWriter(output, COMMENT_WRITER);
    }
    return commentWriter;
  }
  
  public FileWriter getFileWriter() {
    if (fileWriter == null) {
      fileWriter = WriterFactory.createWriter(output, FILE_WRITER);
    }
    return fileWriter;
  }
  
  public UsedJarWriter getUsedJarWriter() {
    if (usedJarWriter == null) {
      usedJarWriter = WriterFactory.createWriter(output, USED_JAR_WRITER);
    }
    return usedJarWriter;
  }
  
  public MissingTypeWriter getMissingTypeWriter() {
    if (missingTypeWriter == null) {
      missingTypeWriter = WriterFactory.createWriter(output, MISSING_TYPE_WRITER);
    }
    return missingTypeWriter;
  }

  @Override
  public void close() {
    IOUtils.close(importWriter, problemWriter, entityWriter, localVariableWriter, relationWriter, commentWriter, fileWriter, usedJarWriter, missingTypeWriter);
  }
}