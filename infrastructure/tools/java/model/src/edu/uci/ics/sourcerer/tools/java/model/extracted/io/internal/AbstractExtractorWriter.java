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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ExtractorWriter;
import edu.uci.ics.sourcerer.util.io.EntryWriter;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractExtractorWriter<T extends SimpleSerializable> implements ExtractorWriter {
  private SimpleSerializer serializer;
  private EntryWriter<T> writer;
  
  protected AbstractExtractorWriter(File output, Class<T> klass) {
    try {
      serializer = IOUtils.makeSimpleSerializer(output);
      writer = serializer.getEntryWriter(klass);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to make serializer for " + output.getPath(), e);
      writer = null;
      serializer = null;
    }
  }

  public final void close() {
    IOUtils.close(writer, serializer);
    writer = null;
    serializer = null;
  }
  
  protected void write(T item) {
    try {
      if (writer != null) {
        writer.write(item);
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to serialize item.", e);
      close();
    }
  }
}
