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
package edu.uci.ics.sourcerer.extractor.io.dummy;

import edu.uci.ics.sourcerer.extractor.io.ILocalVariableWriter;
import edu.uci.ics.sourcerer.model.Location;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class DummyLocalVariableWriter implements ILocalVariableWriter {
  @Override
  public void close() {}

  @Override
  public void writeClassParameter(String name, String type, String parent, int position, String path) {}

  @Override
  public void writeLocalVariable(String name, int modifiers, String type, int typeStartPos, int typeLength, String parent, Location location) {}

  @Override
  public void writeParameter(String name, int modifiers, String type, int typeStartPos, int typeLength, String parent, int position, Location location) {}
}