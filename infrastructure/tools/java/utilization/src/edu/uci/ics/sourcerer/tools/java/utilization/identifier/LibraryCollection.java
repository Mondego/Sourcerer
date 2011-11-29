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
package edu.uci.ics.sourcerer.tools.java.utilization.identifier;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.utilization.model.FqnFragment;
import edu.uci.ics.sourcerer.tools.java.utilization.model.JarSet;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LibraryCollection implements Iterable<Library> {
  private final Collection<Library> libraries;
  
  LibraryCollection() {
    libraries = new ArrayList<>();
  }
  
  void addLibrary(Library library) {
    libraries.add(library);
  }
  
  public Collection<Library> getLibraries() {
    return libraries;
  }

  @Override
  public Iterator<Library> iterator() {
    return libraries.iterator();
  }

  public int size() {
    return libraries.size();
  }
  
  public void printStatistics(TaskProgressLogger task) {
    task.start("Printing library statistics");
    task.report(libraries.size() + " libraries identified");
    int trivial = 0;
    TreeSet<Library> nonTrivial = new TreeSet<>(new Comparator<Library>() {
      @Override
      public int compare(Library o1, Library o2) {
        int cmp = Integer.compare(o1.getJars().size(), o2.getJars().size());
        if (cmp == 0) {
          return Integer.compare(o1.hashCode(), o2.hashCode());
        } else {
          return cmp;
        }
      }});
    for (Library library : libraries) {
      if (library.getJars().size() > 1) {
        nonTrivial.add(library);
      } else {
        trivial++;
      }
    }
    task.report(trivial + " unique libraries");
    task.report(nonTrivial.size() + " compound libraries");
    task.start("Examining compound libraries");
    while (!nonTrivial.isEmpty()) {
      Library biggest = nonTrivial.pollLast();
      task.start("Listing FQNs for library found in " + biggest.getJars().size() + " jars");
      for (FqnFragment fqn : biggest.getFqns()) {
        task.report(fqn.getFqn());
      }
      task.finish();
    }
    task.finish();
    task.finish();
  }
  
  public void printStatistics(TaskProgressLogger task, String name) {
    NumberFormat format = NumberFormat.getPercentInstance();
    format.setMinimumFractionDigits(2);
    format.setMaximumFractionDigits(2);
    task.start("Printing library statistics");
    try (BufferedWriter bw = IOUtils.makeBufferedWriter(new File(Arguments.OUTPUT.getValue(), name))) {
      bw.write(libraries.size() + " libraries identified");
      bw.newLine();
      
      int trivial = 0;
      TreeSet<Library> nonTrivial = new TreeSet<>(new Comparator<Library>() {
        @Override
        public int compare(Library o1, Library o2) {
          int cmp = Integer.compare(o1.getJars().size(), o2.getJars().size());
          if (cmp == 0) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
          } else {
            return cmp;
          }
        }});
      for (Library library : libraries) {
        if (library.getJars().size() > 1) {
          nonTrivial.add(library);
        } else {
          trivial++;
        }
      }
      bw.write(trivial + " unique libraries");
      bw.newLine();
      bw.write(nonTrivial.size() + " compound libraries");
      bw.newLine();
      while (!nonTrivial.isEmpty()) {
        Library biggest = nonTrivial.pollLast();
        JarSet mainSet = biggest.getJars();
        bw.write("Listing FQNs for library found in " + mainSet.size() + " jars");
        bw.newLine();
        for (FqnFragment fqn : biggest.getFqns()) {
          double percent = (double) fqn.getJars().getIntersectionSize(mainSet) / (double) mainSet.size();
          bw.write("  " + fqn.getFqn() + " " + format.format(percent));
          bw.newLine();
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error printing statistics", e);
    }
    task.finish();
  }
}
