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
import edu.uci.ics.sourcerer.tools.java.utilization.model.FqnFragment;
import edu.uci.ics.sourcerer.util.Action;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.DoubleArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public enum ClusterMergeMethod {
  RELATED_SUBPACKAGE {
    @Override
    public boolean shouldMergeHelper(Cluster smaller, Cluster larger, LogFileWriter writer) {
      // Is every package in larger cluster either in the smaller cluster
      //   or a subpackage of a package in the smaller cluster
      Set<FqnFragment> smallerPackages = new HashSet<>();
      for (FqnFragment fqn : smaller.getFqns()) {
        smallerPackages.add(fqn.getParent());
      }
      for (FqnFragment fqn : larger.getFqns()) {
        boolean found = false;
        while (fqn != null) {
          if (smallerPackages.contains(fqn)) {
            found = true;
            fqn = null;
          } else {
            fqn = fqn.getParent();
          }
        }
        if (!found) {
          writer.writeAndIndent("Result");
          writer.write("Merge? No");
          writer.unindent();
          return false;
        }
      }
      writer.writeAndIndent("Result");
      writer.write("Merge? Yes");
      writer.unindent();
      return true;
    }
  },
  JACCARD_PACKAGE {
    @Override
    public void doForEachVersion(Action action) {
      Cluster.MERGE_METHOD.setValue(this);
      for (int threshold = 100; threshold > 75; threshold -= 5) {
        JACCARD_THRESHOLD.setValue(threshold / 100.);
        action.doMe();
      }
    }
    
    @Override
    public String toString() {
      NumberFormat format = NumberFormat.getInstance();
      format.setMaximumFractionDigits(2);
      return name() + "-" + format.format(JACCARD_THRESHOLD.getValue());
    }
    
    @Override
    public boolean shouldMergeHelper(Cluster smaller, Cluster larger, LogFileWriter writer) {
      // Size of package intersection over size of package union
      Set<FqnFragment> smallerPackages = new HashSet<>();
      for (FqnFragment fqn : smaller.getFqns()) {
        smallerPackages.add(fqn.getParent());
      }
      Set<FqnFragment> largerPackages = new HashSet<>();
      for (FqnFragment fqn : larger.getFqns()) {
        largerPackages.add(fqn.getParent());
      }
      int intersectionSize = 0;
      int unionSize = 0;
      for (FqnFragment fqn : largerPackages) {
        if (smallerPackages.contains(fqn)) {
          intersectionSize++;
        } else {
          unionSize++;
        }
      }
      unionSize += largerPackages.size() - intersectionSize;
      double jaccardIndex = (double) intersectionSize / unionSize;
      
      writer.writeAndIndent("Results");
      writer.write("Jaccard: " + jaccardIndex);
      boolean doMerge = jaccardIndex >= JACCARD_THRESHOLD.getValue();
      writer.write("Merge? " + (doMerge ? "yes" : " no"));
      writer.unindent();

      return doMerge;
    }
  },
  // TODO Make an edit-distance version!
  MAX_PATH_SIMILARITY {
    @Override
    public void doForEachVersion(Action action) {
      Cluster.MERGE_METHOD.setValue(this);
      for (int threshold = 100; threshold > 75; threshold -= 5) {
        PATH_SIMILARITY_THRESHOLD.setValue(threshold / 100.);
        action.doMe();
      }
    }
    
    @Override
    public String toString() {
      NumberFormat format = NumberFormat.getInstance();
      format.setMaximumFractionDigits(2);
      return name() + "-" + format.format(PATH_SIMILARITY_THRESHOLD.getValue());
    }
    
    private void breakFqn(FqnFragment fqn, LinkedList<FqnFragment> fragments) {
      while (fqn.getName() != null) {
        fragments.addFirst(fqn);
        fqn = fqn.getParent();
      }
    }
    
    @Override
    public boolean shouldMergeHelper(Cluster smaller, Cluster larger, LogFileWriter writer) {
      Averager<Double> averager = new Averager<>();
      
      LinkedList<FqnFragment> largerFragments = new LinkedList<>();
      LinkedList<FqnFragment> smallerFragments = new LinkedList<>();
      for (FqnFragment largerFqn : larger.getFqns()) {
        // Break the package into fragments
        breakFqn(largerFqn.getParent(), largerFragments);
        
        // Find the FQN in the smaller cluster that matches best
        double bestMatch = 0;
        for (FqnFragment smallerFqn : smaller.getFqns()) {
          breakFqn(smallerFqn.getParent(), smallerFragments);
          if (largerFragments.isEmpty()) {
            if (smallerFragments.isEmpty()) {
              bestMatch = 1.;
            }
          } else {
            Iterator<FqnFragment> largerIter = largerFragments.iterator();
            Iterator<FqnFragment> smallerIter = smallerFragments.iterator();
            int overlap = 0;
            while (largerIter.hasNext() && smallerIter.hasNext()) {
              if (largerIter.next() == smallerIter.next()) {
                overlap++;
              } else {
                break;
              }
            }
            bestMatch = Math.max(bestMatch, (double) overlap / largerFragments.size());
          }
          smallerFragments.clear();
        }
        averager.addValue(bestMatch);
        largerFragments.clear();
      }
      
      writer.writeAndIndent("Results");
      writer.write("Average Max Similarity: " + averager.getMean());
      boolean doMerge = averager.getMean() >= PATH_SIMILARITY_THRESHOLD.getValue();
      writer.write("Merge? " + (doMerge ? "yes" : " no"));
      writer.unindent();
      
      return doMerge;
    }
  },
  AVG_PATH_SIMILARITY {
    @Override
    public void doForEachVersion(Action action) {
      Cluster.MERGE_METHOD.setValue(this);
      for (int threshold = 100; threshold > 75; threshold -= 5) {
        PATH_SIMILARITY_THRESHOLD.setValue(threshold / 100.);
        action.doMe();
      }
    }
    
    @Override
    public String toString() {
      NumberFormat format = NumberFormat.getInstance();
      format.setMaximumFractionDigits(2);
      return name() + "-" + format.format(PATH_SIMILARITY_THRESHOLD.getValue());
    }
    
    private void breakFqn(FqnFragment fqn, LinkedList<FqnFragment> fragments) {
      while (fqn.getName() != null) {
        fragments.addFirst(fqn);
        fqn = fqn.getParent();
      }
    }
    
    @Override
    public boolean shouldMergeHelper(Cluster smaller, Cluster larger, LogFileWriter writer) {
      Averager<Double> averager = new Averager<>();
      
      LinkedList<FqnFragment> largerFragments = new LinkedList<>();
      LinkedList<FqnFragment> smallerFragments = new LinkedList<>();
      for (FqnFragment largerFqn : larger.getFqns()) {
        // Break the package into fragments
        breakFqn(largerFqn.getParent(), largerFragments);
        
        // Does the fqn not have a package?
        // Find the FQN in the smaller cluster that matches best
        Averager<Double> averageMatch = new Averager<>();
        for (FqnFragment smallerFqn : smaller.getFqns()) {
          breakFqn(smallerFqn.getParent(), smallerFragments);
          if (largerFragments.isEmpty()) {
            if (smallerFragments.isEmpty()) {
              averageMatch.addValue(1.);
            } else {
              averageMatch.addValue(0.);
            }
          } else {
            Iterator<FqnFragment> largerIter = largerFragments.iterator();
            Iterator<FqnFragment> smallerIter = smallerFragments.iterator();
            int overlap = 0;
            while (largerIter.hasNext() && smallerIter.hasNext()) {
              if (largerIter.next() == smallerIter.next()) {
                overlap++;
              } else {
                break;
              }
            }
            averageMatch.addValue((double) overlap / largerFragments.size());
          }
          smallerFragments.clear();
          averager.addValue(averageMatch.getMean());
        }
        largerFragments.clear();
      }
      
      writer.writeAndIndent("Results");
      writer.write("Average Max Similarity: " + averager.getMean());
      boolean doMerge = averager.getMean() >= PATH_SIMILARITY_THRESHOLD.getValue();
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
      writer.write("Smaller entropy: " + smaller);
      writer.write("Larger entropy: " + larger);
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
  
  public static final Argument<Double> JACCARD_THRESHOLD = new DoubleArgument("jaccard-threshold", "Threshold for jaccard package similarity").permit();
  public static final Argument<Double> PATH_SIMILARITY_THRESHOLD = new DoubleArgument("path-similarity-threshold", "Threshold for path similarity").permit();
  
  public void doForEachVersion(Action action) {
    Cluster.MERGE_METHOD.setValue(this);
    action.doMe();
  }
  
  public boolean shouldMerge(Cluster smaller, Cluster larger, LogFileWriter writer) {
    // Smaller means "has fewer primary jars"
    // Larger means "has more primary jars"
    // We want to check if the smaller cluster should subsume the larger cluster
    // Is every primary jar from the smaller cluster also in the larger cluster?
    if (smaller.getPrimaryJars().getIntersectionSize(larger.getPrimaryJars()) == smaller.getPrimaryJars().size()) {
      writer.writeAndIndent("Considering merging two clusters");
      
      writer.writeAndIndent("Smaller Cluster");
      for (FqnFragment fqn : smaller.getFqns()) {
        writer.write(fqn.getFqn());
      }
      writer.unindent();
      
      writer.writeAndIndent("Larger Cluster");
      for (FqnFragment fqn : larger.getFqns()) {
        writer.write(fqn.getFqn());
      }
      writer.unindent();
      
      boolean result = shouldMergeHelper(smaller, larger, writer);
      
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
