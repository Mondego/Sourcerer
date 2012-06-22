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
package edu.uci.ics.sourcerer.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class WeightedAverager <T extends Number> {
  private double sum;
  private double weightedSum;
  private double weightSum;
  private T min;
  private T max;
  private Collection<Pair<T, Double>> values;
  
  public WeightedAverager() {
    values = new ArrayList<>();
    sum = 0;
    weightedSum = 0;
  }
  
  public void addValue(T value, double weight) {
    values.add(new Pair<T, Double>(value, weight));
    sum += value.doubleValue();
    weightedSum += value.doubleValue() * weight;
    weightSum += weight;
    if (min == null || value.doubleValue() < min.doubleValue()) {
      min = value;
    }
    if (max == null || value.doubleValue() > max.doubleValue()) {
      max = value;
    }
  }
  
  public double getSum() {
    return sum;
  }
  
  public double getMean() {
    if (values.size() == 0) {
      return Double.NaN;
    } else {
      return sum / (double) values.size();
    }
  }
  
  public double getWeightedMean() {
    if (values.size() == 0) {
      return Double.NaN;
    } else {
      return weightedSum / weightSum;
    }
  }
  
  public T getMin() {
    return min; 
  }
  
  public T getMax() {
    return max;
  }
  
  public double getStandardDeviation() {
    if (values.size() == 0) {
      return Double.NaN;
    } else {
      double mean = sum / (double) values.size();
      double variance = 0;
      for (Pair<T, Double> entry : values) {
        variance += Math.pow(entry.getFirst().doubleValue() - mean, 2);
      }
      variance /= values.size();
      double std = Math.sqrt(variance);
      return std;
    }
  }
  
  public double getWeightedStandardDeviation() {
    if (values.size() == 0) {
      return Double.NaN;
    } else {
      double mean = sum / (double) values.size();
      double variance = 0;
      for (Pair<T, Double> entry : values) {
        variance += entry.getSecond() * Math.pow(entry.getFirst().doubleValue() - mean, 2);
      }
      variance /= weightedSum;
      double std = Math.sqrt(variance);
      return std;
    }
  }
}
