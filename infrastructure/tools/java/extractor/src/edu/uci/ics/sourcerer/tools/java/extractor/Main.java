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
package edu.uci.ics.sourcerer.tools.java.extractor;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import edu.uci.ics.sourcerer.tools.java.extractor.Extractor.ExtractionMethod;
import edu.uci.ics.sourcerer.tools.java.extractor.Extractor.JarType;
import edu.uci.ics.sourcerer.tools.java.extractor.eclipse.EclipseUtils;
import edu.uci.ics.sourcerer.tools.java.extractor.misc.ExtractedRepositoryAnalyzer;
import edu.uci.ics.sourcerer.tools.java.extractor.misc.UtilizationFilter;
import edu.uci.ics.sourcerer.tools.java.extractor.missing.MissingTypeIdentifier;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.FindBugsRunner;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.WriterBundle;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.internal.CommentWriterImpl;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.internal.EntityWriterImpl;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.internal.FileWriterImpl;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.internal.ImportWriterImpl;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.internal.LocalVariableWriterImpl;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.internal.MissingTypeWriterImpl;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.internal.ProblemWriterImpl;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.internal.RelationWriterImpl;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.internal.UsedJarWriterImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.io.arguments.Command;
import edu.uci.ics.sourcerer.util.io.logging.Logging;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main implements IApplication {
  private static abstract class ExtractorCommand extends Command {
    public ExtractorCommand(String name, String description) {
      super(name, description);
    }
    
    @Override
    protected void execute() {
      Logging.initializeLogger(this);
      
      WriterBundle.IMPORT_WRITER.setValue(ImportWriterImpl.class);
      WriterBundle.PROBLEM_WRITER.setValue(ProblemWriterImpl.class);
      WriterBundle.ENTITY_WRITER.setValue(EntityWriterImpl.class);
      WriterBundle.LOCAL_VARIABLE_WRITER.setValue(LocalVariableWriterImpl.class);
      WriterBundle.RELATION_WRITER.setValue(RelationWriterImpl.class);
      WriterBundle.COMMENT_WRITER.setValue(CommentWriterImpl.class);
      WriterBundle.FILE_WRITER.setValue(FileWriterImpl.class);
      WriterBundle.USED_JAR_WRITER.setValue(UsedJarWriterImpl.class);
      WriterBundle.MISSING_TYPE_WRITER.setValue(MissingTypeWriterImpl.class);
      
      action();
    }
  }
  
  public static final Command ADD_LIBRARIES_TO_REPO =
    new Command("add-libraries-to-repo", "Add the libraries to the repository.") {
      @Override
      protected void action() {
        EclipseUtils.addLibraryJarsToRepository();
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO);
  
  public static final Command EXTRACT_LIBRARIES_ECLIPSE =
    new ExtractorCommand("extract-libraries-eclipse", "Extract the libraries using Eclipse.") {
      protected void action() {
        Extractor.extractJars(JarType.LIBRARY, ExtractionMethod.ECLIPSE);
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT);
    
  public static final Command EXTRACT_LIBRARIES_ASM =
    new ExtractorCommand("extract-libraries-asm", "Extract the libraries using Asm.") {
      protected void action() {
        Extractor.extractJars(JarType.LIBRARY, ExtractionMethod.ASM);
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT, FindBugsRunner.FINDBUGS_JAR);
    
  public static final Command EXTRACT_LIBRARIES =
    new ExtractorCommand("extract-libraries", "Extract the libraries using Eclipse and Asm.") {
      protected void action() {
        Extractor.extractJars(JarType.LIBRARY, ExtractionMethod.ASM_ECLIPSE);
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT);
  
  public static final Command EXTRACT_PROJECT_JARS_ECLIPSE =
    new ExtractorCommand("extract-project-jars-eclipse", "Extract the jars using Eclipse.") {
      protected void action() {
        Extractor.extractJars(JarType.PROJECT, ExtractionMethod.ECLIPSE);
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT);
    
  public static final Command EXTRACT_PROJECT_JARS_ASM =
    new ExtractorCommand("extract-project-jars-asm", "Extract the jars using Asm.") {
      protected void action() {
        Extractor.extractJars(JarType.PROJECT, ExtractionMethod.ASM);
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT, FindBugsRunner.FINDBUGS_JAR);
  
  public static final Command EXTRACT_PROJECT_JARS =
    new ExtractorCommand("extract-project-jars", "Extract the jars using Eclipse and Asm.") {
      protected void action() {
        Extractor.extractJars(JarType.PROJECT, ExtractionMethod.ASM_ECLIPSE);
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT);
    
  public static final Command EXTRACT_MAVEN_JARS_ECLIPSE =
    new ExtractorCommand("extract-maven-jars-eclipse", "Extract the jars using Eclipse.") {
      protected void action() {
        Extractor.extractJars(JarType.MAVEN, ExtractionMethod.ECLIPSE);
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT);
    
  public static final Command EXTRACT_MAVEN_JARS_ASM =
    new ExtractorCommand("extract-maven-jars-asm", "Extract the jars using Asm.") {
      protected void action() {
        Extractor.extractJars(JarType.MAVEN, ExtractionMethod.ASM);
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT, FindBugsRunner.FINDBUGS_JAR);
  
  public static final Command EXTRACT_MAVEN_JARS =
    new ExtractorCommand("extract-maven-jars", "Extract the jars using Eclipse and Asm.") {
      protected void action() {
        Extractor.extractJars(JarType.MAVEN, ExtractionMethod.ASM_ECLIPSE);
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT);
  
  public static final Command EXTRACT_FILTER_JARS_ASM =
    new ExtractorCommand("extract-filter-jars-asm", "Extract the jars using Asm.") {
      protected void action() {
        Extractor.extractJars(JarType.FILTER, ExtractionMethod.ASM);
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT, Extractor.JAR_FILTER.asInput(), FindBugsRunner.FINDBUGS_JAR);
    
  public static final Command EXTRACT_FILTER_JARS =
    new ExtractorCommand("extract-filter-jars", "Extract the jars using Eclipse and Asm.") {
      protected void action() {
        Extractor.extractJars(JarType.FILTER, ExtractionMethod.ASM_ECLIPSE);
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT, Extractor.JAR_FILTER.asInput());
    
  public static final Command EXTRACT_PROJECTS = 
    new ExtractorCommand("extract-projects", "Extract the projects.") {
      protected void action() {
        Extractor.extractProjects();
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT, Extractor.INCLUDE_PROJECT_JARS, Extractor.RESOLVE_MISSING_TYPES);
    
  public static final Command IDENTIFY_EXTERNAL_TYPES =
    new Command("identify-external-types", "Identified the external types") {
      protected void action() {
        WriterBundle.MISSING_TYPE_WRITER.setValue(MissingTypeWriterImpl.class);
        WriterBundle.IMPORT_WRITER.setValue(ImportWriterImpl.class);
        MissingTypeIdentifier.identifyExternalTypes();
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT);
    
  public static final Command IDENTIFY_MISSING_TYPES =
    new Command("identify-missing-types", "Identified the missing types") {
      protected void action() {
        WriterBundle.MISSING_TYPE_WRITER.setValue(MissingTypeWriterImpl.class);
        WriterBundle.IMPORT_WRITER.setValue(ImportWriterImpl.class);
        MissingTypeIdentifier.identifyMissingTypes();
      }
    }.setProperties(JavaRepositoryFactory.INPUT_REPO, JavaRepositoryFactory.OUTPUT_REPO, Extractor.FORCE_REDO, Extractor.COMPRESS_OUTPUT);       
  		  
  public static final Command CREATE_JAR_FILTER =
      new Command("create-jar-filter", "Create jar filter") {
        protected void action() {
//          ExtractedRepositoryAnalyzer.createUsedJarFilter();
          UtilizationFilter.createUtilizationJarFilter();
        }
    }.setProperties(Extractor.JAR_FILTER.asOutput(), DatabaseConnectionFactory.DATABASE_URL, DatabaseConnectionFactory.DATABASE_USER, DatabaseConnectionFactory.DATABASE_PASSWORD);
    
  @Override
  public Object start(IApplicationContext context) throws Exception {
    String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
    Command.execute(args, Main.class);
    return EXIT_OK;
  }

  @Override
  public void stop() {}
  
  public static void main(String[] args) {
    Command.execute(args, Main.class);
  }
}
