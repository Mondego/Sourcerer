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
package edu.uci.ics.sourcerer.apps.artifactbrowser;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClusterFqnType;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClusterVersionToFqnVersionTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClusterVersionToJarTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClusterVersionsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClustersTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.FqnVersionsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.FqnsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.JarToFqnVerionTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.JarsTable;
import edu.uci.ics.sourcerer.util.TimeoutManager;
import edu.uci.ics.sourcerer.util.io.arguments.ArgumentManager;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnection;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;
import edu.uci.ics.sourcerer.utils.servlet.ServletUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("serial")
public class ArtifactRepoBrowser extends HttpServlet {
  private TimeoutManager<DatabaseConnection> db =
      new TimeoutManager<DatabaseConnection>(new TimeoutManager.Instantiator<DatabaseConnection>() {
        @Override
        public DatabaseConnection create() {
          DatabaseConnection conn = DatabaseConnectionFactory.INSTANCE.make();
          if (conn.open()) {
            return conn;
          } else {
            return null;
          }
        }
        
      }, 10 * 60 * 1000);
  
  @Override
  public void init() throws ServletException {
    ArgumentManager.PROPERTIES_STREAM.setValue(getServletContext().getResourceAsStream("/WEB-INF/lib/artifact-repo-browser.properties"));
    DatabaseConnectionFactory.DATABASE_URL.permit();
    DatabaseConnectionFactory.DATABASE_USER.permit();
    DatabaseConnectionFactory.DATABASE_PASSWORD.permit();
    ArgumentManager.initializeProperties();
  }
  
  private void serveMain(StringBuilder html) {
    html.append(
        "<ul>" +
          "<li><a href=\"./clusters\">Cluster Listing</a></li>" +
          "<li><a href=\"./jars\">Jar Listing</a></li>" +
          "<li><a href=\"./fqns\">FQN Listing</a></li>" +
        "</ul>");
  }
  
