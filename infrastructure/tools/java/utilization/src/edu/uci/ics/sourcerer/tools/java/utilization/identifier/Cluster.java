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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.utilization.entropy.ClusterEntopyCalculator;
import edu.uci.ics.sourcerer.tools.java.utilization.entropy.ClusterEntropyCalculatorFactory;
import edu.uci.ics.sourcerer.tools.java.utilization.model.FqnFragment;
import edu.uci.ics.sourcerer.tools.java.utilization.model.JarSet;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.EnumArgument;
import edu.uci.ics.sourcerer.util.io.arguments.IntegerArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Cluster {
  public static final Argument<Integer> COMPATIBILITY_THRESHOLD = new IntegerArgument("compatibility-threshold", 100, "Think percent.").permit();
  public static final Argument<MergeMethod> MERGE_METHOD = new EnumArgument<>("merge-method", MergeMethod.class, "Method for performing second stage merge.").makeOptional();
  
  public static enum MergeMethod {
    RELATED_PACKAGE,
    RELATED_SUBPACKAGE,
    ENTROPY;
  }
  
  private JarSet jars;
  private JarSet primaryJars;
  private final Collection<FqnFragment> fqns;
  
  Cluster() {
    this.fqns = new LinkedList<>();
    jars = JarSet.create();
    primaryJars = JarSet.create();
  }
  
  void addPrimaryFqn(FqnFragment fqn) {
    fqns.add(fqn);
    primaryJars = primaryJars.merge(fqn.getVersions().getJars());
    jars = jars.merge(fqn.getVersions().getJars());
  }
  
  void addSecondaryFqn(FqnFragment fqn) {
    fqns.add(fqn);
    jars = jars.merge(fqn.getVersions().getJars());
  }
  
  public Collection<FqnFragment> getFqns() {
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
      
        for (FqnFragment fqn : fqns) {
          for (FqnFragment otherFqn : other.fqns) {
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
  
  public boolean isSecondStageCompatible(TaskProgressLogger task, Cluster other) {
    // Is every primary jar from this cluster also in the other cluster?
    if (primaryJars.getIntersectionSize(other.primaryJars) == primaryJars.size()) {
      switch (MERGE_METHOD.getValue()) {
        case RELATED_PACKAGE:
          {
            // Is every package from other also in this?
            Set<FqnFragment> packages = new HashSet<>();
            for (FqnFragment fqn : fqns) {
              packages.add(fqn.getParent());
            }
            for (FqnFragment fqn : other.fqns) {
              if (!packages.contains(fqn.getParent())) {
                return false;
              }
            }
            return true;
          }
        case RELATED_SUBPACKAGE:
          {
            //  Is every package from other either in this, or a subpackage of this?
            Set<FqnFragment> packages = new HashSet<>();
            for (FqnFragment fqn : fqns) {
              packages.add(fqn.getParent());
            }
            for (FqnFragment fqn : other.fqns) {
              boolean found = false;
              while (fqn != null) {
                if (packages.contains(fqn)) {
                  found = true;
                  fqn = null;
                } else {
                  fqn = fqn.getParent();
                }
              }
              if (!found) {
                return false;
              }
            }
            return true;
          } 
        case ENTROPY:
          task.start("Considering merging two clusters");
          task.start("Less Popular Cluster");
          for (FqnFragment fqn : fqns) {
            task.report(fqn.getFqn());
          }
          task.finish();
          task.start("More Popular Cluster");
          for (FqnFragment fqn : other.fqns) {
            task.report(fqn.getFqn());
          }
          task.finish();
          ClusterEntopyCalculator calc = ClusterEntropyCalculatorFactory.createCalculator();
          double myEntropy = calc.compute(this);
          double otherEntropy = calc.compute(other);
          double jointEntropy = calc.compute(this, other);
          task.start("Results");
          task.report("Smaller entropy: " + myEntropy);
          task.report("Larger entropy: " + otherEntropy);
          task.report("Joint entropy: " + jointEntropy);
          double minDelta = jointEntropy - Math.max(myEntropy, otherEntropy);
          double maxDelta = jointEntropy - Math.min(myEntropy, otherEntropy);
          task.report("Max Entropy Delta: " + maxDelta);
          task.report("Min Entropy Delta: " + minDelta);
          boolean doMerge = maxDelta <= .2 && minDelta < .1;
          task.report("Merge more into less? " + (doMerge ? "yes" : "no"));
          task.finish();
          task.finish();
          return doMerge; 
        default:
          logger.severe("Invalid merge method: " + MERGE_METHOD.getValue());
          return false;
      }
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return fqns.toString();
  }
}
