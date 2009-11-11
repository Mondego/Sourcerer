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

import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.COMMENT_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.ENTITY_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.FILE_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.IMPORT_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.JAR_ENTITY_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.JAR_FILE_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.JAR_RELATION_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.LOCAL_VARIABLE_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.MISSING_TYPE_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.PROBLEM_WRITER;
import static edu.uci.ics.sourcerer.extractor.io.WriterBundle.RELATION_WRITER;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.COMMENT_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.ENTITY_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.FILE_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.IMPORT_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.JAR_FILE_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.LOCAL_VARIABLE_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.MISSING_TYPE_FILE;
import static edu.uci.ics.sourcerer.repo.extracted.Extracted.RELATION_FILE;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.OUTPUT_REPO;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import edu.uci.ics.sourcerer.extractor.io.file.CommentWriter;
import edu.uci.ics.sourcerer.extractor.io.file.EntityWriter;
import edu.uci.ics.sourcerer.extractor.io.file.FileWriter;
import edu.uci.ics.sourcerer.extractor.io.file.ImportWriter;
import edu.uci.ics.sourcerer.extractor.io.file.JarEntityWriter;
import edu.uci.ics.sourcerer.extractor.io.file.JarFileWriter;
import edu.uci.ics.sourcerer.extractor.io.file.JarRelationWriter;
import edu.uci.ics.sourcerer.extractor.io.file.LocalVariableWriter;
import edu.uci.ics.sourcerer.extractor.io.file.MissingTypeWriter;
import edu.uci.ics.sourcerer.extractor.io.file.ProblemWriter;
import edu.uci.ics.sourcerer.extractor.io.file.RelationWriter;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Extractor implements IApplication {
  public static final Property<Boolean> EXTRACT_LIBRARIES = new BooleanProperty("extract-libraries", false, "Extractor", "Extract the libraries.");
  public static final Property<Boolean> EXTRACT_JARS = new BooleanProperty("extract-jars", false, "Extractor", "Extract the jars.");
  public static final Property<Boolean> EXTRACT_PROJECTS = new BooleanProperty("extract-projects", false, "Extractor", "Extract the projects.");

  public static final Property<Boolean> RESOLVE_MISSING_TYPES = new BooleanProperty("resolve-missing-types", false, "Extractor", "Re-attempt extraction on failed missing type extractions.");
  
  @Override
  public Object start(IApplicationContext context) throws Exception {
    String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
   
    PropertyManager.registerAndVerify(EXTRACT_LIBRARIES, EXTRACT_JARS, EXTRACT_PROJECTS, RESOLVE_MISSING_TYPES,
        IMPORT_WRITER, IMPORT_FILE,
        PROBLEM_WRITER, ProblemWriter.PROBLEM_FILE,
        ENTITY_WRITER, JAR_ENTITY_WRITER, ENTITY_FILE,
        LOCAL_VARIABLE_WRITER, LOCAL_VARIABLE_FILE,
        RELATION_WRITER, JAR_RELATION_WRITER, RELATION_FILE,
        COMMENT_WRITER, COMMENT_FILE,
        FILE_WRITER, FILE_FILE,
        JAR_FILE_WRITER, JAR_FILE_FILE,
        MISSING_TYPE_WRITER, MISSING_TYPE_FILE);
    
    IMPORT_WRITER.setValue(ImportWriter.class);
    PROBLEM_WRITER.setValue(ProblemWriter.class);
    ENTITY_WRITER.setValue(EntityWriter.class);
    JAR_ENTITY_WRITER.setValue(JarEntityWriter.class);
    LOCAL_VARIABLE_WRITER.setValue(LocalVariableWriter.class);
    RELATION_WRITER.setValue(RelationWriter.class);
    JAR_RELATION_WRITER.setValue(JarRelationWriter.class);
    COMMENT_WRITER.setValue(CommentWriter.class);
    FILE_WRITER.setValue(FileWriter.class);
    JAR_FILE_WRITER.setValue(JarFileWriter.class);
    MISSING_TYPE_WRITER.setValue(MissingTypeWriter.class);
    
    if (EXTRACT_LIBRARIES.getValue()) {
      PropertyManager.registerAndVerify(OUTPUT_REPO);
      LibraryExtractor.extract();
    } else if (EXTRACT_JARS.getValue()){
      PropertyManager.registerAndVerify(INPUT_REPO, OUTPUT_REPO);
      JarExtractor.extract();
    } else if (EXTRACT_PROJECTS.getValue()) {
      PropertyManager.registerAndVerify(INPUT_REPO, OUTPUT_REPO);
      ProjectExtractor.extract();
    } else {
      PropertyManager.printUsage();
    }

    return EXIT_OK;
  }

  @Override
  public void stop() {}
}
