package edu.uci.ics.sourcerer.util;

import java.util.Collection;

public class Averager <T extends Number> {
  private Collection<T> values;
  
  public Averager() {
    values = Helper.newLinkedList();
  }
  
  public void addValue(T value) {
    values.add(value);
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
