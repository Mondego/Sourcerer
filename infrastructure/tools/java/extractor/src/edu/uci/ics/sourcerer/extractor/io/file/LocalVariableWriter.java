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

import edu.uci.ics.sourcerer.extractor.io.ILocalVariableWriter;
import edu.uci.ics.sourcerer.extractor.io.Location;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableExJarParser;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableExParser;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LocalVariableWriter extends ExtractorWriter implements ILocalVariableWriter {
  public static final Property<String> LOCAL_VARIABLE_FILE = new StringProperty("local-variable-file", "local-variables.txt", "Extractor Output", "Filename for extracted local variables.");
  
  public LocalVariableWriter(File output, Repository input) {
    super(new File(output, LOCAL_VARIABLE_FILE.getValue()), input);
  }

  @Override
  public void writeLocalVariable(String name, int modifiers, String type, int typeStartPos, int typeLength, String parent, Location location) {
    write(LocalVariableExParser.getLineLocal(name, modifiers, type, typeStartPos, typeLength, parent, convertToRelativePath(location.getCompilationUnitPath()), location.getStartPosition(), location.getLength()));
  }

  @Override
  public void writeJarParameter(String name, String type, String parent, int position) {
    write(LocalVariableExJarParser.getLineParam(name, type, parent, position));
  }
  
  @Override
  public void writeParameter(String name, int modifiers, String type, int typeStartPos, int typeLength, String parent, int position, Location location) {
    write(LocalVariableExParser.getLineParam(name, modifiers, type, typeStartPos, typeLength, position, parent, convertToRelativePath(location.getCompilationUnitPath()), location.getStartPosition(), location.getLength()));
  }
}
