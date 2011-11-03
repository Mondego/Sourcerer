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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import edu.uci.ics.sourcerer.tools.java.utilization.model.FqnFragment;
import edu.uci.ics.sourcerer.tools.java.utilization.model.Jar;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarEntropyCalculatorFactory {
  public static JarEntopyCalculator makeCalculator() {
    return new JarEntopyCalculator() {
      class PackageFragmentEntropy {
        PackageFragmentEntropy parent;
        Collection<PackageFragmentEntropy> children;
        FqnFragment fragment;
        int level;
        int fqnCount;
        double entropy;
        
        PackageFragmentEntropy(FqnFragment fragment) {
          this.parent = null;
          this.fragment = fragment;
          this.level = 0;
          this.fqnCount = 0;
          this.entropy = 0;
        }
        
        PackageFragmentEntropy(PackageFragmentEntropy parent, FqnFragment fragment) {
          this.parent = parent;
          if (parent.children == null) {
            parent.children = new ArrayList<>();
          }
          parent.children.add(this);
          this.fragment = fragment;
          this.level = parent.level + 1;
          this.fqnCount = 0;
          this.entropy = 0;
        }
        
        void addFqn() {
          fqnCount++;
          if (parent != null) {
            parent.addFqn();
          }
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
            entropy = new PackageFragmentEntropy(getFragmentEntropy(fragment.getParent()), fragment);
          }
          entropies.put(fragment, entropy);
        }
        return entropy;
      }
      
      @Override
      public double compute(Jar jar) {
        root = null;
        entropies.clear();
        
        // Build up the jar-specific tree so we can compute the entropy
        for (FqnFragment fqn : jar.getFqns()) {
          // Look for the fragments parent
          // Add to the FQN count for it
          getFragmentEntropy(fqn.getParent()).addFqn();
        }
        
        Deque<PackageFragmentEntropy> reverseOrder = new LinkedList<>();
        Deque<PackageFragmentEntropy> queue = new LinkedList<>();
        
        if (root == null) {
          return 0;
        } else {
          queue.offer(root);
          while (!queue.isEmpty()) {
            PackageFragmentEntropy entropy = queue.poll();
            reverseOrder.push(entropy);
            if (entropy.children != null) {
              for (PackageFragmentEntropy child : entropy.children) {
                queue.offer(child);
              }
            }
          }
          
          // Compute the entropy, starting from the root
          while (!reverseOrder.isEmpty()) {
            PackageFragmentEntropy fragment = reverseOrder.pop();
            // A fragment with no children has entropy 0, the default
            // A fragment with children has entropy equal to half the sum of the entropies of its children
            // plus the entropy of the node itself (computed by taking the factions of fqns coming from each child)
            if (fragment.children != null) {
              double fqnCount = fragment.fqnCount;
              int remaining = fragment.fqnCount;
              for (PackageFragmentEntropy child : fragment.children) {
                // Fraction of the FQNs in this child
                fragment.entropy += child.entropy / 2.;
                remaining -= child.fqnCount;
                double ent = child.fqnCount / fqnCount;
                // Entropy is -frac * log frac
                ent = -ent * Math.log(ent);
                fragment.entropy += ent;
              }
              if (remaining > 0) {
                double ent = remaining / fqnCount;
                // Entropy is -frac * log frac
                ent = -ent * Math.log(ent);
                fragment.entropy += ent;
              }
            }
          }
          return root.entropy;
        }
      }
    };
  }
}