  private void serveClusterList(StringBuilder html) {
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.makeSelectQuery(ClustersTable.TABLE)) {
      query.addSelect(ClustersTable.CLUSTER_ID);
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer clusterID = result.getResult(ClustersTable.CLUSTER_ID);
        html.append("<li><a href=\"?clusterID=" + clusterID + "\">Cluster " + result.getResult(ClustersTable.CLUSTER_ID) + "</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveCluster(Integer clusterID, StringBuilder html) {
    QueryExecutor exec = db.get().getExecutor();
    
    html.append("<h3>Cluster ").append(clusterID).append("</h3>");
    
    try (SelectQuery query = exec.makeSelectQuery(ClusterVersionsTable.CLUSTER_VERSION_ID.compareEquals(ClusterVersionToJarTable.CLUSTER_VERSION_ID), ClusterVersionToJarTable.JAR_ID.compareEquals(JarsTable.JAR_ID))) {
      query.addSelects(JarsTable.NAME, JarsTable.JAR_ID);
      query.andWhere(ClusterVersionsTable.CLUSTER_ID.compareEquals(clusterID));
      query.orderBy(JarsTable.NAME, true);
      
      // Jars
      html.append("<h4>Jars</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        html.append("<li><a href=\"./jars?jarID=" + result.getResult(JarsTable.JAR_ID) + "\">" + result.getResult(JarsTable.NAME) + "</a></li>");
      }
      html.append("</ul>");
    }
    
    try (SelectQuery query = exec.makeSelectQuery(ClusterVersionsTable.TABLE)) {
      query.addSelect(ClusterVersionsTable.CLUSTER_VERSION_ID);
      query.andWhere(ClusterVersionsTable.CLUSTER_ID.compareEquals(clusterID));
      query.orderBy(ClusterVersionsTable.CLUSTER_ID, true);
      
      // Cluster Versions
      html.append("<h4>Cluster Versions</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer clusterVersionID = result.getResult(ClusterVersionsTable.CLUSTER_VERSION_ID);
        html.append("<li><a href=\"./clusters?clusterVersionID=" + clusterVersionID + "\">Cluster Version " + clusterVersionID + "</a></li>");
      }
      html.append("</ul>");
    }
    
    try (SelectQuery query = exec.makeSelectQuery(FqnsTable.TABLE)) {
      query.addSelects(FqnsTable.FQN, FqnsTable.FQN_ID);
      ConstantCondition<ClusterFqnType> typeCond = FqnsTable.TYPE.compareEquals();
      query.andWhere(FqnsTable.CLUSTER_ID.compareEquals(clusterID).and(typeCond));
      query.orderBy(FqnsTable.FQN, true);
      
      // Core FQNs
      html.append("<h4>Core FQNs</h4>");
      html.append("<ul>");
      typeCond.setValue(ClusterFqnType.CORE);
      TypedQueryResult result = query.select();
      while (result.next()) {
        html.append("<li><a href=\"./fqns?fqnID=" + result.getResult(FqnsTable.FQN_ID) + "\">" + result.getResult(FqnsTable.FQN) + "</a></li>");
      }
      html.append("</ul>");
      
      // Version FQNs
      html.append("<h4>Version FQNs</h4>");
      html.append("<ul>");
      typeCond.setValue(ClusterFqnType.VERSION);
      result = query.select();
      while (result.next()) {
        html.append("<li><a href=\"./fqns?" + result.getResult(FqnsTable.FQN_ID) + "\">" + result.getResult(FqnsTable.FQN) + "</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveClusterVersion(Integer clusterVersionID, StringBuilder html) {
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.makeSelectQuery(ClusterVersionsTable.TABLE)) {
      query.addSelect(ClusterVersionsTable.CLUSTER_ID);
      query.andWhere(ClusterVersionsTable.CLUSTER_VERSION_ID.compareEquals(clusterVersionID));
      
      Integer clusterID = query.select().toSingleton(ClusterVersionsTable.CLUSTER_ID, false);
      html.append("<h3><a href=\"./clusters?clusterID=").append(clusterID).append("\">Cluster ").append(clusterID).append(" Version ").append(clusterVersionID).append("</a></h3>");
    }
    
    try (SelectQuery query = exec.makeSelectQuery(ClusterVersionToJarTable.JAR_ID.compareEquals(JarsTable.JAR_ID))) {
      query.addSelects(JarsTable.NAME, JarsTable.JAR_ID);
      query.andWhere(ClusterVersionToJarTable.CLUSTER_VERSION_ID.compareEquals(clusterVersionID));
      query.orderBy(JarsTable.NAME, true);
      
      // Jars
      html.append("<h4>Jars</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        html.append("<li><a href=\"./jars?jarID=" + result.getResult(JarsTable.JAR_ID) + "\">" + result.getResult(JarsTable.NAME) + "</a></li>");
      }
      html.append("</ul>");
    }
    
    try (SelectQuery query = exec.makeSelectQuery(ClusterVersionToFqnVersionTable.FQN_VERSION_ID.compareEquals(FqnVersionsTable.FQN_VERSION_ID), FqnVersionsTable.FQN_ID.compareEquals(FqnsTable.FQN_ID))) {
      query.addSelects(FqnVersionsTable.FQN_VERSION_ID, FqnsTable.FQN);
      query.andWhere(ClusterVersionToFqnVersionTable.CLUSTER_VERSION_ID.compareEquals(clusterVersionID));
      query.orderBy(FqnsTable.FQN, true);
      
      // FQNs
      html.append("<h4>FQNs</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        html.append("<li><a href=\"./fqns?fqnVersionID=" + result.getResult(FqnVersionsTable.FQN_VERSION_ID) + "\">" + result.getResult(FqnsTable.FQN) + "</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveJarList(StringBuilder html) {
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.makeSelectQuery(JarsTable.TABLE)) {
      query.addSelects(JarsTable.JAR_ID, JarsTable.NAME);
      query.orderBy(JarsTable.NAME, true);
      
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        html.append("<li><a href=\"?jarID=" + result.getResult(JarsTable.JAR_ID)+ "\">" + result.getResult(JarsTable.NAME) + "</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveJar(Integer jarID, StringBuilder html) {
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.makeSelectQuery(JarsTable.TABLE)) {
      query.addSelect(JarsTable.NAME);
      query.andWhere(JarsTable.JAR_ID.compareEquals(jarID));
      
      html.append("<h3>Jar ").append(jarID).append(": " + query.select().toSingleton(JarsTable.NAME, false) +"</h3>");
    }
    
    try (SelectQuery query = exec.makeSelectQuery(ClusterVersionToJarTable.CLUSTER_VERSION_ID.compareEquals(ClusterVersionsTable.CLUSTER_VERSION_ID))) {
      query.addSelects(ClusterVersionToJarTable.CLUSTER_VERSION_ID, ClusterVersionsTable.CLUSTER_ID);
      query.andWhere(ClusterVersionToJarTable.JAR_ID.compareEquals(jarID));
      query.orderBy(ClusterVersionsTable.CLUSTER_ID, true);
      
      // Clusters
      html.append("<h4>Clusters</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer clusterVersionID = result.getResult(ClusterVersionToJarTable.CLUSTER_VERSION_ID);
        html.append("<li><a href=\"./clusters?clusterVersionID=" + clusterVersionID + "\">Cluster " + result.getResult(ClusterVersionsTable.CLUSTER_ID) + "." + clusterVersionID + "</a></li>");
      }
      html.append("</ul>");
    }
    
    try (SelectQuery query = exec.makeSelectQuery(JarToFqnVerionTable.FQN_VERSION_ID.compareEquals(FqnVersionsTable.FQN_VERSION_ID), FqnVersionsTable.FQN_ID.compareEquals(FqnsTable.FQN_ID))) {
      query.addSelects(FqnVersionsTable.FQN_VERSION_ID, FqnsTable.FQN);
      query.andWhere(JarToFqnVerionTable.JAR_ID.compareEquals(jarID));
      query.orderBy(FqnsTable.FQN, true);
      
      // Fqns
      html.append("<h4>FQNs</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        html.append("<li><a href=\"./fqns?fqnVersionID=" + result.getResult(FqnVersionsTable.FQN_VERSION_ID) + "\">" + result.getResult(FqnsTable.FQN) + "</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveFqnList(StringBuilder html) {
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.makeSelectQuery(FqnsTable.TABLE)) {
      query.addSelects(FqnsTable.FQN_ID, FqnsTable.FQN);
      query.orderBy(FqnsTable.FQN, true);
      
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        html.append("<li><a href=\"?fqnID=" + result.getResult(FqnsTable.FQN_ID)+ "\">" + result.getResult(FqnsTable.FQN) + "</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveFqn(Integer fqnID, StringBuilder html) {
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.makeSelectQuery(FqnsTable.TABLE)) {
      query.addSelect(FqnsTable.FQN);
      query.andWhere(FqnsTable.FQN_ID.compareEquals(fqnID));
      
      html.append("<h3>FQN ").append(fqnID).append(": " + query.select().toSingleton(FqnsTable.FQN, false) +"</h3>");
    }
    
    try (SelectQuery query = exec.makeSelectQuery(FqnVersionsTable.FQN_VERSION_ID.compareEquals(JarToFqnVerionTable.FQN_VERSION_ID), JarToFqnVerionTable.JAR_ID.compareEquals(JarsTable.JAR_ID))) {
      query.addSelects(JarsTable.NAME, JarsTable.JAR_ID);
      query.andWhere(FqnVersionsTable.FQN_ID.compareEquals(fqnID));
      query.orderBy(JarsTable.NAME, true);
      
      // Jars
      html.append("<h4>Jars</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        html.append("<li><a href=\"./jars?jarID=" + result.getResult(JarsTable.JAR_ID) + "\">" + result.getResult(JarsTable.NAME) + "</a></li>");
      }
      html.append("</ul>");
    }
    
    try (SelectQuery query = exec.makeSelectQuery(FqnVersionsTable.FQN_VERSION_ID.compareEquals(ClusterVersionToFqnVersionTable.FQN_VERSION_ID), ClusterVersionToFqnVersionTable.CLUSTER_VERSION_ID.compareEquals(ClusterVersionsTable.CLUSTER_VERSION_ID))) {
      Selectable<Integer> distinctCol = ClusterVersionsTable.CLUSTER_ID.makeDistinct();
      query.addSelects(distinctCol);
      query.andWhere(FqnVersionsTable.FQN_ID.compareEquals(fqnID));
      query.orderBy(ClusterVersionsTable.CLUSTER_ID, true);
      
      // Clusters
      html.append("<h4>Clusters</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer clusterID = result.getResult(distinctCol);
        html.append("<li><a href=\"./clusters?clusterID=" + clusterID + "\">" + clusterID + "</a></li>");
      }
      html.append("</ul>");
    }
    
    try (SelectQuery query = exec.makeSelectQuery(FqnVersionsTable.TABLE)) {
      query.addSelect(FqnVersionsTable.FQN_VERSION_ID);
      query.andWhere(FqnVersionsTable.FQN_ID.compareEquals(fqnID));
      query.orderBy(FqnVersionsTable.FQN_ID, true);
      
      // FQN Versions
      html.append("<h4>FQN Versions</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer fqnVersionID = result.getResult(FqnVersionsTable.FQN_VERSION_ID);
        html.append("<li><a href=\"./fqns?fqnVersionID=" + fqnVersionID + "\">FQN Version " + fqnVersionID + "</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveFqnVersion(Integer fqnVersionID, StringBuilder html) {
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.makeSelectQuery(FqnVersionsTable.FQN_ID.compareEquals(FqnsTable.FQN_ID))) {
      query.addSelects(FqnsTable.FQN, FqnsTable.FQN_ID);
      query.andWhere(FqnVersionsTable.FQN_VERSION_ID.compareEquals(fqnVersionID));
      
      TypedQueryResult result = query.select();
      if (result.next()) {
        Integer fqnID = result.getResult(FqnsTable.FQN_ID);
        html.append("<h3><a href=\"./fqns?fqnID=").append(fqnID).append("\">").append("FQN ").append(fqnID).append(": ").append(result.getResult(FqnsTable.FQN)).append(" Version ").append(fqnVersionID).append("</a></h3>");
      }
    }
    
    try (SelectQuery query = exec.makeSelectQuery(JarToFqnVerionTable.JAR_ID.compareEquals(JarsTable.JAR_ID))) {
      query.addSelects(JarsTable.NAME, JarsTable.JAR_ID);
      query.andWhere(JarToFqnVerionTable.FQN_VERSION_ID.compareEquals(fqnVersionID));
      query.orderBy(JarsTable.NAME, true);
      
      // Jars
      html.append("<h4>Jars</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        html.append("<li><a href=\"./jars?jarID=" + result.getResult(JarsTable.JAR_ID) + "\">" + result.getResult(JarsTable.NAME) + "</a></li>");
      }
      html.append("</ul>");
    }
    
    try (SelectQuery query = exec.makeSelectQuery(ClusterVersionToFqnVersionTable.CLUSTER_VERSION_ID.compareEquals(ClusterVersionsTable.CLUSTER_VERSION_ID))) {
      query.addSelects(ClusterVersionsTable.CLUSTER_ID, ClusterVersionsTable.CLUSTER_VERSION_ID);
      query.andWhere(ClusterVersionToFqnVersionTable.FQN_VERSION_ID.compareEquals(fqnVersionID));
      query.orderBy(ClusterVersionsTable.CLUSTER_ID, true);
      query.orderBy(ClusterVersionsTable.CLUSTER_VERSION_ID, true);
      
      // Clusters
      html.append("<h4>Cluster Versions</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer clusterID = result.getResult(ClusterVersionsTable.CLUSTER_ID);
        Integer clusterVersionID = result.getResult(ClusterVersionsTable.CLUSTER_VERSION_ID);
        html.append("<li><a href=\"./clusters?clusterVersionID=").append(clusterVersionID).append("\">").append(clusterID).append(".").append(clusterVersionID).append("</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    StringBuilder html = new StringBuilder(
        "<html>" +
        "<head><title>Sourcerer Artifact Repository</title></head>" +
        "<body>");
    String path = request.getPathInfo();
    if ("/clusters".equals(path)) {
      Integer clusterID = ServletUtils.getIntValue(request, "clusterID");
      if (clusterID == null) {
        Integer clusterVersionID = ServletUtils.getIntValue(request, "clusterVersionID");
        if (clusterVersionID == null) {
          serveClusterList(html);
        } else {
          serveClusterVersion(clusterVersionID, html);
        }
        
      } else {
        serveCluster(clusterID, html);
      }
    } else if ("/jars".equals(path)) {
      Integer jarID = ServletUtils.getIntValue(request, "jarID");
      if (jarID == null) {
        serveJarList(html);
      } else {
        serveJar(jarID, html);
      }
    } else if ("/fqns".equals(path)) {
      Integer fqnID = ServletUtils.getIntValue(request, "fqnID");
      if (fqnID == null) {
        Integer fqnVersionID = ServletUtils.getIntValue(request, "fqnVersionID");
        if (fqnVersionID == null) {
          serveFqnList(html);
        } else {
          serveFqnVersion(fqnVersionID, html);
        }
      } else {
        serveFqn(fqnID, html);
      }
    } else {
      serveMain(html);
    }
    html.append("</body></html>");
    ServletUtils.writeString(response, null, html.toString(), true);
  }
}
