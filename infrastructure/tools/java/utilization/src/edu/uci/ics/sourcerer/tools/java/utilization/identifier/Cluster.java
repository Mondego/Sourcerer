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

import java.util.Collection;
import java.util.LinkedList;

import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarSet;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.EnumArgument;
import edu.uci.ics.sourcerer.util.io.arguments.IntegerArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Cluster {
  public static final Argument<Integer> COMPATIBILITY_THRESHOLD = new IntegerArgument("compatibility-threshold", 100, "Think percent.").permit();
  public static final Argument<ClusterMergeMethod> MERGE_METHOD = new EnumArgument<>("merge-method", ClusterMergeMethod.class, "Method for performing second stage merge.").makeOptional();

  private JarSet jars;
  private JarSet primaryJars;
  private final Collection<VersionedFqnNode> fqns;
  
  Cluster() {
    this.fqns = new LinkedList<>();
    jars = JarSet.create();
    primaryJars = JarSet.create();
  }
  
  void addPrimaryFqn(VersionedFqnNode fqn) {
    fqns.add(fqn);
    primaryJars = primaryJars.merge(fqn.getVersions().getJars());
    jars = jars.merge(fqn.getVersions().getJars());
  }
  
  void addSecondaryFqn(VersionedFqnNode fqn) {
    fqns.add(fqn);
    jars = jars.merge(fqn.getVersions().getJars());
  }
  
  public Collection<VersionedFqnNode> getFqns() {
    return fqns;
  }
  
  public JarSet getPrimaryJars() {
    return primaryJars;
  }
  
  public JarSet getJars() {
    return jars;
  }
  
  public boolean isCompatible(Cluster other) {
    // Do a pairwise comparison of every FQN. Calculate the conditional
    // probability of each FQN in B appearing given each FQN in A and average.
    // Then compute the reverse. Both values must be above the threshold.
    double threshold = COMPATIBILITY_THRESHOLD.getValue() / 100.;
    // If the threshold is greater than 1, no match is possible
    if (threshold > 1) {
      return false;
    }
    // If the threshold is 1, we can short-circuit this comparison
    // The primary jars must match exactly (can do == because JarSet is interned)
    else if (threshold == 1.) {
      return primaryJars == other.primaryJars;
    }
    // Now we have to actually do the comparison
    else {
      // If there's no intersection between the JarSet, return false
      // There may be other optimizations that can be done to cut out cases where the full comparison has to be done
      if (jars.getIntersectionSize(other.jars) == 0) {
        return false;
      } else {
        Averager<Double> otherGivenThis = new Averager<>();
        Averager<Double> thisGivenOther = new Averager<>();
      
        for (VersionedFqnNode fqn : fqns) {
          for (VersionedFqnNode otherFqn : other.fqns) {
            JarSet fqnJars = fqn.getVersions().getJars();
            JarSet otherFqnJars = otherFqn.getVersions().getJars();
            // Conditional probability of other given this
            // # shared jars / total jars in this
            otherGivenThis.addValue((double) fqnJars.getIntersectionSize(otherFqnJars) / fqnJars.size());
            // Conditional probabilty for this given other
            // # shared jars / total jars in other
            thisGivenOther.addValue((double) otherFqnJars.getIntersectionSize(fqnJars) / otherFqnJars.size());
          }
        }
        return otherGivenThis.getMean() >= threshold && thisGivenOther.getMean() >= threshold;
      }
    }
  }
  
  public boolean isSecondStageCompatible(Cluster other, LogFileWriter writer) {
    return MERGE_METHOD.getValue().shouldMerge(this, other, writer);
  }
  
  @Override
  public String toString() {
    return fqns.toString();
  }
}