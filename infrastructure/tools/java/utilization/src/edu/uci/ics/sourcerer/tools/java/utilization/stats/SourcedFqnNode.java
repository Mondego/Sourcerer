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
package edu.uci.ics.sourcerer.tools.java.utilization.stats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

import com.google.common.collect.EnumMultiset;
import com.google.common.collect.Multiset;

import edu.uci.ics.sourcerer.tools.java.utilization.model.fqn.AbstractFqnNode;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SourcedFqnNode extends AbstractFqnNode<SourcedFqnNode> {
  public static enum Source {
    PROJECT,
    MAVEN,
    MISSING;
  }
  
  private Multiset<Source> sources;
  
  protected SourcedFqnNode(String name, SourcedFqnNode parent) {
    super(name, parent);
    sources = EnumMultiset.create(Source.class);
  }
  
  static SourcedFqnNode createRoot() {
    return new SourcedFqnNode(null, null);
  }
  
  @Override
  protected SourcedFqnNode create(String name, AbstractFqnNode<?> parent) {
    return new SourcedFqnNode(name, (SourcedFqnNode) parent);
  }
  
  void addSource(Source source) {
    sources.add(source);
  }
  
  public Set<Source> getSources() {
    return sources.elementSet();
  }
  
  public int getCount(Source source) {
    return sources.count(source);
  }
  
  protected Saver createSaver() {
    return new Saver() {
      @Override
      protected void save(BufferedWriter writer, SourcedFqnNode node) throws IOException {
        for (Source source : Source.values()) {
          writer.write(" ");
          writer.write(Integer.toString(node.sources.count(source)));
        }
      }
    };
  }
  
  protected Loader createLoader() {
    return new Loader() {
      @Override
      protected void load(Scanner scanner, SourcedFqnNode node) {
        for (Source source : Source.values()) {
          node.sources.setCount(source, scanner.nextInt());
        }
      }
    };
  }
}
