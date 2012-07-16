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

import edu.uci.ics.sourcerer.tools.java.model.extracted.FileEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.FileWriter;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.Metrics;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FileWriterImpl extends AbstractExtractorWriter<FileEX> implements FileWriter {
  public FileWriterImpl(java.io.File output) {
    super(new java.io.File(output, FileEX.FILE_FILE.getValue()), FileEX.class);
  }

  @Override
  public void writeFile(FileEX file) {
    write(file);
  }
  
  private FileEX trans = new FileEX();
  @Override
  public void writeFile(File type, String name, Metrics metrics, String hashPath) {
    write(trans.update(type, name, metrics, hashPath));
  }
}