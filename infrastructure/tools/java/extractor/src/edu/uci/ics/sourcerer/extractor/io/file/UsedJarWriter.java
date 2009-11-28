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

import edu.uci.ics.sourcerer.extractor.io.IUsedJarWriter;
import edu.uci.ics.sourcerer.model.extracted.UsedJarExParser;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.Extracted;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class UsedJarWriter extends ExtractorWriter implements IUsedJarWriter {
  public UsedJarWriter(File output, Repository input) {
    super(new File(output, Extracted.USED_JAR_FILE.getValue()), input, true);
  }
  
  public void writeUsedJar(String hash, String ... missingTypes) {
    write(UsedJarExParser.getLine(hash, missingTypes));
  }
}
