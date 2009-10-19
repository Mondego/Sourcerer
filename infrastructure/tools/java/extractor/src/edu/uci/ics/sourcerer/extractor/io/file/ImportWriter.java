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

import edu.uci.ics.sourcerer.extractor.io.IImportWriter;
import edu.uci.ics.sourcerer.extractor.io.Location;
import edu.uci.ics.sourcerer.model.extracted.ImportExParser;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ImportWriter extends ExtractorWriter implements IImportWriter {
  public static final Property<String> IMPORT_FILE = new StringProperty("import-file", "imports.txt", "Extractor Output", "Filename for extracted imports.");
  
  public ImportWriter(Repository input) {
    super(input, IMPORT_FILE);
  }
  
  public void writeImport(String name, boolean isStatic, boolean onDemand, Location location) {
    write(ImportExParser.getLine(name, isStatic, onDemand, convertToRelativePath(location.getCompilationUnitPath()), location.getStartPosition(), location.getLength()));
  }
}
