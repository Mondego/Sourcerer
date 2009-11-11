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
package edu.uci.ics.sourcerer.repo.extracted;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class Extracted {
  public static final Property<String> ENTITY_FILE = new StringProperty("entity-file", "entities.txt", "Repository Manager", "Filename for the extracted entities.");
  public static final Property<String> RELATION_FILE = new StringProperty("relation-file", "relations.txt", "Repository Manager", "Filename for the extracted relations.");
  public static final Property<String> LOCAL_VARIABLE_FILE = new StringProperty("local-variables-file", "local-variables.txt", "Repository Manager", "Filename for the extracted local variables / parameters.");
  public static final Property<String> COMMENT_FILE = new StringProperty("comment-file", "comments.txt", "Repository Manager", "Filename for the extracted comments.");
  public static final Property<String> FILE_FILE = new StringProperty("file-file", "files.txt", "Repository Manager", "Filename for the extracted files.");
  public static final Property<String> PROBLEM_FILE = new StringProperty("problem-file", "problems.txt", "Repository Manager", "Filename for the extracted problems.");
  public static final Property<String> IMPORT_FILE = new StringProperty("import-file", "imports.txt", "Repository Manager", "Filename for the extracted imports.");
  public static final Property<String> JAR_FILE_FILE = new StringProperty("jar-file-file", "jars.txt", "Repository Manager", "Filename for used jar files.");
  public static final Property<String> MISSING_TYPE_FILE = new StringProperty("missing-type-file", "missing-types.txt", "Repository Manager", "Filename for missing types.");
  
  protected File content;

  public Extracted(File content) {
    this.content = content;
  }

  public File getContent() {
    return content;
  }
  
  protected File getPropertiesFile() {
    return new File(content, ".properties");
  }
  
  protected InputStream getInputStream(Property<String> property) throws IOException {
    File file = new File(content, property.getValue());
    return new FileInputStream(file);
  }

  public InputStream getEntityInputStream() throws IOException {
    return getInputStream(ENTITY_FILE); 
  }
  
  public InputStream getRelationInputStream() throws IOException {
    return getInputStream(RELATION_FILE);
  }
  
  public InputStream getLocalVariableInputStream() throws IOException {
    return getInputStream(LOCAL_VARIABLE_FILE);
  }
  
  public InputStream getCommentInputStream() throws IOException {
    return getInputStream(COMMENT_FILE);
  }
  
  public InputStream getFileInputStream() throws IOException {
    return getInputStream(FILE_FILE);
  }
  
  public InputStream getProblemInputStream() throws IOException {
    return getInputStream(PROBLEM_FILE);
  }
  
  public InputStream getImportInputStream() throws IOException {
    return getInputStream(IMPORT_FILE);
  }
  
  public InputStream getMissingTypeInputStream() throws IOException {
    return getInputStream(MISSING_TYPE_FILE);
  }
}