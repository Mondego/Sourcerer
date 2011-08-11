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
package edu.uci.ics.sourcerer.tools.java.repo.model.internal;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;

import edu.uci.ics.sourcerer.repo.general.RepoFile;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class JarIndex extends AbstractCollection<JarFileImpl> {
  public static final Argument<String> JAR_INDEX = new StringArgument("jar-index", "index.txt", "Jar index file.");
  
  private RepoFileImpl root;
  private RepoFileImpl indexFile;
  
  private Map<String, JarFileImpl> index;
  
  private JarIndex(RepoFileImpl dir) {
    root = dir.asRoot();
    indexFile = dir.getChild(JAR_INDEX.getValue());
  }
  
  static JarIndex make(RepoFileImpl dir) {
    return new JarIndex(dir);
  }

  @Override
  public Iterator<JarFileImpl> iterator() {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }
}