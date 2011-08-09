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

import edu.uci.ics.sourcerer.model.extracted.CommentEX;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.FileEX;
import edu.uci.ics.sourcerer.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.model.extracted.MissingTypeEX;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.model.extracted.UsedJarEX;
import edu.uci.ics.sourcerer.repo.extracted.io.ExtractedReader;
import edu.uci.ics.sourcerer.repo.general.AbstractExtractedProperties;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;
import edu.uci.ics.sourcerer.util.io.internal.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class Extracted {
  public static final Argument<String> ENTITY_FILE = new StringArgument("entity-file", "entities.txt", "Filename for the extracted entities.");
  public static final Argument<String> RELATION_FILE = new StringArgument("relation-file", "relations.txt", "Filename for the extracted relations.");
  public static final Argument<String> LOCAL_VARIABLE_FILE = new StringArgument("local-variables-file", "local-variables.txt", "Filename for the extracted local variables / parameters.");
  public static final Argument<String> COMMENT_FILE = new StringArgument("comment-file", "comments.txt", "Filename for the extracted comments.");
  public static final Argument<String> FILE_FILE = new StringArgument("file-file", "files.txt", "Filename for the extracted files.");
  public static final Argument<String> PROBLEM_FILE = new StringArgument("problem-file", "problems.txt", "Filename for the extracted problems.");
  public static final Argument<String> IMPORT_FILE = new StringArgument("import-file", "imports.txt", "Filename for the extracted imports.");
  public static final Argument<String> USED_JAR_FILE = new StringArgument("used-jar-file", "used-jars.txt", "Filename for used jar files.");
  public static final Argument<String> MISSING_TYPE_FILE = new StringArgument("missing-type-file", "missing-types.txt", "Filename for missing types.");
  
  protected RepoFileImpl content;

  public Extracted(RepoFileImpl content) {
    this.content = content;
  }

  public File getOutputDir() {
    return content.toDir();
  }
  
  public String getRelativePath() {
    return content.getRelativePath();
  }
  
  protected File getPropertiesFile() {
    return content.getChildFile("extracted.properties");
  }
  
  protected void clonePropertiesFile(ExtractedRepository target) {
    File properties = getPropertiesFile();
    if (properties.exists()) {
      File newProperties = target.rebasePath(content).getChildFile("extracted.properties");
      FileUtils.copyFile(properties, newProperties);
    }
  }
  
  protected File getInputFile(Argument<String> property) {
    return content.getChildFile(property.getValue());
  }
  
  protected InputStream getInputStream(Argument<String> property) throws IOException {
    File file = getInputFile(property);
    if (file.exists()) {
      return new FileInputStream(file);
    } else {
      return null;
    }
  }
  
  public InputStream getFileInputStream() throws IOException {
    return getInputStream(FILE_FILE);
  }
  
  public ExtractedReader<FileEX> getFileReader() {
    return ExtractedReader.getExtractedReader(FileEX.getParser(), getInputFile(FILE_FILE));
  }
  
  public InputStream getImportInputStream() throws IOException {
    return getInputStream(IMPORT_FILE);
  }
  
  public ExtractedReader<ImportEX> getImportReader() {
    return ExtractedReader.getExtractedReader(ImportEX.getParser(), getInputFile(IMPORT_FILE));
  }
  
  public InputStream getProblemInputStream() throws IOException {
    return getInputStream(PROBLEM_FILE);
  }
  
  public ExtractedReader<ProblemEX> getProblemReader() {
    return ExtractedReader.getExtractedReader(ProblemEX.getParser(), getInputFile(PROBLEM_FILE));
  }
  
  public InputStream getCommentInputStream() throws IOException {
    return getInputStream(COMMENT_FILE);
  }
 
  public ExtractedReader<CommentEX> getCommentReader() {
    return ExtractedReader.getExtractedReader(CommentEX.getParser(), getInputFile(COMMENT_FILE));
  }

  public InputStream getEntityInputStream() throws IOException {
    return getInputStream(ENTITY_FILE); 
  }
  
  public ExtractedReader<EntityEX> getEntityReader() {
    return ExtractedReader.getExtractedReader(EntityEX.getParser(), getInputFile(ENTITY_FILE));
  }
  
  public InputStream getRelationInputStream() throws IOException {
    return getInputStream(RELATION_FILE);
  }
  
  public ExtractedReader<RelationEX> getRelationReader() {
    return ExtractedReader.getExtractedReader(RelationEX.getParser(), getInputFile(RELATION_FILE));
  }
  
  public InputStream getLocalVariableInputStream() throws IOException {
    return getInputStream(LOCAL_VARIABLE_FILE);
  }
  
  public ExtractedReader<LocalVariableEX> getLocalVariableReader() {
    return ExtractedReader.getExtractedReader(LocalVariableEX.getParser(), getInputFile(LOCAL_VARIABLE_FILE));
  }
  
  public InputStream getMissingTypeInputStream() throws IOException {
    return getInputStream(MISSING_TYPE_FILE);
  }
  
  public ExtractedReader<MissingTypeEX> getMissingTypeReader() {
    return ExtractedReader.getExtractedReader(MissingTypeEX.getParser(), getInputFile(MISSING_TYPE_FILE));
  }
  
  public InputStream getUsedJarInputStream() throws IOException {
    return getInputStream(USED_JAR_FILE);
  }
  
  public ExtractedReader<UsedJarEX> getUsedJarReader() {
    return ExtractedReader.getExtractedReader(UsedJarEX.getParser(), getInputFile(USED_JAR_FILE));
  }
  
  public boolean usesJars() {
    return getInputFile(USED_JAR_FILE).exists();
  }
  
  protected abstract AbstractExtractedProperties getProperties();
  
  public String getName() {
    return getProperties().getName();
  }
  
  public boolean extracted() {
    return getProperties().extracted();
  }
  
  public boolean reallyExtracted() {
    File file = getInputFile(FILE_FILE);
    return file.exists() && file.length() > 0;
  }
  
  public boolean hasMissingTypes() {
    return getProperties().missingTypes();
  }
  
  public boolean empty() {
    return !hasSource();
  }
  
  public boolean hasSource() {
    return getExtractedFromSource() + getSourceExceptions() > 0;
  }
  
  public int getExtractedFromSource() {
    return getProperties().getExtractedFromSource();
  }
  
  public boolean hasSourceExceptions() {
    return getSourceExceptions() > 0;
  }
  
  public int getSourceExceptions() {
    return getProperties().getSourceExceptions();
  }
  
  public int getFirstOrderJars() {
    return getProperties().getFirstOrderJars();
  }
  
  public int getJars() {
    return getProperties().getJars();
  }
  
  public String getHash() {
    return null;
  }
  
  public String getGroup() {
    return null;
  }
  
  public String getVersion() {
    return null;
  }
  
  public String getDescription() {
    return null;
  }
  
  @Override
  public String toString() {
    return getProperties().getName();
  }
}