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
package edu.uci.ics.sourcerer.model.metrics;

import java.util.Collection;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.LineBuildable;
import edu.uci.ics.sourcerer.util.io.LineBuilder;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Metrics implements LineBuildable {
  private Map<Metric, Integer> metrics;
  
  public Metrics() {
    this.metrics = Helper.newEnumMap(Metric.class); 
  }
  
  public void addMetric(Metric metric, int value) {
    metrics.put(metric, value);
  }
  
  public Collection<Map.Entry<Metric, Integer>> getMetricValues() {
    return metrics.entrySet();
  }
  
  public static Metrics parse(Scanner scanner) {
    Metrics metrics = new Metrics();
    
    if (scanner.hasNextInt()) {
      for (int count = scanner.nextInt(); count > 0; count--) {
        metrics.metrics.put(Metric.valueOf(scanner.next()), scanner.nextInt());
      }
    } else if (!"null".equals(scanner.next())) {
      throw new InputMismatchException();
    }
    
    return metrics;
  } 
  
  @Override
  public void addToLineBuilder(LineBuilder builder) {
    builder.append(metrics.size());
    for (Map.Entry<Metric, Integer> entry : metrics.entrySet()) {
      builder.append(entry.getKey().name()).append(entry.getValue().toString());
    }
  }
}
