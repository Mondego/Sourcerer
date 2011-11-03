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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
      class FragmentEntropy {
        FragmentEntropy parent;
        FqnFragment fragment;
        int level;
        int fqnCount;
        
        FragmentEntropy(FqnFragment fragment) {
          this.parent = null;
          this.fragment = fragment;
          this.level = 0;
          this.fqnCount = 0;
        }
        
        FragmentEntropy(FragmentEntropy parent, FqnFragment fragment) {
          this.parent = parent;
          this.fragment = fragment;
          this.level = parent.level + 1;
          this.fqnCount = 0;
        }
        
        void addFqn() {
          fqnCount++;
          if (parent != null) {
            parent.addFqn();
          }
        }
      }

      Map<FqnFragment, FragmentEntropy> entropies = new HashMap<>();
      TreeMap<Integer, Collection<FragmentEntropy>> levelMap = new TreeMap<>();
      
      private FragmentEntropy getFragmentEntropy(FqnFragment fragment) {
        FragmentEntropy entropy = entropies.get(fragment);
        if (entropy == null) {
          if (fragment.getParent() == null) {
            entropy = new FragmentEntropy(fragment);
          } else {
            entropy = new FragmentEntropy(getFragmentEntropy(fragment.getParent()), fragment);
          }
          entropies.put(fragment, entropy);
          Collection<FragmentEntropy> level = levelMap.get(entropy.level);
          if (level == null) {
            level = new ArrayList<>();
            levelMap.put(entropy.level, level);
          }
          level.add(entropy);
        }
        return entropy;
      }
      
      @Override
      public double compute(Jar jar) {
        entropies.clear();
        levelMap.clear();
        
        // Build up the jar-specific tree so we can compute the entropy
        for (FqnFragment fqn : jar.getFqns()) {
          // Look for the fragments parent
          // Add to the FQN count for it
          getFragmentEntropy(fqn.getParent()).addFqn();
        }
        
        double fqnCount = jar.getFqns().size();
        double entropy = 0;
        // Compute the entropy, starting from the root
        for (Map.Entry<Integer, Collection<FragmentEntropy>> entry : levelMap.entrySet()) {
          for (FragmentEntropy fragmentEntropy : entry.getValue()) {
            // Fraction of the FQNs in this subtree
            double ent = fragmentEntropy.fqnCount / fqnCount; 
            // Entropy is -frac * log frac
            ent = -ent * Math.log(ent);
            // Scale by geometric series
            ent /= Math.pow(2, fragmentEntropy.level);
            entropy += ent;
          }
        }
        
        return entropy;
      }
    };
  }
}
