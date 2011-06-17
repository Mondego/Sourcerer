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
package edu.uci.ics.sourcerer.server.famix;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uci.ics.sourcerer.db.tools.FamixExporter;
import edu.uci.ics.sourcerer.model.db.LargeProjectDB;
import edu.uci.ics.sourcerer.model.db.ProjectMetricDB;
import edu.uci.ics.sourcerer.model.metrics.Metric;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.server.ServletUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FamixServer extends HttpServlet {
  @Override
  public void init() throws ServletException {
    PropertyManager.PROPERTIES_STREAM.setValue(getServletContext().getResourceAsStream("/WEB-INF/lib/famix-server.properties"));
    PropertyManager.initializeProperties();
  }
  
  @Override
  public void destroy() {
    logger.log(Level.INFO, "Destroying");
  }
  
  private Integer getIntValue(HttpServletRequest request, String name) {
    String val = request.getParameter(name);
    if (val == null) {
      return null;
    } else {
      try {
        return Integer.valueOf(val);
      } catch (NumberFormatException e) {
        logger.log(Level.SEVERE, val + " is not an int");
        return null;
      }
    }
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Integer projectID = getIntValue(request, "projectID");
    if (projectID == null) {
      Map<Metric, Map<Integer, Integer>> map = Helper.newEnumMap(Metric.class);
      for (Metric metric : Metric.values()) {
        map.put(metric, Helper.<Integer, Integer>newHashMap());
      }
      for (ProjectMetricDB metric : FamixExporter.getProjectMetrics()) {
        map.get(metric.getMetric()).put(metric.getProjectID(), metric.getValue());
      }
      
      StringBuilder builder = new StringBuilder();
      for (LargeProjectDB project : FamixExporter.getProjects()) {
        if (project.completed()) {
          builder.append(project.getProjectID()).append(" ").append(project.getName());
          for (Map.Entry<Metric, Map<Integer, Integer>> entry : map.entrySet()) {
            if (entry.getValue().containsKey(project.getProjectID())) {
              builder.append(" ").append(entry.getKey().name()).append(":").append(entry.getValue().get(project.getProjectID()));
            }
          }
        }
        builder.append("\n");
      }
      ServletUtils.writeByteArray(response, null, builder.toString().getBytes());
    } else {
      ServletUtils.writeByteArray(response, projectID.toString() + ".mse", FamixExporter.getFamixModel(projectID));
    }
  }
}
