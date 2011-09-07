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
package edu.uci.ics.sourcerer.tools.java.extractor.eclipse;

import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.tools.java.model.types.Metrics;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MetricsCalculator {
  private static final Pattern linePattern = Pattern.compile("\\r?\\n");
  private static final Pattern pureWhitespace = Pattern.compile("\\s*");
  public static Metrics computeLinesOfCode(String source) {
    Metrics metrics = new Metrics();
    if (source == null) {
      metrics.addMetric(Metric.LINES_OF_CODE, 0);
      metrics.addMetric(Metric.NON_WHITESPACE_LINES_OF_CODE, 0);
    } else {
      String[] lines = linePattern.split(source);
      int nwLoc = 0; 
      for (String line : lines) {
        if (!pureWhitespace.matcher(line).matches()) {
          nwLoc++;
        }
      }
      metrics.addMetric(Metric.LINES_OF_CODE, lines.length);
      metrics.addMetric(Metric.NON_WHITESPACE_LINES_OF_CODE, nwLoc);
    }
    return metrics;
  }
}