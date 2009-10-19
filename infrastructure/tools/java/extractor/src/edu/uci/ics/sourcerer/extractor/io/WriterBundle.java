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

import edu.uci.ics.sourcerer.extractor.io.dummy.DummyCommentWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyEntityWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyFileWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyImportWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyJarEntityWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyJarRelationWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyLocalVariableWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyProblemWriter;
import edu.uci.ics.sourcerer.extractor.io.dummy.DummyRelationWriter;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.properties.ClassProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class WriterBundle {
  public static final Property<Class<?>> IMPORT_WRITER = new ClassProperty("import-writer", DummyImportWriter.class, "Extractor", "Import writer.");
  private IImportWriter importWriter;
  private IProblemWriter problemWriter;
  private IEntityWriter entityWriter;
  private IJarEntityWriter jarEntityWriter;
  private ILocalVariableWriter localVariableWriter;
  private IRelationWriter relationWriter;
  private IJarRelationWriter jarRelationWriter;
  private ICommentWriter commentWriter;
  private IFileWriter fileWriter;
  
  private Repository input;

  public WriterBundle() {
    PropertyManager properties = PropertyManager.getProperties();
    properties.getValueAsFile(Property.OUTPUT).mkdirs();
  }
  
  public WriterBundle(Repository input) {
    this.input = input;
    PropertyManager properties = PropertyManager.getProperties();
    properties.getValueAsFile(Property.OUTPUT).mkdirs();
  }

  public IImportWriter getImportWriter() {
    if (importWriter == null) {
      importWriter = WriterFactory.createWriter(input, Property.IMPORT_WRITER, DummyImportWriter.class);
    }
    return importWriter;
  }
  
  public IProblemWriter getProblemWriter() {
    if (problemWriter == null) {
      problemWriter = WriterFactory.createWriter(input, Property.PROBLEM_WRITER, DummyProblemWriter.class);
    }
    return problemWriter;
  }
  
  public IEntityWriter getEntityWriter() {
    if (entityWriter == null) {
      entityWriter = WriterFactory.createWriter(input, Property.ENTITY_WRITER, DummyEntityWriter.class);
    }
    return entityWriter;
  }
  
  public IJarEntityWriter getJarEntityWriter() {
    if (jarEntityWriter == null) {
      jarEntityWriter = WriterFactory.createWriter(input, Property.JAR_ENTITY_WRITER, DummyJarEntityWriter.class);
    }
    return jarEntityWriter;
  }
  
  public ILocalVariableWriter getLocalVariableWriter() {
    if (localVariableWriter == null) {
      localVariableWriter = WriterFactory.createWriter(input, Property.LOCAL_VARIABLE_WRITER, DummyLocalVariableWriter.class);
    }
    return localVariableWriter;
  }
  
  public IRelationWriter getRelationWriter() {
    if (relationWriter == null) {
      relationWriter = WriterFactory.createWriter(input, Property.RELATION_WRITER, DummyRelationWriter.class);
    }
    return relationWriter;
  }
  
  public IJarRelationWriter getJarRelationWriter() {
    if (jarRelationWriter == null) {
      jarRelationWriter = WriterFactory.createWriter(input, Property.JAR_RELATION_WRITER, DummyJarRelationWriter.class);
    }
    return jarRelationWriter;
  }
  
  public ICommentWriter getCommentWriter() {
    if (commentWriter == null) {
      commentWriter = WriterFactory.createWriter(input, Property.COMMENT_WRITER, DummyCommentWriter.class);
    }
    return commentWriter;
  }
  
  public IFileWriter getFileWriter() {
    if (fileWriter == null) {
      fileWriter = WriterFactory.createWriter(input, Property.FILE_WRITER, DummyFileWriter.class);
    }
    return fileWriter;
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
  }
  
  private void close(IExtractorWriter writer) {
    if (writer != null) {
      writer.close();
    }
  }
}