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
package edu.uci.ics.sourcerer.tools.java.token.eclipse;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import edu.uci.ics.sourcerer.tools.java.extractor.eclipse.EclipseUtils;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFileSet;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TokenExtractor {
  public static final Argument<File> TOKEN_FILE = new RelativeFileArgument("token-file", "tokens.txt", Arguments.OUTPUT, "Output file contain token dump.");
  
  private int count = 0;
  private TokenExtractor() {}
  
  
  public static void extractTokens() {
    new TokenExtractor().internalExtractTokens();
  }
  
  private void internalExtractTokens() {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Extracting tokens from repository: " + JavaRepositoryFactory.INPUT_REPO.getValue());
    
    JavaRepository repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    
    task.start("Loading projects");
    Collection<? extends JavaProject> projects = repo.getProjects();
    task.finish();
    
    try (PrintWriter out = new PrintWriter(TOKEN_FILE.getValue())) {
      task.start("Extracting " + projects.size() + " projects", "projects extracted", 1);
      for (JavaProject project : projects) {
        task.progress("Extracting " + project + " (%d of " + projects.size() + ")");
        
        JavaFileSet files = project.getContent();
        EclipseUtils.initializeProject(Collections.<JarFile>emptyList());
        
        Map<JavaFile, IFile> sourceFiles = EclipseUtils.loadFilesIntoProject(files.getFilteredJavaFiles());
        
        for (Map.Entry<JavaFile, IFile> entry : sourceFiles.entrySet()) {
          printMethodTokens(JavaCore.createCompilationUnitFrom(entry.getValue()), out);
        }
      }
      task.finish();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing token file", e);
    }
    task.finish();
  }
  
  private static boolean isNotNullNotEmptyNotWhiteSpaceOnlyByJava( final String string)  
  {  
         return string != null && !string.isEmpty() && !string.trim().isEmpty();  
  }
  
  private void printMethodTokens(ICompilationUnit unit, PrintWriter out) {
    try {
      IType[] allTypes = unit.getAllTypes();
      for (IType type : allTypes) {
        IMethod[] methods = type.getMethods();
        for (IMethod method : methods) {
  
          //System.out.println("Method name " + method.getElementName());
          String methodBody = method.getSource();
          methodBody = methodBody.replaceAll("\\r\\n|\\r|\\n", " ");
          //String truncatedmethodBody = methodBody.replaceAll(", replacement)
//          System.out.println(methodBody);
          //methodBody.
          if(isNotNullNotEmptyNotWhiteSpaceOnlyByJava(methodBody))
          { 
            out.print(count+":");
            out.print(method.getElementName()+":");
            out.print(methodBody);
            out.println();
            count++;
          } 
          //System.out.println("Signature " + method.getSignature());
          //System.out.println("Return Type " + method.getReturnType());
  
        }
      }
    } catch (JavaModelException e) {
      logger.log(Level.SEVERE, "Error processing file", e);
    }
  }
}
