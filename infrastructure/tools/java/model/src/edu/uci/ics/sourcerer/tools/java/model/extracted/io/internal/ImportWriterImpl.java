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
package edu.uci.ics.sourcerer.tools.java.model.extracted.io.internal;

import java.io.File;

import edu.uci.ics.sourcerer.tools.java.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ImportWriter;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ImportWriterImpl extends AbstractExtractorWriter<ImportEX> implements ImportWriter {
  public ImportWriterImpl(File output) {
    super(new File(output, ImportEX.IMPORT_FILE.getValue()), ImportEX.class);
  }
  
  @Override
  public void writeImport(ImportEX imp) {
    write(imp);
  }


  private ImportEX trans = new ImportEX();
  @Override
  public void writeImport(String imported, boolean isStatic, boolean onDemand, Location location) {
    write(trans.update(imported, isStatic, onDemand, location));
  }
}
