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
package edu.uci.ics.sourcerer.tools.java.component.identifier.stats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.uci.ics.sourcerer.tools.java.component.model.fqn.AbstractFqnNode;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SourcedFqnNode extends AbstractFqnNode<SourcedFqnNode> {
  public static enum Source {
    PROJECT,
    MAVEN,
    IMPORTED,
    EXTERNAL,
    MISSING,
    ;
  }
  
  private Multimap<Source, String> sources;
  
  protected SourcedFqnNode(String name, SourcedFqnNode parent) {
    super(name, parent);
    sources = HashMultimap.create(Source.values().length, 5);
  }
  
  static SourcedFqnNode createRoot() {
    return new SourcedFqnNode(null, null);
  }
  
  @Override
  protected SourcedFqnNode create(String name, AbstractFqnNode<?> parent) {
    return new SourcedFqnNode(name, (SourcedFqnNode) parent);
  }
  
  void addSource(Source source, String sourceID) {
    sources.put(source, sourceID);
  }
  
  public boolean hasSource() {
    return !sources.isEmpty();
  }
  
  public boolean has(Source source) {
    return sources.containsKey(source);
  }
  
  public Set<Source> getSources() {
    return sources.keySet();
  }
  
  public Collection<String> getSourceIDs(Source source) {
    return sources.get(source);
  }
  
  public int getCount(Source source) {
    return sources.get(source).size();
  }
  
  public static Comparator<SourcedFqnNode> createComparator(final Source source) {
    return new Comparator<SourcedFqnNode>() {
      @Override
      public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
        int cmp = Integer.compare(o1.getCount(Source.EXTERNAL), o2.getCount(Source.EXTERNAL));
        if (cmp == 0) {
          return o1.compareTo(o2);
        } else {
          return cmp;
        }
      }
    };
  }
  
  protected Saver createSaver() {
    return new Saver() {
      @Override
      protected void save(BufferedWriter writer, SourcedFqnNode node) throws IOException {
        for (Source source : Source.values()) {
          writer.write(" ");
          Collection<String> projects = node.sources.get(source);
          writer.write(Integer.toString(projects.size()));
          for (String project : projects) {
            writer.write(" ");
            writer.write(project);
          }
        }
      }
    };
  }
  
  protected Loader createLoader() {
    return new Loader() {
      @Override
      protected void load(Scanner scanner, SourcedFqnNode node) {
        for (Source source : Source.values()) {
          for (int i = scanner.nextInt(); i > 0; i--) {
            node.sources.put(source, scanner.next());
          }
        }
      }
    };
  }
}
