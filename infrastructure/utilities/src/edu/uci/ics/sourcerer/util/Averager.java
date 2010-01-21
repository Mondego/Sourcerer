package edu.uci.ics.sourcerer.util;

import java.util.Collection;

public class Averager <T extends Number> {
  private double sum;
  private T min;
  private T max;
  private Collection<T> values;
  
  public Averager() {
    values = Helper.newLinkedList();
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
  
  public String getSum() {
    return "" + sum;
  }
  
  public String getMean() {
    if (values.size() == 0) {
      return "-";
    } else {
      return Double.toString(sum / (double) values.size());
    }
  }
  
  public String getMin() {
    return min.toString(); 
  }
  
  public String getMax() {
    return max.toString();
  }
  
  public String getStandardDeviation() {
    if (values.size() == 0) {
      return "-";
    } else {
      double mean = sum / (double) values.size();
      double variance = 0;
      for (T value : values) {
        variance += Math.pow(value.doubleValue() - mean, 2);
      }
      variance /= values.size();
      double std = Math.sqrt(variance);
      return "" + std;
    }
  }
  
  public String getCellValue() {
    if (values.size() == 0) {
      return "-";
    } else {
      double sum = 0;
      for (T value : values) {
        sum += value.doubleValue();
      }
      double mean = sum / values.size();
      return "" + mean;
    }
  }
  
  public String getCellValueWithStandardDeviation() {
    if (values.size() == 0) {
      return "-";
    } else {
      double sum = 0;
      for (T value : values) {
        sum += value.doubleValue();
      }
      double mean = sum / values.size();
      double variance = 0;
      for (T value : values) {
        variance += Math.pow(value.doubleValue() - mean, 2);
      }
      variance /= values.size();
      double std = Math.sqrt(variance);
      return mean + " (" + std + ")";
    }
  }
}
