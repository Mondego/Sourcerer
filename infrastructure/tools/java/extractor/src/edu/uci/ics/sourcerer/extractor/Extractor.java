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
package edu.uci.ics.sourcerer.extractor;

import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.CLASS_ENTITY_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.CLASS_FILE_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.CLASS_RELATION_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.COMMENT_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.ENTITY_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.FILE_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.IMPORT_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.LOCAL_VARIABLE_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.MISSING_TYPE_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.PROBLEM_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.RELATION_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.USED_JAR_WRITER;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.COMMENT_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.ENTITY_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.FILE_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.IMPORT_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.LOCAL_VARIABLE_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.MISSING_TYPE_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.RELATION_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.USED_JAR_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.PROBLEM_FILE;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.OUTPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.JARS_DIR;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.JAR_FILTER;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.PROJECT_FILTER;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.JAR_INDEX_FILE;
import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_URL;
import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_USER;
import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_PASSWORD;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.extractor.io.file.ClassEntityWriter;
import edu.uci.ics.sourcerer.extractor.io.file.ClassFileWriter;
import edu.uci.ics.sourcerer.extractor.io.file.ClassRelationWriter;
import edu.uci.ics.sourcerer.extractor.io.file.CommentWriter;
import edu.uci.ics.sourcerer.extractor.io.file.EntityWriter;
import edu.uci.ics.sourcerer.extractor.io.file.FileWriter;
import edu.uci.ics.sourcerer.extractor.io.file.ImportWriter;
import edu.uci.ics.sourcerer.extractor.io.file.LocalVariableWriter;
import edu.uci.ics.sourcerer.extractor.io.file.MissingTypeWriter;
import edu.uci.ics.sourcerer.extractor.io.file.ProblemWriter;
import edu.uci.ics.sourcerer.extractor.io.file.RelationWriter;
import edu.uci.ics.sourcerer.extractor.io.file.UsedJarWriter;
import edu.uci.ics.sourcerer.extractor.resolver.MissingTypeResolver;
import edu.uci.ics.sourcerer.util.io.Command;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Extractor implements IApplication {
  public static final Property<Boolean> EXTRACT_LATEST_MAVEN = new BooleanProperty("extract-latest-maven", false, "Extract only the latest maven jars.");
  public static final Property<Boolean> EXTRACT_BINARY = new BooleanProperty("extract-binary", false, "Extract jars as binary only.");
  public static final Property<Boolean> USE_PROJECT_JARS = new BooleanProperty("use-project-jars", true, "Use project jars on the classpath.");
  public static final Property<Boolean> RESOLVE_MISSING_TYPES = new BooleanProperty("resolve-missing-types", false, "Attempt to resolve missing types.")
      .setRequiredProperties(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
  public static final Property<Boolean> SKIP_MISSING_TYPES = new BooleanProperty("skip-missing-types", false, "Skip extraction of projects with missing types.");
  public static final Property<Boolean> FORCE_MISSING_REDO = new BooleanProperty("force-missing-redo", false, "Re-attempt extraction on failed missing type extractions.");
  
  private static abstract class ExtractorCommand extends Command {
    protected DatabaseConnection connection = null;
    protected MissingTypeResolver resolver = null;
    
    public ExtractorCommand(String name, String description) {
      super(name, description);
    }
    
    @Override
    protected void execute() {
      Logging.initializeLogger(this);
      
      IMPORT_WRITER.setValue(ImportWriter.class);
      PROBLEM_WRITER.setValue(ProblemWriter.class);
      ENTITY_WRITER.setValue(EntityWriter.class);
      CLASS_ENTITY_WRITER.setValue(ClassEntityWriter.class);
      LOCAL_VARIABLE_WRITER.setValue(LocalVariableWriter.class);
      RELATION_WRITER.setValue(RelationWriter.class);
      CLASS_RELATION_WRITER.setValue(ClassRelationWriter.class);
      COMMENT_WRITER.setValue(CommentWriter.class);
      FILE_WRITER.setValue(FileWriter.class);
      CLASS_FILE_WRITER.setValue(ClassFileWriter.class);
      USED_JAR_WRITER.setValue(UsedJarWriter.class);
      MISSING_TYPE_WRITER.setValue(MissingTypeWriter.class);

      if (RESOLVE_MISSING_TYPES.getValue()) {
        connection = new DatabaseConnection();
        connection.open();
        resolver = new MissingTypeResolver(connection);
      }
      
      action();
      
      FileUtils.close(connection);
    }
  }
  
  public static final Command EXTRACT_LIBRARIES =
    new ExtractorCommand("extract-libraries", "Extract the libraries.") {
      protected void action() {
        LibraryExtractor.extract();
      }
    }.setProperties(OUTPUT_REPO, 
            IMPORT_FILE, PROBLEM_FILE, ENTITY_FILE, LOCAL_VARIABLE_FILE, RELATION_FILE, COMMENT_FILE, FILE_FILE, USED_JAR_FILE, MISSING_TYPE_FILE);
  
  public static final Command EXTRACT_JARS =
    new ExtractorCommand("extract-jars", "Extract the jars.") {
      protected void action() {
        JarExtractor.extract(resolver);
      }
    }.setProperties(INPUT_REPO, OUTPUT_REPO, JARS_DIR, JAR_INDEX_FILE, 
            JAR_FILTER, EXTRACT_LATEST_MAVEN, EXTRACT_BINARY, RESOLVE_MISSING_TYPES, SKIP_MISSING_TYPES, FORCE_MISSING_REDO,
            IMPORT_FILE, PROBLEM_FILE, ENTITY_FILE, LOCAL_VARIABLE_FILE, RELATION_FILE, COMMENT_FILE, FILE_FILE, USED_JAR_FILE, MISSING_TYPE_FILE);
  
  public static final Command EXTRACT_PROJECTS = 
    new ExtractorCommand("extract-projects", "Extract the projects.") {
      protected void action() {
        ProjectExtractor.extract(resolver);
      }
    }.setProperties(INPUT_REPO, OUTPUT_REPO, PROJECT_FILTER,
            USE_PROJECT_JARS, RESOLVE_MISSING_TYPES, SKIP_MISSING_TYPES, FORCE_MISSING_REDO,
            IMPORT_FILE, PROBLEM_FILE, ENTITY_FILE, LOCAL_VARIABLE_FILE, RELATION_FILE, COMMENT_FILE, FILE_FILE, USED_JAR_FILE, MISSING_TYPE_FILE);
  		  
  @Override
  public Object start(IApplicationContext context) throws Exception {
    String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
    PropertyManager.executeCommand(args, Extractor.class);
    return EXIT_OK;
  }

  @Override
  public void stop() {}
}
