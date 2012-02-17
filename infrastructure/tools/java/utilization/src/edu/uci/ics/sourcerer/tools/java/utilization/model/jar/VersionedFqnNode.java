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
package edu.uci.ics.sourcerer.tools.java.utilization.model.jar;

import java.util.Iterator;
import java.util.NoSuchElementException;

import edu.uci.ics.sourcerer.tools.java.utilization.model.fqn.AbstractFqnNode;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class VersionedFqnNode extends AbstractFqnNode<VersionedFqnNode> {
  private VersionMap versions;
  
  private VersionedFqnNode(String name, VersionedFqnNode parent) {
    super(name, parent);
    this.versions = VersionMap.make();
  }
  
  @Override
  protected VersionedFqnNode create(String name, AbstractFqnNode<?> parent) {
    return new VersionedFqnNode(name, (VersionedFqnNode) parent);
  }
  
  public static VersionedFqnNode createRoot() {
    return new VersionedFqnNode(null, null);
  }
  
  void addJar(Jar jar, Fingerprint fingerprint) {
    versions.add(fingerprint, jar);
  }
  
  public VersionMap getVersions() {
    return versions;
  }
//  
//  public Iterable<VersionedFqnNode> getPackageIterable() {
//    // We want all the nodes that are the direct parent of a node with a version 
//    return new Iterable<VersionedFqnNode>() {
//      @Override
//      public Iterator<VersionedFqnNode> iterator() {
//        return new Iterator<VersionedFqnNode>() {
//          VersionedFqnNode node = null;
//          {
//            // We need to find the first package
//            // Start at the first leaf
//            for (node = VersionedFqnNode.this; node.firstChild != null; node = node.firstChild);
//            // Find the lowest node that has children
//            // See if a sibling has children
//            for (; node.sibling != null; node = node.sibling) {
//              // If there are children, go to the first leaf
//              for (; node.firstChild != null; node = node.firstChild);
//            }
//            // Now we're at the final child of the lowest node with children
//            // So its parent is our first package
//            node = node.parent;
//          }
//          
//          @Override
//          public boolean hasNext() {
//            return node != null;
//          }
//
//          @Override
//          public VersionedFqnNode next() {
//            if (node == null) {
//              throw new NoSuchElementException();
//            } else {
//              VersionedFqnNode next = node;
//              // We're currently at a package
//              // Does it have a sibling?
//              if (node.sibling != null) {
//                
//              } else {
//                // This package doesn't have a sibling, so we need to walk up the tree
//                
//              }
//              // See if any siblings are packages
//              for (; node.sibling != null; node = node.sibling) {
//                for (; node.firstChild != null; node = node.firstChild);
//              }
//              // If we haven't gone anywhere
//              if (next == node)
//            }
//            return null;
//          }
//
//          @Override
//          public void remove() {
//            throw new UnsupportedOperationException();
//          }
//        };
//      }
//    };
//  }
}
