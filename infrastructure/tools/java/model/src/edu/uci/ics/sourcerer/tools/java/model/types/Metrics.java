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
package edu.uci.ics.sourcerer.tools.java.model.types;

import java.util.Collection;
import java.util.EnumMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

import edu.uci.ics.sourcerer.util.io.CustomSerializable;
import edu.uci.ics.sourcerer.util.io.LineBuilder;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Metrics implements CustomSerializable {
  private Map<Metric, Double> metrics;
  
  public Metrics() {
    this.metrics = new EnumMap<>(Metric.class); 
  }
  
  public void addMetric(Metric metric, double value) {
    metrics.put(metric, value);
  }
  
  public Double getValue(Metric metric) {
    return metrics.get(metric);
  }
  
  public Collection<Map.Entry<Metric, Double>> getMetricValues() {
    return metrics.entrySet();
  }
  
  public static Metrics deserialize(Scanner scanner) {
    if (scanner.hasNextInt()) {
      Metrics metrics = new Metrics();
      
      for (int count = scanner.nextInt(); count > 0; count--) {
        metrics.metrics.put(Metric.valueOf(scanner.next()), scanner.nextDouble());
      }
      return metrics;
    } else if (!"null".equals(scanner.next())) {
      throw new InputMismatchException();
    } else {
      return null;
    }
  } 
  
  @Override
  public String serialize() {
    LineBuilder builder = new LineBuilder();
    builder.append(metrics.size());
    for (Map.Entry<Metric, Double> entry : metrics.entrySet()) {
      builder.append(entry.getKey().name()).append(entry.getValue().toString());
    }
    return builder.toString();
  }
}
