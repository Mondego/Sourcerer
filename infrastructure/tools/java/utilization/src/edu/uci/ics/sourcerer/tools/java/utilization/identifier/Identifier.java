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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import edu.uci.ics.sourcerer.tools.java.utilization.model.FqnFragment;
import edu.uci.ics.sourcerer.tools.java.utilization.model.JarCollection;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;



/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Identifier {
  private Identifier() {
  }
  
  public static LibraryCollection identifyLibraries(TaskProgressLogger task, JarCollection jars) {
    task.start("Identifying libraries in " + jars.size() + " jar files using tree clustering method");
    task.report("Compatibility threshold set to " + Library.COMPATIBILITY_THRESHOLD.getValue());
    
    Multimap<FqnFragment, Library> tempLibMap = ArrayListMultimap.create();
    
    task.start("Identification Stage One: performing post-order traversal of FQN suffix tree", "FQN fragments visited", 10000);
    int libCount = 0;
    // Explore the tree in post-order
    for (FqnFragment fragment : jars.getRoot().getPostOrderIterable()) {
      task.progress("%d FQN fragments visited (" + libCount + " libraries) in %s");
      // If there are no children, then make it its own single-fqn library
      if (!fragment.hasChildren()) {
        Library library = new Library();
        // Add the fqn
        library.addFqn(fragment);
        // Store it in the map for processing with the parent
        tempLibMap.put(fragment, library);
        libCount++;
      } else {
        // Start merging children
        for (FqnFragment child : fragment.getChildren()) {
          for (Library childLib : tempLibMap.get(child)) {
            LinkedList<Library> candidates = new LinkedList<>();
            
            // Check to see if it can be merged with any of the libraries
            for (Library merge : tempLibMap.get(fragment)) {
              if (merge.isCompatible(childLib)) {
                candidates.add(merge);
              }
            }
            if (candidates.size() == 0) {
              // If nothing was found, promote the library
              tempLibMap.put(fragment, childLib);
            } else if (candidates.size() == 1) {
              // If one was found, merge in the child
              Library candidate = candidates.getFirst();
              for (FqnFragment fqn : childLib.getFqns()) {
                candidate.addFqn(fqn);
              }
              libCount--;
            } else {
              // If more than one was found, promote the library
              tempLibMap.put(fragment, childLib);
            }
          }
          // Clear the entry for this child fragment
          tempLibMap.removeAll(child);
        }
      }
    }
    task.finish();
    
    task.report("Stage One identified " + libCount + " libraries");
    
    task.start("Identification Stage Two: merging similar libraries");
    // Second stage
    TreeSet<Library> sortedLibs = new TreeSet<>(new Comparator<Library>() {
      @Override
      public int compare(Library o1, Library o2) {
        int cmp = Integer.compare(o1.getJars().size(), o2.getJars().size());
        if (cmp == 0) {
          return Integer.compare(o1.hashCode(), o2.hashCode());
        } else {
          return cmp;
        }
      }
    });
    
    // Sort all the libraries by the number of jars they contain
    for (Library lib : tempLibMap.get(jars.getRoot())) {
      sortedLibs.add(lib);
    }
    tempLibMap.clear();
    
    Collection<Library> processedLibs = new ArrayList<>();
    // Go from libraries containing the most jars to the least
    while (!sortedLibs.isEmpty()) {
      Library biggest = sortedLibs.pollLast();
      
      // Find and merge any candidate libraries
      for (Iterator<Library> processedIter = processedLibs.iterator(); processedIter.hasNext();) {
        Library processed = processedIter.next();
        // Check if they should merge
        if (biggest.isSecondStageCompatible(task, processed)) {
          for (FqnFragment fqn : processed.getFqns()) {
            biggest.addSecondaryFqn(fqn);
          }
          processedIter.remove();
        }
      }
      
      processedLibs.add(biggest);
    }
    task.finish();
   
    LibraryCollection libraries = new LibraryCollection();
    for (Library lib : processedLibs) {
      libraries.addLibrary(lib);
    }
    
    task.report("Stage Two reduced the library count to " + libraries.getLibraries().size());
    
    task.finish();
    
    return libraries;
  }
}
