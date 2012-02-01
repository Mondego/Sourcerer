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

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.utilization.entropy.ClusterEntopyCalculator;
import edu.uci.ics.sourcerer.tools.java.utilization.entropy.ClusterEntropyCalculatorFactory;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.Action;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.DoubleArgument;
import edu.uci.ics.sourcerer.util.io.arguments.IntegerArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public enum ClusterMergeMethod {
  RELATED_SUBPACKAGE {
    @Override
    public void doForEachVersion(Action action) {
      Identifier.MERGE_METHOD.setValue(this);
      for (int threshold = 100, min = MINIMUM_THRESHOLD.getValue(), dec = THRESHOLD_DECREMENT.getValue(); threshold >= min; threshold -= dec) {
        MERGE_THRESHOLD.setValue(threshold / 100.);
        action.doMe();
      }
    }
    
    @Override
    public String toString() {
      NumberFormat format = NumberFormat.getInstance();
      format.setMaximumFractionDigits(2);
      return name() + "-" + format.format(MERGE_THRESHOLD.getValue());
    }
    
    @Override
    public boolean shouldMergeHelper(Cluster core, Cluster other, LogFileWriter writer) {
      // What percentage of packages in other cluster are either in the core cluster
      //   or a subpackage of a package in the core cluster
      Set<VersionedFqnNode> corePackages = new HashSet<>();
      for (VersionedFqnNode fqn : core.getCoreFqns()) {
        corePackages.add(fqn.getParent());
      }
      Set<VersionedFqnNode> otherPackages = new HashSet<>();
      for (VersionedFqnNode fqn : other.getCoreFqns()) {
        otherPackages.add(fqn.getParent());
      }
      int validPackages = 0;
      for (VersionedFqnNode pkg : otherPackages) {
        while (pkg != null) {
          if (corePackages.contains(pkg)) {
            validPackages++;
            pkg = null;
          } else {
            pkg = pkg.getParent();
          }
        }
      }
      
      double validRate = (double) validPackages / otherPackages.size();
      
      writer.writeAndIndent("Results");
      writer.write("Valid rate: " + validRate);
      boolean doMerge = validRate >= MERGE_THRESHOLD.getValue();
      writer.write("Merge? " + (doMerge ? "yes" : " no"));
      writer.unindent();
   
      return doMerge;
    }
  },
  JACCARD_PACKAGE {
    @Override
    public void doForEachVersion(Action action) {
      Identifier.MERGE_METHOD.setValue(this);
      for (int threshold = 100, min = MINIMUM_THRESHOLD.getValue(), dec = THRESHOLD_DECREMENT.getValue(); threshold >= min; threshold -= dec) {
        MERGE_THRESHOLD.setValue(threshold / 100.);
        action.doMe();
      }
    }
    
    @Override
    public String toString() {
      NumberFormat format = NumberFormat.getInstance();
      format.setMaximumFractionDigits(2);
      return name() + "-" + format.format(MERGE_THRESHOLD.getValue());
    }
    
    @Override
    public boolean shouldMergeHelper(Cluster core, Cluster other, LogFileWriter writer) {
      // Size of package intersection over size of package union
      Set<VersionedFqnNode> corePackages = new HashSet<>();
      for (VersionedFqnNode fqn : core.getCoreFqns()) {
        corePackages.add(fqn.getParent());
      }
      Set<VersionedFqnNode> otherPackages = new HashSet<>();
      for (VersionedFqnNode fqn : other.getCoreFqns()) {
        otherPackages.add(fqn.getParent());
      }
      int intersectionSize = 0;
      int unionSize = corePackages.size();
      for (VersionedFqnNode fqn : otherPackages) {
        if (corePackages.contains(fqn)) {
          intersectionSize++;
        } else {
          unionSize++;
        }
      }
      double jaccardIndex = (double) intersectionSize / unionSize;
      
      writer.writeAndIndent("Results");
      writer.write("Jaccard: " + jaccardIndex);
      boolean doMerge = jaccardIndex >= MERGE_THRESHOLD.getValue();
      writer.write("Merge? " + (doMerge ? "yes" : " no"));
      writer.unindent();

      return doMerge;
    }
  },
  // TODO Make an edit-distance version!
  MAX_PATH_SIMILARITY {
    @Override
    public void doForEachVersion(Action action) {
      Identifier.MERGE_METHOD.setValue(this);
      for (int threshold = 100, min = MINIMUM_THRESHOLD.getValue(), dec = THRESHOLD_DECREMENT.getValue(); threshold >= min; threshold -= dec) {
        MERGE_THRESHOLD.setValue(threshold / 100.);
        action.doMe();
      }
    }
    
    @Override
    public String toString() {
      NumberFormat format = NumberFormat.getInstance();
      format.setMaximumFractionDigits(2);
      return name() + "-" + format.format(MERGE_THRESHOLD.getValue());
    }
    
    private void breakFqn(VersionedFqnNode fqn, LinkedList<VersionedFqnNode> fragments) {
      while (fqn.getName() != null) {
        fragments.addFirst(fqn);
        fqn = fqn.getParent();
      }
    }
    
    @Override
    public boolean shouldMergeHelper(Cluster core, Cluster other, LogFileWriter writer) {
      Averager<Double> averager = new Averager<>();
      
      LinkedList<VersionedFqnNode> coreFragments = new LinkedList<>();
      LinkedList<VersionedFqnNode> otherFragments = new LinkedList<>();
      for (VersionedFqnNode otherFqn : other.getCoreFqns()) {
        // Break the package into fragments
        breakFqn(otherFqn.getParent(), otherFragments);
        
        // Find the core FQN in the core cluster that matches best
        double bestMatch = 0;
        for (VersionedFqnNode coreFqn : core.getCoreFqns()) {
          breakFqn(coreFqn.getParent(), coreFragments);
          if (coreFragments.isEmpty()) {
            if (otherFragments.isEmpty()) {
              bestMatch = 1.;
            }
          } else {
            Iterator<VersionedFqnNode> coreIter = coreFragments.iterator();
            Iterator<VersionedFqnNode> otherIter = otherFragments.iterator();
            int overlap = 0;
            while (coreIter.hasNext() && otherIter.hasNext()) {
              if (coreIter.next() == otherIter.next()) {
                overlap++;
              } else {
                break;
              }
            }
            bestMatch = Math.max(bestMatch, (double) overlap / otherFragments.size());
          }
          coreFragments.clear();
        }
        // See if an extra FQN is a better match
        for (VersionedFqnNode extraFqn : core.getExtraFqns()) {
          breakFqn(extraFqn.getParent(), coreFragments);
          if (coreFragments.isEmpty()) {
            if (otherFragments.isEmpty()) {
              bestMatch = 1.;
            }
          } else {
            Iterator<VersionedFqnNode> coreIter = coreFragments.iterator();
            Iterator<VersionedFqnNode> otherIter = otherFragments.iterator();
            int overlap = 0;
            while (coreIter.hasNext() && otherIter.hasNext()) {
              if (coreIter.next() == otherIter.next()) {
                overlap++;
              } else {
                break;
              }
            }
            bestMatch = Math.max(bestMatch, (double) overlap / otherFragments.size());
          }
          coreFragments.clear();
        }
        averager.addValue(bestMatch);
        otherFragments.clear();
      }
      
      writer.writeAndIndent("Results");
      writer.write("Average Max Similarity: " + averager.getMean());
      boolean doMerge = averager.getMean() >= MERGE_THRESHOLD.getValue();
      writer.write("Merge? " + (doMerge ? "yes" : " no"));
      writer.unindent();
      
      return doMerge;
    }
  },
  AVG_PATH_SIMILARITY {
    @Override
    public void doForEachVersion(Action action) {
      Identifier.MERGE_METHOD.setValue(this);
      for (int threshold = 100, min = MINIMUM_THRESHOLD.getValue(), dec = THRESHOLD_DECREMENT.getValue(); threshold >= min; threshold -= dec) {
        MERGE_THRESHOLD.setValue(threshold / 100.);
        action.doMe();
      }
    }
    
    @Override
    public String toString() {
      NumberFormat format = NumberFormat.getInstance();
      format.setMaximumFractionDigits(2);
      return name() + "-" + format.format(MERGE_THRESHOLD.getValue());
    }
    
    private void breakFqn(VersionedFqnNode fqn, LinkedList<VersionedFqnNode> fragments) {
      while (fqn.getName() != null) {
        fragments.addFirst(fqn);
        fqn = fqn.getParent();
      }
    }
    
    @Override
    public boolean shouldMergeHelper(Cluster core, Cluster other, LogFileWriter writer) {
      Averager<Double> averager = new Averager<>();
      
      LinkedList<VersionedFqnNode> coreFragments = new LinkedList<>();
      LinkedList<VersionedFqnNode> otherFragments = new LinkedList<>();
      
      for (VersionedFqnNode otherFqn : other.getCoreFqns()) {
        // Break the package into fragments
        breakFqn(otherFqn.getParent(), otherFragments);
        
        // Does the fqn not have a package?
        // Find the FQN in the core cluster that matches best
        Averager<Double> averageMatch = new Averager<>();
        for (VersionedFqnNode coreFqn : core.getCoreFqns()) {
          breakFqn(coreFqn.getParent(), coreFragments);
          if (coreFragments.isEmpty()) {
            if (otherFragments.isEmpty()) {
              averageMatch.addValue(1.);
            } else {
              averageMatch.addValue(0.);
            }
          } else {
            Iterator<VersionedFqnNode> coreIter = coreFragments.iterator();
            Iterator<VersionedFqnNode> otherIter = otherFragments.iterator();
            int overlap = 0;
            while (coreIter.hasNext() && otherIter.hasNext()) {
              if (coreIter.next() == otherIter.next()) {
                overlap++;
              } else {
                break;
              }
            }
            averageMatch.addValue((double) overlap / otherFragments.size());
          }
          coreFragments.clear();
          averager.addValue(averageMatch.getMean());
        }
        otherFragments.clear();
      }
      
      writer.writeAndIndent("Results");
      writer.write("Average Max Similarity: " + averager.getMean());
      boolean doMerge = averager.getMean() >= MERGE_THRESHOLD.getValue();
      writer.write("Merge? " + (doMerge ? "yes" : " no"));
      writer.unindent();
      
      return doMerge;
    }
  },
  ENTROPY {
    @Override
    protected boolean shouldMergeHelper(Cluster smaller, Cluster larger, LogFileWriter writer) {
      ClusterEntopyCalculator calc = ClusterEntropyCalculatorFactory.createCalculator();
      double smallerEntropy = calc.compute(smaller);
      double largerEntropy = calc.compute(larger);
      double jointEntropy = calc.compute(smaller, larger);
      
      writer.writeAndIndent("Results");
      writer.write("Smaller entropy: " + smallerEntropy);
      writer.write("Larger entropy: " + largerEntropy);
      writer.write("Joint entropy: " + jointEntropy);
      double minDelta = jointEntropy - Math.max(smallerEntropy, largerEntropy);
      double maxDelta = jointEntropy - Math.min(smallerEntropy, largerEntropy);
      writer.write("Max Entropy Delta: " + maxDelta);
      writer.write("Min Entropy Delta: " + minDelta);
      boolean doMerge = maxDelta <= .2 && minDelta < .1;
      writer.write("Merge more into less? " + (doMerge ? "yes" : "no"));
      writer.unindent();
      
      return doMerge; 
    }
  };
  
  public static final Argument<Double> MERGE_THRESHOLD = new DoubleArgument("merge-threshold", 0.5, "Threshold for jaccard package similarity").permit();
  public static final Argument<Integer> MINIMUM_THRESHOLD = new IntegerArgument("minimum-threshold", 50, "Minimum threshold (out of 100) to test").permit();
  public static final Argument<Integer> THRESHOLD_DECREMENT = new IntegerArgument("threshold-decrement", 5, "Threshold decrement, starting at 100, to test").permit();
  
  public void doForEachVersion(Action action) {
    Identifier.MERGE_METHOD.setValue(this);
    action.doMe();
  }
  
  public boolean shouldMerge(Cluster core, Cluster other, LogFileWriter writer) {
    // Core means a large cluster of co-occuring FQNs
    // Other means a smaller cluster of co-occuring FQNs
    // We want to check if the smaller cluster always occurs with the larger cluster
    // If it does, then it needs to meet the individual merging criteria
    if (other.getJars().isSubset(core.getJars())) {
      writer.writeAndIndent("Considering merging two clusters");
      
      writer.writeAndIndent("Core Cluster (" + core.getJars().size() + ")");
      
      writer.writeAndIndent("Core FQNs");
      for (VersionedFqnNode fqn : core.getCoreFqns()) {
        writer.write(fqn.getFqn());
      }
      writer.unindent();
      
      if (!core.getExtraFqns().isEmpty()) {
        writer.writeAndIndent("Extra FQNs");
        for (VersionedFqnNode fqn : core.getExtraFqns()) {
          writer.write(fqn.getFqn());
        }
        writer.unindent();
      }
      
      writer.unindent();
      
      writer.writeAndIndent("Other Cluster (" + other.getJars().size() + ")");
      
      writer.writeAndIndent("Core FQNs");
      for (VersionedFqnNode fqn : other.getCoreFqns()) {
        writer.write(fqn.getFqn());
      }
      writer.unindent();
      
      if (!other.getExtraFqns().isEmpty()) {
        writer.writeAndIndent("Extra FQNs");
        for (VersionedFqnNode fqn : other.getExtraFqns()) {
          writer.write(fqn.getFqn());
        }
        writer.unindent();
      }
      
      writer.unindent();
      
      boolean result = shouldMergeHelper(core, other, writer);
      
      writer.unindent();
      
      return result;
    } else {
      return false;
    }
  }
  
  protected abstract boolean shouldMergeHelper(Cluster smaller, Cluster larger, LogFileWriter writer);
  
  @Override
  public String toString() {
    return name();
  }
}
