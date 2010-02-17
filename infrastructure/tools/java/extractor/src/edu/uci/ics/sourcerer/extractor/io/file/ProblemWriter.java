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
package edu.uci.ics.sourcerer.extractor.io.file;

import java.io.File;

import edu.uci.ics.sourcerer.extractor.io.IProblemWriter;
import edu.uci.ics.sourcerer.model.Problem;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.extracted.Extracted;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ProblemWriter extends ExtractorWriter implements IProblemWriter {
  public static final Property<String> PROBLEM_FILE = new StringProperty("problem-file", "problems.txt", "Extractor Output", "Filename for extracted problems.");
  
  public ProblemWriter(File output, IFileSet input) {
    super(new File(output, Extracted.PROBLEM_FILE.getValue()), input);
  }

  @Override
  public void writeProblem(String filename, boolean isError, int errorCode, String message) {
    write(ProblemEX.getLine(isError ? Problem.ERROR : Problem.WARNING, errorCode, message, convertToRelativePath(filename)));
  }
}
