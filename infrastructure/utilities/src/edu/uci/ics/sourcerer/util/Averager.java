package edu.uci.ics.sourcerer.util;

import java.util.Collection;

public class Averager <T extends Number> {
  private double sum;
  private T min;
  private T max;
  private Collection<T> values;
  
  public Averager() {
    values = Helper.newArrayList();
    sum = 0;
  }
  
  public void addValue(T value) {
    values.add(value);
    sum += value.doubleValue();
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
