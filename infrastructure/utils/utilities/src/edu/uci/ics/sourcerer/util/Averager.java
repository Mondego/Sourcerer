package edu.uci.ics.sourcerer.util;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

import edu.uci.ics.sourcerer.util.io.IOUtils;

public class Averager <T extends Number> {
  private double sum;
  private int nonZero;
  private T min;
  private T max;
  private Collection<T> values;
  
  private Averager() {
    values = new ArrayList<>();
    sum = 0;
    nonZero = 0;
  }
  
  public static <T extends Number> Averager<T> create() {
    return new Averager<T>();
  }
  
  public void addValue(T value) {
    if (Math.abs(value.doubleValue()) > .00000001) {
      nonZero++;
    }
    values.add(value);
    sum += value.doubleValue();
    if (min == null || value.doubleValue() < min.doubleValue()) {
      min = value;
    }
    if (max == null || value.doubleValue() > max.doubleValue()) {
      max = value;
    }
  }
  
  public int getCount() {
    return values.size();
  }
  
  public int getNonZeroCount() {
    return nonZero;
  }
  
  public double getSum() {
    return sum;
  }
  
  public double getMean() {
    if (values.isEmpty()) {
      return Double.NaN;
    } else {
      return sum / (double) values.size();
    }
  }
  
  @SuppressWarnings("unchecked")
  public double getMedian() {
    if (values.isEmpty()) {
      return Double.NaN;
    } else {
      Object[] arr = values.toArray(); 
      Arrays.sort(arr);
      if (arr.length % 2 == 0) {
        T left = (T) arr[arr.length / 2 - 1];
        T right = (T) arr[arr.length / 2];
        return (left.doubleValue() + right.doubleValue()) / 2;
      } else {
        return ((T) arr[arr.length / 2]).doubleValue();
      }
    }
  }
  
  public double getNonZeroMean() {
    if (values.size() == 0) {
      return Double.NaN;
    } else {
      return sum / (double) nonZero;
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
      for (T value : values) {
        variance += Math.pow(value.doubleValue() - mean, 2);
      }
      variance /= values.size();
      double std = Math.sqrt(variance);
      return std;
    }
  }
  
  public double getNonZeroStandardDeviation() {
    if (values.size() == 0) {
      return Double.NaN;
    } else {
      double mean = sum / (double) values.size();
      double variance = 0;
      for (T value : values) {
        variance += Math.pow(value.doubleValue() - mean, 2);
      }
      variance /= values.size();
      double std = Math.sqrt(variance);
      return std;
    }
  }
  
  public void writeValueMap(File file) {
    Multiset<T> counts = HashMultiset.create();
    for (T value : values) {
      counts.add(value);
    }
    SortedMultiset<Integer> sorted = TreeMultiset.create();
    for (T value : counts.elementSet()) {
      sorted.add(counts.count(value));
    }
    try (BufferedWriter writer = IOUtils.makeBufferedWriter(file)) {
      for (Integer element : sorted.elementSet()) {
        writer.write(element + " " + sorted.count(element));
        writer.newLine();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing value map", e);
    }
  }
  
  public void writeDoubleValueMap(File file, int places) {
    double mult = Math.pow(10, places);
    Multiset<Integer> counts = HashMultiset.create();
    for (T value : values) {
      counts.add((int) (value.doubleValue() * mult));
    }
    try (BufferedWriter writer = IOUtils.makeBufferedWriter(file)) {
      NumberFormat format = NumberFormat.getNumberInstance();
      format.setMinimumFractionDigits(places);
      format.setMaximumFractionDigits(places);
      for (Integer val : counts.elementSet()) {
        writer.write(format.format(val.doubleValue() / mult) + " " + counts.count(val));
        writer.newLine();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing value map", e);
    }
  }
  
//  public String getCellValue() {
//    if (values.size() == 0) {
//      return "-";
//    } else {
//      double sum = 0;
//      for (T value : values) {
//        sum += value.doubleValue();
//      }
//      double mean = sum / values.size();
//      return "" + mean;
//    }
//  }
//  
//  public String getCellValueWithStandardDeviation() {
//    if (values.size() == 0) {
//      return "-";
//    } else {
//      double sum = 0;
//      for (T value : values) {
//        sum += value.doubleValue();
//      }
//      double mean = sum / values.size();
//      double variance = 0;
//      for (T value : values) {
//        variance += Math.pow(value.doubleValue() - mean, 2);
//      }
//      variance /= values.size();
//      double std = Math.sqrt(variance);
//      return mean + " (" + std + ")";
//    }
//  }
}
