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

import java.io.File;
import java.io.IOException;

import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FindBugsRunner {
  public static Argument<File> FINDBUGS_JAR = new FileArgument("findbugs-jar", null, "Location of the findbugs jar file");
  public static Argument<String> FINDBUGS_FILE_NAME = new StringArgument("findbugs-file-name", "findbugs.xml", "Name of the FindBugs xml file.").permit();
  
  public static void runFindBugs(File file, File dir) {
    if (FINDBUGS_JAR.getValue() != null) {
      TaskProgressLogger task = TaskProgressLogger.get();
      task.start("Running FindBugs");
      File output = new File(dir, FINDBUGS_FILE_NAME.getValue());
      try {
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", FINDBUGS_JAR.getValue().getPath(), "-textui", "-xml", "-output", output.getPath(), file.getPath());
        builder.inheritIO();
        builder.directory(dir);
        Process process = builder.start();
        process.waitFor();
        task.finish();
      } catch (IOException | InterruptedException e) {
        task.exception(e);
      }
    }
  }
}
