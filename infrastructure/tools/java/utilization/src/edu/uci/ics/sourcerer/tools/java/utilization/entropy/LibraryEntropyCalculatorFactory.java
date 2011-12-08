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
package edu.uci.ics.sourcerer.tools.java.utilization.entropy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.utilization.identifier.Library;
import edu.uci.ics.sourcerer.tools.java.utilization.model.FqnFragment;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LibraryEntropyCalculatorFactory {
  private static LibraryEntopyCalculator calculator = null;

  public static LibraryEntopyCalculator createCalculator() {
    if (calculator == null) {
      calculator = new LibraryEntopyCalculator() {
        class PackageFragmentEntropy {
          PackageFragmentEntropy parent;
          PackageFragmentEntropy sibling;
          PackageFragmentEntropy firstChild;
          FqnFragment fragment;
          int level;
          int fqnCount;
          int childCount;
          double entropy;

          PackageFragmentEntropy(FqnFragment fragment) {
            this.parent = null;
            this.fragment = fragment;
            this.level = 0;
            this.fqnCount = 0;
            this.childCount = 0;
            this.entropy = 0;
          }

          PackageFragmentEntropy(PackageFragmentEntropy parent, FqnFragment fragment) {
            this.parent = parent;
            this.fragment = fragment;
            this.level = parent.level + 1;
            this.fqnCount = 0;
            this.childCount = 0;
            this.entropy = 0;
          }

          PackageFragmentEntropy addChild(FqnFragment fragment) {
            PackageFragmentEntropy previousChild = null;
            for (PackageFragmentEntropy child = firstChild; child != null; child = child.sibling) {
              int cmp = child.fragment.getName().compareTo(fragment.getName());
              if (cmp == 0) {
                return child;
              } else if (cmp > 0) {
                PackageFragmentEntropy newChild = new PackageFragmentEntropy(this, fragment);
                childCount++;
                newChild.sibling = child;
                if (previousChild == null) {
                  firstChild = newChild;
                } else {
                  previousChild.sibling = newChild;
                }
                return newChild;
              }
              previousChild = child;
            }
            PackageFragmentEntropy newChild = new PackageFragmentEntropy(this, fragment);
            childCount++;
            if (previousChild == null) {
              firstChild = newChild;
            } else {
              previousChild.sibling = newChild;
            }
            return newChild;
          }

          void addFqn() {
            fqnCount++;
            if (parent != null) {
              parent.addFqn();
            }
          }

          Iterable<PackageFragmentEntropy> getPostOrderIterable() {
            return new Iterable<PackageFragmentEntropy>() {
              @Override
              public Iterator<PackageFragmentEntropy> iterator() {
                return new Iterator<PackageFragmentEntropy>() {
                  PackageFragmentEntropy node = null;
                  {
                    for (node = PackageFragmentEntropy.this; node.firstChild != null; node = node.firstChild)
                      ;
                  }

                  @Override
                  public boolean hasNext() {
                    return node != null;
                  }

                  @Override
                  public PackageFragmentEntropy next() {
                    PackageFragmentEntropy next = node;
                    if (node == PackageFragmentEntropy.this) {
                      node = null;
                    } else if (node.sibling != null) {
                      for (node = node.sibling; node.firstChild != null; node = node.firstChild)
                        ;
                    } else {
                      node = node.parent;
                    }
                    return next;
                  }

                  @Override
                  public void remove() {
                    throw new UnsupportedOperationException();
                  }
                };
              }
            };
          }
          
          @Override
          public String toString() {
            return fragment.getFqn();
          }
        }

        PackageFragmentEntropy root;
        Map<FqnFragment, PackageFragmentEntropy> entropies = new HashMap<>();

        private PackageFragmentEntropy getFragmentEntropy(FqnFragment fragment) {
          PackageFragmentEntropy entropy = entropies.get(fragment);
          if (entropy == null) {
            if (fragment.getParent() == null) {
              entropy = new PackageFragmentEntropy(fragment);
              root = entropy;
            } else {
              entropy = getFragmentEntropy(fragment.getParent()).addChild(fragment);
            }
            entropies.put(fragment, entropy);
          }
          return entropy;
        }

        @Override
        public double compute(Library ... libraries) {
          root = null;
          entropies.clear();

          // Build up the library-specific tree so we can compute the entropy
          for (Library library : libraries) {
            for (FqnFragment fqn : library.getFqns()) {
              // Look for the fragments parent
              // Add to the FQN count for it
              getFragmentEntropy(fqn.getParent()).addFqn();
            }
          }

          if (root == null) {
            return 0;
          } else {
            for (PackageFragmentEntropy fragment : root.getPostOrderIterable()) {
              // A fragment with no children has entropy 0, the default
              // A fragment with children has entropy equal to half the sum of
              // the entropies of its children
              // plus the entropy of the node itself (computed by taking the
              // fractions of fqns coming from each child)
              if (fragment.firstChild != null) {
                double fqnCount = fragment.fqnCount;
                double logBase = fragment.childCount + 1;
                int remaining = fragment.fqnCount;
                for (PackageFragmentEntropy child = fragment.firstChild; child != null; child = child.sibling) {
                  // Fraction of the FQNs in this child
                  fragment.entropy += (double) child.entropy / fragment.childCount / 2;
                  remaining -= child.fqnCount;
                  double ent = child.fqnCount / fqnCount;
                  // Entropy is -frac * log frac
                  // The base of the log is the total number of children
                  //   which ensures the entropy scales to 1
                  ent = -ent * Math.log(ent) / Math.log(logBase) / 2;
                  fragment.entropy += ent;
                }
                if (remaining > 0) {
                  double ent = remaining / fqnCount;
                  // Entropy is -frac * log frac
                  // The base of the log is the total number of children
                  //   which ensures the entropy scales to 1
                  ent = -ent * Math.log(ent) / Math.log(logBase) / 2;
                  fragment.entropy += ent;
                }
              }
            }
            return root.entropy;
          }
        }
      };
    }
    return calculator;
  }
}
