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

import edu.uci.ics.sourcerer.tools.java.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.LocalVariableWriter;
import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LocalVariableWriterImpl extends AbstractExtractorWriter<LocalVariableEX> implements LocalVariableWriter {
  public LocalVariableWriterImpl(File output) {
    super(new File(output, LocalVariableEX.LOCAL_VARIABLE_FILE.getValue()), LocalVariableEX.class);
  }

  @Override
  public void writeLocalVariable(LocalVariableEX var) {
    write(var);
  }

  private LocalVariableEX trans = new LocalVariableEX();
  @Override
  public void writeLocalVariable(LocalVariable type, String name, int modifiers, String typeFqn, Location typeLocation, String parent, Integer position, Location location) {
    write(trans.update(type, name, modifiers, typeFqn, typeLocation, parent, position, location));
  }
}
