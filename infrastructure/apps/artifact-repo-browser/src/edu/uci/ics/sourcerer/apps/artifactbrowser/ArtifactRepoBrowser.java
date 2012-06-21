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
import java.util.EnumSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentRelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.TypeVersionsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.TypesTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Component;
import edu.uci.ics.sourcerer.tools.java.model.types.ComponentRelation;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.model.types.Type;
import edu.uci.ics.sourcerer.util.TimeoutManager;
import edu.uci.ics.sourcerer.util.io.arguments.ArgumentManager;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnection;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedColumn;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedTable;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
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
          DatabaseConnection conn = DatabaseConnectionFactory.INSTANCE.create();
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
  
  private static void appendJarName(TypedQueryResult result, StringBuilder builder) {
    String name = result.getResult(ProjectsTable.NAME);
    String version = result.getResult(ProjectsTable.VERSION);
    String group = result.getResult(ProjectsTable.GROUP);
    
    if (group != null) {
      builder.append(group).append(".");
    }
    builder.append(name);
    if (version != null) {
      builder.append(" (").append(version).append(")");
    }
  }
  
  private void serveMain(StringBuilder html) {
    html.append(
        "<ul>" +
          "<li><a href=\"./libraries\">Library Listing</a></li>" +
          "<li><a href=\"./clusters\">Cluster Listing</a></li>" +
          "<li><a href=\"./jars\">Jar Listing</a></li>" +
          "<li><a href=\"./fqns\">FQN Listing</a></li>" +
          "<li><a href=\"./maven\">Maven Listing</a></li>" +
        "</ul>");
  }
  
  private void serveLibraryList(StringBuilder html) {
    html.append("<p><a href=\"./\">main</a></p>");

    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.createSelectQuery(ComponentsTable.TABLE)) {
      query.addSelect(ComponentsTable.COMPONENT_ID);
      query.andWhere(ComponentsTable.TYPE.compareEquals(Component.LIBRARY));
      query.orderBy(ComponentsTable.COMPONENT_ID, true);
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer componentID = result.getResult(ComponentsTable.COMPONENT_ID);
        html.append("<li><a href=\"?libraryID=").append(componentID).append("\">Library ").append(componentID).append("</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveLibrary(Integer libraryID, StringBuilder html) {
    html.append("<p><a href=\"./\">main</a>/<a href=\"./libraries\">libraries</a></p>");
    
    QueryExecutor exec = db.get().getExecutor();
    
    html.append("<h3>Library ").append(libraryID).append("</h3>");
    
    // Library Versions
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TABLE);
         SelectQuery inner = exec.createSelectQuery(ComponentRelationsTable.SOURCE_ID.compareEquals(ProjectsTable.PROJECT_ID))) {
      query.addSelect(ComponentRelationsTable.TARGET_ID);
      query.andWhere(ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION), ComponentRelationsTable.SOURCE_ID.compareEquals(libraryID));
      query.orderBy(ComponentRelationsTable.TARGET_ID, true);
      
      inner.addSelect(ProjectsTable.PROJECT_ID, ProjectsTable.NAME, ProjectsTable.GROUP, ProjectsTable.VERSION);
      ConstantCondition<Integer> innerCond = ComponentRelationsTable.TARGET_ID.compareEquals();
      inner.andWhere(innerCond.and(ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.JAR_MATCHES_LIBRARY_VERSION)));
      
      int libraryVersionCount = 0;
      StringBuilder temp = new StringBuilder();
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer libraryVersionID = result.getResult(ComponentRelationsTable.TARGET_ID);
        temp.append("<li>");
        temp.append("<a href=\"./libraries?libraryVersionID=").append(libraryVersionID).append("\">Library Version ").append(libraryVersionID).append("</a>");
        
        temp.append("<ul>");
        innerCond.setValue(libraryVersionID);
        TypedQueryResult innerResult = inner.select();
        while (innerResult.next()) {
          Integer jarID = innerResult.getResult(ProjectsTable.PROJECT_ID);
          temp.append("<li><a href=\"./jars?jarID=").append(jarID).append("\">");
          appendJarName(innerResult, temp);
          temp.append("</a></li>");
        }
        temp.append("</ul></li>");
        libraryVersionCount++;
      }
      
      html.append("<h4>").append(libraryVersionCount).append(" Library Versions</h4>");
      html.append("<ul>");
      html.append(temp.toString());
      html.append("</ul>");
    }
    
    { // Jars
      QualifiedTable l2lv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable j2lv = ComponentRelationsTable.TABLE.qualify("b");
      
      try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.qualify(l2lv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(j2lv)), ComponentRelationsTable.SOURCE_ID.qualify(j2lv).compareEquals(ProjectsTable.PROJECT_ID))) {
        query.addSelect(ProjectsTable.NAME, ProjectsTable.GROUP, ProjectsTable.VERSION, ProjectsTable.PROJECT_ID);
        query.andWhere(ComponentRelationsTable.SOURCE_ID.qualify(l2lv).compareEquals(libraryID), ComponentRelationsTable.TYPE.qualify(l2lv).compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION), ComponentRelationsTable.TYPE.qualify(j2lv).compareEquals(ComponentRelation.JAR_MATCHES_LIBRARY_VERSION));
        query.orderBy(ProjectsTable.GROUP, true);
        query.orderBy(ProjectsTable.NAME, true);
        query.orderBy(ProjectsTable.VERSION, true);
        
        StringBuilder temp = new StringBuilder();
        Integer jarCount = 0;
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer jarID = result.getResult(ProjectsTable.PROJECT_ID);
          temp.append("<li><a href=\"./jars?jarID=").append(jarID).append("\">");
          appendJarName(result, temp);
          temp.append("</a></li>");
          jarCount++;
        }
        html.append("<h4>").append(jarCount).append(" Jars</h4>");
        html.append("<ul>");
        html.append(temp.toString());
        html.append("</ul>");
      }
    }
 
    // Contained by Libraries
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TABLE)) {
      query.addSelect(ComponentRelationsTable.SOURCE_ID);
      query.andWhere(ComponentRelationsTable.TARGET_ID.compareEquals(libraryID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_LIBRARY));
      query.orderBy(ComponentRelationsTable.TARGET_ID, true);   

      html.append("<h4>Contained by library versions</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer libraryVersionID = result.getResult(ComponentRelationsTable.SOURCE_ID);
        html.append("<li><a href=\"./libraries?libraryVersionID=").append(libraryVersionID).append("\">Library Version ").append(libraryVersionID).append("</a></li>");
      }
      html.append("</ul>");
    }
    
    // Core Cluster
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TABLE)) {
      query.addSelect(ComponentRelationsTable.TARGET_ID);
      query.andWhere(ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_MATCHES_CLUSTER), ComponentRelationsTable.SOURCE_ID.compareEquals(libraryID));
      
      Integer clusterID = query.select().toSingleton(ComponentRelationsTable.TARGET_ID, true);
      if (clusterID != null) {
        html.append("<h4>Core Cluster</h4>");
        html.append("<ul>");
        html.append("<li><a href=\"./clusters?clusterID=").append(clusterID).append("\">Cluster ").append(clusterID).append("</a></li>");
        html.append("</ul>");
      }
    }
    
    // Clusters
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TABLE)) {
      query.addSelect(ComponentRelationsTable.TARGET_ID);
      query.andWhere(ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_CONTAINS_CLUSTER), ComponentRelationsTable.SOURCE_ID.compareEquals(libraryID));
      query.orderBy(ComponentRelationsTable.TARGET_ID, true);
      
      html.append("<h4>Version Clusters</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer clusterID = result.getResult(ComponentRelationsTable.TARGET_ID);
        html.append("<li><a href=\"./clusters?clusterID=").append(clusterID).append("\">Cluster ").append(clusterID).append("</a></li>");
      }
      html.append("</ul>");
    }
    
    { // FQNs
      QualifiedTable l2lv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable lv2tv = ComponentRelationsTable.TABLE.qualify("b");
      
      try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.qualify(l2lv).compareEquals(ComponentRelationsTable.SOURCE_ID.qualify(lv2tv)), ComponentRelationsTable.TARGET_ID.qualify(lv2tv).compareEquals(TypeVersionsTable.TYPE_VERSION_ID), TypeVersionsTable.TYPE_ID.compareEquals(TypesTable.TYPE_ID))) {
        query.setDistinct(true);
        query.addSelect(TypesTable.FQN, TypesTable.TYPE_ID);
        query.andWhere(ComponentRelationsTable.SOURCE_ID.qualify(l2lv).compareEquals(libraryID), ComponentRelationsTable.TYPE.qualify(l2lv).compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION), ComponentRelationsTable.TYPE.qualify(lv2tv).compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_TYPE_VERSION));
        query.orderBy(TypesTable.FQN, true);
        
        html.append("<h4>FQNs</h4>");
        html.append("<ul>");
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer fqnID = result.getResult(TypesTable.TYPE_ID);
          String fqn = result.getResult(TypesTable.FQN);
          html.append("<li><a href=\"./fqns?fqnID=").append(fqnID).append("\">").append(fqn).append("</a></li>");
        }
        html.append("</ul>");
      }
    }
  }
  
  private void serveLibraryVersion(Integer libraryVersionID, StringBuilder html) {
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TABLE)) {
      query.addSelect(ComponentRelationsTable.SOURCE_ID);
      query.andWhere(ComponentRelationsTable.TARGET_ID.compareEquals(libraryVersionID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION));
      
      Integer libraryID = query.select().toSingleton(ComponentRelationsTable.SOURCE_ID, false);
      html.append("<p><a href=\"./\">main</a>/<a href=\"./libraries\">libraries</a>/<a href=\"./libraries?libraryID=").append(libraryID).append("\">Library ").append(libraryID).append("</a></p>");
      html.append("<h3>Library ").append(libraryID).append(" Version ").append(libraryVersionID).append("</h3>");
    }

    // Jars
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.SOURCE_ID.compareEquals(ProjectsTable.PROJECT_ID))) {
      query.addSelect(ProjectsTable.NAME, ProjectsTable.GROUP, ProjectsTable.VERSION, ProjectsTable.PROJECT_ID);
      query.andWhere(ComponentRelationsTable.TARGET_ID.compareEquals(libraryVersionID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.JAR_MATCHES_LIBRARY_VERSION));
      query.orderBy(ProjectsTable.GROUP, true);
      query.orderBy(ProjectsTable.NAME, true);
      query.orderBy(ProjectsTable.VERSION, true);
      
      Integer jarCount = 0;
      StringBuilder temp = new StringBuilder();
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer jarID = result.getResult(ProjectsTable.PROJECT_ID);
        temp.append("<li><a href=\"./jars?jarID=").append(jarID).append("\">");
        appendJarName(result, temp);
        temp.append("</a></li>");
        jarCount++;
      }
      html.append("<h4>").append(jarCount).append(" Jars</h4>");
      html.append("<ul>");
      html.append(temp.toString());
      html.append("</ul>");
    }
    
    { // Cluster Versions
      QualifiedTable libV2clusterV = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable cluster2clusterV = ComponentRelationsTable.TABLE.qualify("b");
      
      try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.qualify(libV2clusterV).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(cluster2clusterV)))) {
        query.addSelect(ComponentRelationsTable.SOURCE_ID.qualify(cluster2clusterV), ComponentRelationsTable.TARGET_ID.qualify(cluster2clusterV));
        query.andWhere(ComponentRelationsTable.SOURCE_ID.qualify(libV2clusterV).compareEquals(libraryVersionID), ComponentRelationsTable.TYPE.qualify(libV2clusterV).compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_CLUSTER_VERSION), ComponentRelationsTable.TYPE.qualify(cluster2clusterV).compareEquals(ComponentRelation.CLUSTER_CONTAINS_CLUSTER_VERSION));
        query.orderBy(ComponentRelationsTable.TARGET_ID.qualify(cluster2clusterV), true);
        
        
        html.append("<h4>Cluster Versions</h4>");
        html.append("<ul>");
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer clusterID = result.getResult(ComponentRelationsTable.SOURCE_ID.qualify(cluster2clusterV));
          Integer clusterVersionID = result.getResult(ComponentRelationsTable.TARGET_ID.qualify(cluster2clusterV));
          html.append("<li><a href=\"./clusters?clusterVersionID=").append(clusterVersionID).append("\">Cluster ").append(clusterID).append(".").append(clusterVersionID).append("</a></li>");
        }
        html.append("</ul>");
      }
    }

    // Libraries
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TABLE)) {
      query.addSelect(ComponentRelationsTable.TARGET_ID);
      query.andWhere(ComponentRelationsTable.SOURCE_ID.compareEquals(libraryVersionID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_LIBRARY));
      query.orderBy(ComponentRelationsTable.TARGET_ID, true);   

      html.append("<h4>Contains libraries</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer libraryID = result.getResult(ComponentRelationsTable.TARGET_ID);
        html.append("<li><a href=\"./libraries?libraryID=").append(libraryID).append("\">Library ").append(libraryID).append("</a></li>");
      }
      html.append("</ul>");
    }

    { // Library versions
      QualifiedTable lv2lv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable l2lv = ComponentRelationsTable.TABLE.qualify("b");
      
      try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.qualify(lv2lv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(l2lv)))) {
        query.addSelect(ComponentRelationsTable.SOURCE_ID.qualify(l2lv), ComponentRelationsTable.TARGET_ID.qualify(l2lv));
        query.andWhere(ComponentRelationsTable.SOURCE_ID.qualify(lv2lv).compareEquals(libraryVersionID), ComponentRelationsTable.TYPE.qualify(lv2lv).compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_LIBRARY_VERSION), ComponentRelationsTable.TYPE.qualify(l2lv).compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION));
        query.orderBy(ComponentRelationsTable.TARGET_ID.qualify(l2lv), true);   
  
        html.append("<h4>Contains library versions</h4>");
        html.append("<ul>");
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer libraryID = result.getResult(ComponentRelationsTable.SOURCE_ID.qualify(l2lv));
          Integer versionID = result.getResult(ComponentRelationsTable.TARGET_ID.qualify(l2lv));
          html.append("<li><a href=\"./libraries?libraryVersionID=").append(versionID).append("\">Library ").append(libraryID).append(".").append(versionID).append("</a></li>");
        }
        html.append("</ul>");
      }
    }

    // Contained by library versions
    {
      QualifiedTable lv2lv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable l2lv = ComponentRelationsTable.TABLE.qualify("b");
      
      try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.SOURCE_ID.qualify(lv2lv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(l2lv)))) {
        query.addSelect(ComponentRelationsTable.SOURCE_ID.qualify(l2lv), ComponentRelationsTable.TARGET_ID.qualify(l2lv));
        query.andWhere(ComponentRelationsTable.TARGET_ID.qualify(lv2lv).compareEquals(libraryVersionID), ComponentRelationsTable.TYPE.qualify(lv2lv).compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_LIBRARY_VERSION), ComponentRelationsTable.TYPE.qualify(l2lv).compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION));
        query.orderBy(ComponentRelationsTable.TARGET_ID.qualify(l2lv), true);   
  
        html.append("<h4>Contained by library versions</h4>");
        html.append("<ul>");
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer libraryID = result.getResult(ComponentRelationsTable.SOURCE_ID.qualify(l2lv));
          Integer versionID = result.getResult(ComponentRelationsTable.TARGET_ID.qualify(l2lv));
          html.append("<li><a href=\"./libraries?libraryVersionID=").append(versionID).append("\">Library ").append(libraryID).append(".").append(versionID).append("</a></li>");
        }
        html.append("</ul>");
      }
    }

    // FQNs
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.compareEquals(TypeVersionsTable.TYPE_VERSION_ID), TypeVersionsTable.TYPE_ID.compareEquals(TypesTable.TYPE_ID))) {
      query.addSelect(TypeVersionsTable.TYPE_VERSION_ID, TypesTable.FQN);
      query.andWhere(ComponentRelationsTable.SOURCE_ID.compareEquals(libraryVersionID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_TYPE_VERSION));
      query.orderBy(TypesTable.FQN, true);
      
      html.append("<h4>FQNs</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer fqnVersionID = result.getResult(TypeVersionsTable.TYPE_VERSION_ID);
        String fqn = result.getResult(TypesTable.FQN);
        html.append("<li><a href=\"./fqns?fqnVersionID=").append(fqnVersionID).append("\">").append(fqn).append("</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveClusterList(StringBuilder html) {
    html.append("<p><a href=\"./\">main</a></p>");
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.createSelectQuery(ComponentsTable.TABLE)) {
      query.addSelect(ComponentsTable.COMPONENT_ID);
      query.andWhere(ComponentsTable.TYPE.compareEquals(Component.CLUSTER));
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer clusterID = result.getResult(ComponentsTable.COMPONENT_ID);
        html.append("<li><a href=\"?clusterID=").append(clusterID).append("\">Cluster ").append(clusterID).append("</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveCluster(Integer clusterID, StringBuilder html) {
    html.append("<p><a href=\"./\">main</a>/<a href=\"./clusters\">clusters</a></p>");
    
    QueryExecutor exec = db.get().getExecutor();
    
    html.append("<h3>Cluster ").append(clusterID).append("</h3>");

    { // Jars
      QualifiedTable c2cv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable jar2cv = ComponentRelationsTable.TABLE.qualify("b");
       
      try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.qualify(c2cv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(jar2cv)), ComponentRelationsTable.SOURCE_ID.qualify(jar2cv).compareEquals(ProjectsTable.PROJECT_ID))) {
        query.addSelect(ProjectsTable.NAME, ProjectsTable.GROUP, ProjectsTable.VERSION, ProjectsTable.PROJECT_ID);
        query.andWhere(ComponentRelationsTable.SOURCE_ID.qualify(c2cv).compareEquals(clusterID), ComponentRelationsTable.TYPE.qualify(c2cv).compareEquals(ComponentRelation.CLUSTER_CONTAINS_CLUSTER_VERSION), ComponentRelationsTable.TYPE.qualify(jar2cv).compareEquals(ComponentRelation.JAR_CONTAINS_CLUSTER_VERSION));
        query.orderBy(ProjectsTable.GROUP, true);
        query.orderBy(ProjectsTable.NAME, true);
        query.orderBy(ProjectsTable.VERSION, true);
  
        StringBuilder temp = new StringBuilder();
        Integer jarCount = 0;
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer jarID = result.getResult(ProjectsTable.PROJECT_ID);
          temp.append("<li><a href=\"./jars?jarID=").append(jarID).append("\">");
          appendJarName(result, temp);
          temp.append("</a></li>");
          jarCount++;
        }
        html.append("<h4>").append(jarCount).append(" Jars</h4>");
        html.append(temp.toString());
        html.append("<ul>");
        html.append("</ul>");
      }
    }
    
    // Cluster Versions
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TABLE)) {
      query.addSelect(ComponentRelationsTable.TARGET_ID);
      query.andWhere(ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.CLUSTER_CONTAINS_CLUSTER_VERSION), ComponentRelationsTable.SOURCE_ID.compareEquals(clusterID));
      query.orderBy(ComponentRelationsTable.TARGET_ID, true);
      
      html.append("<h4>Cluster Versions</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer clusterVersionID = result.getResult(ComponentRelationsTable.TARGET_ID);
        html.append("<li><a href=\"./clusters?clusterVersionID=").append(clusterVersionID).append("\">Cluster Version ").append(clusterVersionID).append("</a></li>");
      }
      html.append("</ul>");
    }

    // Core Library
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TABLE)) {
      query.addSelect(ComponentRelationsTable.SOURCE_ID);
      query.andWhere(ComponentRelationsTable.TARGET_ID.compareEquals(clusterID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_MATCHES_CLUSTER));
      

      html.append("<h4>Core Library</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer libraryID = result.getResult(ComponentRelationsTable.SOURCE_ID);
        html.append("<li><a href=\"./libraries?libraryID=").append(libraryID).append("\">Library ").append(libraryID).append("</a></li>");
      }
      html.append("</ul>");
    }

    // Libraries
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TABLE)) {
      query.addSelect(ComponentRelationsTable.SOURCE_ID);
      query.andWhere(ComponentRelationsTable.TARGET_ID.compareEquals(clusterID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_CONTAINS_CLUSTER));
      query.orderBy(ComponentRelationsTable.SOURCE_ID, true);
      
      html.append("<h4>Libraries</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
//      Integer previousLibraryID = null;
      while (result.next()) {
        Integer libraryID = result.getResult(ComponentRelationsTable.SOURCE_ID);
//        if (libraryID != previousLibraryID) {
//          if (previousLibraryID != null) {
//            html.append("</ul></li>");
//          }
//          html.append("<li><a href=\"./libraries?libraryID=").append(libraryID).append("\">Library ").append(libraryID).append("</a><ul>");
//          previousLibraryID = libraryID;
//        }
//        Integer libraryVersionID = result.getResult(LibraryVersionsTable.LIBRARY_VERSION_ID);
//        html.append("<li><a href=\"./libraries?libraryVersionID=").append(libraryVersionID).append("\">Library ").append(libraryID).append(".").append(libraryVersionID).append("</a></li>");
        html.append("<li><a href=\"./libraries?libraryID=").append(libraryID).append("\">Library ").append(libraryID).append("</a></li>");
      }
//      if (previousLibraryID != null) {
//        html.append("</ul></li>");
//      }
      html.append("</ul>");
    }

    // FQNs
    try (SelectQuery query = exec.createSelectQuery(TypesTable.TABLE)) {
      query.addSelect(TypesTable.FQN, TypesTable.TYPE_ID);
      ConstantCondition<Type> typeCond = TypesTable.TYPE.compareEquals();
      query.andWhere(TypesTable.COMPONENT_ID.compareEquals(clusterID).and(typeCond));
      query.orderBy(TypesTable.FQN, true);
      
      html.append("<h4>Core FQNs</h4>");
      html.append("<ul>");
      typeCond.setValue(Type.CORE);
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer fqnID = result.getResult(TypesTable.TYPE_ID);
        String fqn = result.getResult(TypesTable.FQN);
        html.append("<li><a href=\"./fqns?fqnID=").append(fqnID).append("\">").append(fqn).append("</a></li>");
      }
      html.append("</ul>");
      
      // Version FQNs
      html.append("<h4>Version FQNs</h4>");
      html.append("<ul>");
      typeCond.setValue(Type.VERSION);
      result = query.select();
      while (result.next()) {
        Integer fqnID = result.getResult(TypesTable.TYPE_ID);
        String fqn = result.getResult(TypesTable.FQN);
        html.append("<li><a href=\"./fqns?").append(fqnID).append("\">").append(fqn).append("</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveClusterVersion(Integer clusterVersionID, StringBuilder html) {
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TABLE)) {
      query.addSelect(ComponentRelationsTable.SOURCE_ID);
      query.andWhere(ComponentRelationsTable.TARGET_ID.compareEquals(clusterVersionID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.CLUSTER_CONTAINS_CLUSTER_VERSION));
      
      Integer clusterID = query.select().toSingleton(ComponentRelationsTable.SOURCE_ID, false);
      html.append("<p><a href=\"./\">main</a>/<a href=\"./clusters\">libraries</a>/<a href=\"./clusters?clusterID=").append(clusterID).append("\">Cluster ").append(clusterID).append("</a></p>");
      html.append("<h3>Cluster ").append(clusterID).append(" Version ").append(clusterVersionID).append("</h3>");
    }

    // Jars
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.SOURCE_ID.compareEquals(ProjectsTable.PROJECT_ID))) {
      query.addSelect(ProjectsTable.NAME, ProjectsTable.GROUP, ProjectsTable.VERSION, ProjectsTable.PROJECT_ID);
      query.andWhere(ComponentRelationsTable.TARGET_ID.compareEquals(clusterVersionID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.JAR_CONTAINS_CLUSTER_VERSION));
      query.orderBy(ProjectsTable.GROUP, true);
      query.orderBy(ProjectsTable.NAME, true);
      query.orderBy(ProjectsTable.VERSION, true);

      StringBuilder temp = new StringBuilder();
      Integer jarCount = 0;
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer jarID = result.getResult(ProjectsTable.PROJECT_ID);
        temp.append("<li><a href=\"./jars?jarID=").append(jarID).append("\">");
        appendJarName(result, temp);
        temp.append("</a></li>");
      }
      html.append("<h4>").append(jarCount).append(" Jars</h4>");
      html.append("<ul>");
      html.append(temp.toString());
      html.append("</ul>");
    }

    { // Library versions
      QualifiedTable lv2cv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable l2lv = ComponentRelationsTable.TABLE.qualify("b");
      
      try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.SOURCE_ID.qualify(lv2cv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(l2lv)))) {
        query.addSelect(ComponentRelationsTable.SOURCE_ID.qualify(l2lv), ComponentRelationsTable.TARGET_ID.qualify(l2lv));
        query.andWhere(ComponentRelationsTable.TARGET_ID.qualify(lv2cv).compareEquals(clusterVersionID), ComponentRelationsTable.TYPE.qualify(lv2cv).compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_CLUSTER_VERSION));
        query.orderBy(ComponentRelationsTable.TARGET_ID.qualify(l2lv), true);
  
        html.append("<h4>Library Versions</h4>");
        html.append("<ul>");
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer libraryID = result.getResult(ComponentRelationsTable.SOURCE_ID.qualify(l2lv));
          Integer libraryVersionID = result.getResult(ComponentRelationsTable.TARGET_ID.qualify(l2lv));
          html.append("<li><a href=\"./libraries?libraryVersionID=").append(libraryVersionID).append("\">").append("Library ").append(libraryID).append(".").append(libraryVersionID).append("</a></li>");
        }
        html.append("</ul>");
      }
    }

    // FQNs
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.compareEquals(TypeVersionsTable.TYPE_VERSION_ID), TypeVersionsTable.TYPE_ID.compareEquals(TypesTable.TYPE_ID))) {
      query.addSelect(TypeVersionsTable.TYPE_VERSION_ID, TypesTable.FQN);
      query.andWhere(ComponentRelationsTable.SOURCE_ID.compareEquals(clusterVersionID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.CLUSTER_VERSION_CONTAINS_TYPE_VERSION));
      query.orderBy(TypesTable.FQN, true);

      html.append("<h4>FQNs</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer fqnVersionID = result.getResult(TypeVersionsTable.TYPE_VERSION_ID);
        String fqn = result.getResult(TypesTable.FQN);
        html.append("<li><a href=\"./fqns?fqnVersionID=").append(fqnVersionID).append("\">").append(fqn).append("</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveJarList(StringBuilder html) {
    html.append("<p><a href=\"./\">main</a></p>");
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.createSelectQuery(ProjectsTable.TABLE)) {
      query.addSelect(ProjectsTable.PROJECT_ID, ProjectsTable.GROUP, ProjectsTable.VERSION, ProjectsTable.NAME);
      query.andWhere(ProjectsTable.PROJECT_TYPE.compareIn(EnumSet.of(Project.JAR, Project.MAVEN)));
      query.orderBy(ProjectsTable.GROUP, true);
      query.orderBy(ProjectsTable.NAME, true);
      query.orderBy(ProjectsTable.VERSION, true);
      
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer jarID = result.getResult(ProjectsTable.PROJECT_ID);
        html.append("<li><a href=\"?jarID=").append(jarID).append("\">");
        appendJarName(result, html);
        html.append("</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveJar(Integer jarID, StringBuilder html) {
    html.append("<p><a href=\"./\">main</a>/<a href=\"./jars\">jars</a></p>");
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.createSelectQuery(ProjectsTable.TABLE)) {
      query.addSelect(ProjectsTable.NAME, ProjectsTable.GROUP, ProjectsTable.VERSION);
      query.andWhere(ProjectsTable.PROJECT_ID.compareEquals(jarID));
      
      html.append("<h3>Jar ").append(jarID).append(": ");
      TypedQueryResult result = query.select();
      if (result.next()) {
        appendJarName(result, html);
      }
      html.append("</h3>");
    }

    { // Library versions
      QualifiedTable j2lv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable l2lv = ComponentRelationsTable.TABLE.qualify("b");
      
      try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.qualify(j2lv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(l2lv)))) {
        query.addSelect(ComponentRelationsTable.SOURCE_ID.qualify(l2lv), ComponentRelationsTable.TARGET_ID.qualify(l2lv));
        query.andWhere(ComponentRelationsTable.SOURCE_ID.qualify(j2lv).compareEquals(jarID), ComponentRelationsTable.TYPE.qualify(j2lv).compareEquals(ComponentRelation.JAR_MATCHES_LIBRARY_VERSION), ComponentRelationsTable.TYPE.qualify(l2lv).compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION));
        query.orderBy(ComponentRelationsTable.TARGET_ID.qualify(l2lv), true);
        
        html.append("<h4>Library Version</h4>");
        html.append("<ul>");
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer libraryID = result.getResult(ComponentRelationsTable.SOURCE_ID.qualify(l2lv));
          Integer libraryVersionID = result.getResult(ComponentRelationsTable.TARGET_ID.qualify(l2lv));
          html.append("<li><a href=\"./libraries?libraryVersionID=").append(libraryVersionID).append("\">").append("Library ").append(libraryID).append(".").append(libraryVersionID).append("</a></li>");
        }
        html.append("</ul>");
      }
    }
    {
      // Cluster versions
      QualifiedTable j2cv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable c2cv = ComponentRelationsTable.TABLE.qualify("b");
      try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.qualify(j2cv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(c2cv)))) {
        query.addSelect(ComponentRelationsTable.SOURCE_ID.qualify(c2cv), ComponentRelationsTable.TARGET_ID.qualify(c2cv));
        query.andWhere(ComponentRelationsTable.SOURCE_ID.qualify(j2cv).compareEquals(jarID), ComponentRelationsTable.TYPE.qualify(j2cv).compareEquals(ComponentRelation.JAR_CONTAINS_CLUSTER_VERSION), ComponentRelationsTable.TYPE.qualify(c2cv).compareEquals(ComponentRelation.CLUSTER_CONTAINS_CLUSTER_VERSION));
        query.orderBy(ComponentRelationsTable.TARGET_ID.qualify(c2cv), true);
        
        html.append("<h4>Clusters Versions</h4>");
        html.append("<ul>");
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer clusterID = result.getResult(ComponentRelationsTable.SOURCE_ID.qualify(c2cv));
          Integer clusterVersionID = result.getResult(ComponentRelationsTable.TARGET_ID.qualify(c2cv));
          html.append("<li>Cluster Version <a href=\"./clusters?clusterID=").append(clusterID).append("\">").append(clusterID).append(".").append("<a href=\"./clusters?clusterVersionID=").append(clusterVersionID).append("\">").append(clusterVersionID).append("</a></li>");
        }
        html.append("</ul>");
      }
    }

    // Fqns
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.compareEquals(TypeVersionsTable.TYPE_VERSION_ID), TypeVersionsTable.TYPE_ID.compareEquals(TypesTable.TYPE_ID))) {
      query.addSelect(TypeVersionsTable.TYPE_VERSION_ID, TypesTable.FQN);
      query.andWhere(ComponentRelationsTable.SOURCE_ID.compareEquals(jarID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.JAR_CONTAINS_TYPE_VERSION));
      query.orderBy(TypesTable.FQN, true);
      

      html.append("<h4>FQNs</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer fqnVersionID = result.getResult(TypeVersionsTable.TYPE_VERSION_ID);
        String fqn = result.getResult(TypesTable.FQN);
        html.append("<li><a href=\"./fqns?fqnVersionID=").append(fqnVersionID).append("\">").append(fqn).append("</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveFqnList(StringBuilder html) {
    html.append("<p><a href=\"./\">main</a></p>");
    
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.createSelectQuery(TypesTable.TABLE)) {
      query.addSelect(TypesTable.TYPE_ID, TypesTable.FQN);
      query.orderBy(TypesTable.FQN, true);
      
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer fqnID = result.getResult(TypesTable.TYPE_ID);
        String fqn = result.getResult(TypesTable.FQN);
        html.append("<li><a href=\"?fqnID=").append(fqnID).append("\">").append(fqn).append("</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveFqn(Integer fqnID, StringBuilder html) {
    html.append("<p><a href=\"./\">main</a>/<a href=\"./fqns\">fqns</a></p>");
    
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.createSelectQuery(TypesTable.TABLE)) {
      query.addSelect(TypesTable.FQN);
      query.andWhere(TypesTable.TYPE_ID.compareEquals(fqnID));
      
      html.append("<h3>FQN ").append(fqnID).append(": ").append(query.select().toSingleton(TypesTable.FQN, false)).append("</h3>");
    }
    
    { // Libraries
      QualifiedTable lv2tv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable l2lv = ComponentRelationsTable.TABLE.qualify("b");
      
      try (SelectQuery query = exec.createSelectQuery(TypeVersionsTable.TYPE_VERSION_ID.compareEquals(ComponentRelationsTable.TARGET_ID.qualify(lv2tv)), ComponentRelationsTable.SOURCE_ID.qualify(lv2tv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(l2lv)));
           SelectQuery inner = exec.createSelectQuery(ComponentRelationsTable.SOURCE_ID.compareEquals(ProjectsTable.PROJECT_ID))) {
        query.addSelect(ComponentRelationsTable.SOURCE_ID.qualify(l2lv), ComponentRelationsTable.TARGET_ID.qualify(l2lv));
        query.andWhere(TypeVersionsTable.TYPE_ID.compareEquals(fqnID), ComponentRelationsTable.TYPE.qualify(lv2tv).compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_TYPE_VERSION), ComponentRelationsTable.TYPE.qualify(l2lv).compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION));
        query.orderBy(ComponentRelationsTable.TARGET_ID.qualify(l2lv), true);
        
        inner.addSelect(ProjectsTable.PROJECT_ID, ProjectsTable.GROUP, ProjectsTable.VERSION, ProjectsTable.NAME);
        ConstantCondition<Integer> innerCond = ComponentRelationsTable.TARGET_ID.compareEquals();
        inner.andWhere(innerCond.and(ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.JAR_MATCHES_LIBRARY_VERSION)));
        inner.orderBy(ProjectsTable.GROUP, true);
        inner.orderBy(ProjectsTable.NAME, true);
        inner.orderBy(ProjectsTable.VERSION, true);

        int libraryCount = 0;
        Integer lastLibrary = null;
        StringBuilder temp = new StringBuilder();
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer libraryID = result.getResult(ComponentRelationsTable.SOURCE_ID.qualify(l2lv));
          Integer libraryVersionID = result.getResult(ComponentRelationsTable.TARGET_ID.qualify(l2lv));
          if (lastLibrary == null || !lastLibrary.equals(libraryID)) {
            if (lastLibrary != null) {
              temp.append("</ul></li>");
            }
            temp.append("<li><a href=\"./libraries?libraryID=").append(libraryID).append("\">").append("Library ").append(libraryID).append("</a><ul>");
            lastLibrary = libraryID;
            libraryCount++;
          }
          temp.append("<li><a href=\"./libraries?libraryVersionID=").append(libraryVersionID).append("\">").append("Library Version ").append(libraryID).append(".").append(libraryVersionID).append("</a>");

          innerCond.setValue(libraryVersionID);
          TypedQueryResult innerResult = inner.select();
          temp.append("<ul>");
          while (innerResult.next()) {
            Integer jarID = innerResult.getResult(ProjectsTable.PROJECT_ID);
            temp.append("<li><a href=\"./jars?jarID=").append(jarID).append("\">");
            appendJarName(innerResult, temp);
            temp.append("</a></li>");
          }
          temp.append("</ul>");
        }
        if (lastLibrary != null) {
          temp.append("</ul></li>");
        }
        temp.append("</ul>");
        html.append("<h4>" + libraryCount + " Libraries</h4>");
        html.append(temp.toString()); 
      }
    }

    // Jars
    try (SelectQuery query = exec.createSelectQuery(TypeVersionsTable.TYPE_VERSION_ID.compareEquals(ComponentRelationsTable.TARGET_ID), ComponentRelationsTable.SOURCE_ID.compareEquals(ProjectsTable.PROJECT_ID))) {
      query.addSelect(ProjectsTable.NAME, ProjectsTable.GROUP, ProjectsTable.VERSION, ProjectsTable.PROJECT_ID);
      query.andWhere(TypeVersionsTable.TYPE_ID.compareEquals(fqnID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.JAR_CONTAINS_TYPE_VERSION));
      query.orderBy(ProjectsTable.GROUP, true);
      query.orderBy(ProjectsTable.NAME, true);
      query.orderBy(ProjectsTable.VERSION, true);

      Integer jarCount = 0;
      StringBuilder temp = new StringBuilder();
      temp.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer jarID = result.getResult(ProjectsTable.PROJECT_ID);
        temp.append("<li><a href=\"./jars?jarID=").append(jarID).append("\">");
        appendJarName(result, temp);
        temp.append("</a></li>");
        jarCount++;
      }
      temp.append("</ul>");
      html.append("<h4>" + jarCount + " Jars</h4>");
      html.append(temp.toString());
    }

    { // Clusters
      QualifiedTable c2cv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable cv2tv = ComponentRelationsTable.TABLE.qualify("b");
      
      try (SelectQuery query = exec.createSelectQuery(TypeVersionsTable.TYPE_VERSION_ID.compareEquals(ComponentRelationsTable.TARGET_ID.qualify(cv2tv)), ComponentRelationsTable.SOURCE_ID.qualify(cv2tv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(c2cv)))) {
        query.setDistinct(true);
        query.addSelect(ComponentRelationsTable.SOURCE_ID.qualify(c2cv));
        query.andWhere(TypeVersionsTable.TYPE_ID.compareEquals(fqnID), ComponentRelationsTable.TYPE.qualify(c2cv).compareEquals(ComponentRelation.CLUSTER_CONTAINS_CLUSTER_VERSION), ComponentRelationsTable.TYPE.qualify(cv2tv).compareEquals(ComponentRelation.CLUSTER_VERSION_CONTAINS_TYPE_VERSION));
        query.orderBy(ComponentRelationsTable.SOURCE_ID.qualify(c2cv), true);
  
        html.append("<h4>Clusters</h4>");
        html.append("<ul>");
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer clusterID = result.getResult(ComponentRelationsTable.SOURCE_ID.qualify(c2cv));
          html.append("<li><a href=\"./clusters?clusterID=").append(clusterID).append("\">Cluster ").append(clusterID).append("</a></li>");
        }
        html.append("</ul>");
      }
    }

    // FQN Versions
    try (SelectQuery query = exec.createSelectQuery(TypeVersionsTable.TABLE)) {
      query.addSelect(TypeVersionsTable.TYPE_VERSION_ID);
      query.andWhere(TypeVersionsTable.TYPE_ID.compareEquals(fqnID));
      query.orderBy(TypeVersionsTable.TYPE_VERSION_ID, true);
      

      html.append("<h4>FQN Versions</h4>");
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer fqnVersionID = result.getResult(TypeVersionsTable.TYPE_VERSION_ID);
        html.append("<li><a href=\"./fqns?fqnVersionID=").append(fqnVersionID).append("\">FQN Version ").append(fqnVersionID).append("</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveFqnVersion(Integer fqnVersionID, StringBuilder html) {
    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.createSelectQuery(TypeVersionsTable.TYPE_ID.compareEquals(TypesTable.TYPE_ID))) {
      query.addSelect(TypesTable.FQN, TypesTable.TYPE_ID);
      query.andWhere(TypeVersionsTable.TYPE_VERSION_ID.compareEquals(fqnVersionID));
      
      TypedQueryResult result = query.select();
      if (result.next()) {
        String fqn = result.getResult(TypesTable.FQN);
        Integer fqnID = result.getResult(TypesTable.TYPE_ID);
        html.append("<p><a href=\"./\">main</a>/<a href=\"./fqns\">fqns</a>/<a href=\"./fqns?fqnID=").append(fqnID).append("\">").append(fqn).append("</a></p>");
        html.append("<h3>FQN ").append(fqnID).append(": ").append(fqn).append(" Version ").append(fqnVersionID).append("</h3>");
      }
    }

    { // Library versions
      QualifiedTable l2lv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable lv2tv = ComponentRelationsTable.TABLE.qualify("b");
      
      try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.SOURCE_ID.qualify(lv2tv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(l2lv)))) {
        query.addSelect(ComponentRelationsTable.SOURCE_ID.qualify(l2lv), ComponentRelationsTable.TARGET_ID.qualify(l2lv));
        query.andWhere(ComponentRelationsTable.TARGET_ID.qualify(lv2tv).compareEquals(fqnVersionID), ComponentRelationsTable.TYPE.qualify(lv2tv).compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_TYPE_VERSION), ComponentRelationsTable.TYPE.qualify(l2lv).compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION));
        query.orderBy(ComponentRelationsTable.TARGET_ID.qualify(l2lv), true);
        
  
        html.append("<h4>Library Version</h4>");
        html.append("<ul>");
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer libraryID = result.getResult(ComponentRelationsTable.SOURCE_ID.qualify(l2lv));
          Integer libraryVersionID = result.getResult(ComponentRelationsTable.TARGET_ID.qualify(l2lv));
          html.append("<li><a href=\"./libraries?libraryVersionID=").append(libraryVersionID).append("\">").append("Library ").append(libraryID).append(".").append(libraryVersionID).append("</a></li>");
        }
        html.append("</ul>"); 
      }
    }
    
    try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.SOURCE_ID.compareEquals(ProjectsTable.PROJECT_ID))) {
      query.addSelect(ProjectsTable.NAME, ProjectsTable.GROUP, ProjectsTable.VERSION, ProjectsTable.PROJECT_ID);
      query.andWhere(ComponentRelationsTable.TARGET_ID.compareEquals(fqnVersionID), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.JAR_CONTAINS_TYPE_VERSION));
      query.orderBy(ProjectsTable.GROUP, true);
      query.orderBy(ProjectsTable.NAME, true);
      query.orderBy(ProjectsTable.VERSION, true);
      
      // Jars
      StringBuilder temp = new StringBuilder();
      Integer jarCount = 0;
      TypedQueryResult result = query.select();
      while (result.next()) {
        Integer jarID = result.getResult(ProjectsTable.PROJECT_ID);
        temp.append("<li><a href=\"./jars?jarID=").append(jarID).append("\">");
        appendJarName(result, temp);
        temp.append("</a></li>");
        jarCount++;
      }
      html.append("<h4>").append(jarCount).append(" Jars</h4>");
      html.append("<ul>");
      html.append(temp.toString());
      html.append("</ul>");
    }
    
    {
      QualifiedTable c2cv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedColumn<Integer> clusterIDcol = ComponentRelationsTable.SOURCE_ID.qualify(c2cv);
      QualifiedColumn<Integer> clusterVersionIDcol = ComponentRelationsTable.TARGET_ID.qualify(c2cv);
      QualifiedTable cv2tv = ComponentRelationsTable.TABLE.qualify("b");
      
      try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.SOURCE_ID.qualify(cv2tv).compareEquals(clusterVersionIDcol))) {
        query.addSelect(clusterIDcol, clusterVersionIDcol);
        query.andWhere(ComponentRelationsTable.TARGET_ID.qualify(cv2tv).compareEquals(fqnVersionID), ComponentRelationsTable.TYPE.qualify(cv2tv).compareEquals(ComponentRelation.CLUSTER_VERSION_CONTAINS_TYPE_VERSION), ComponentRelationsTable.TYPE.qualify(c2cv).compareEquals(ComponentRelation.CLUSTER_CONTAINS_CLUSTER_VERSION));
        query.orderBy(clusterIDcol, true);
        query.orderBy(clusterVersionIDcol, true);
        
        // Clusters
        html.append("<h4>Cluster Versions</h4>");
        html.append("<ul>");
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer clusterID = result.getResult(clusterIDcol);
          Integer clusterVersionID = result.getResult(clusterVersionIDcol);
          html.append("<li><a href=\"./clusters?clusterVersionID=").append(clusterVersionID).append("\">").append(clusterID).append(".").append(clusterVersionID).append("</a></li>");
        }
        html.append("</ul>");
      }
    }
  }
  
  private void serveMavenArtifactList(StringBuilder html) {
    html.append("<p><a href=\"./\">main</a></p>");

    QueryExecutor exec = db.get().getExecutor();
    
    try (SelectQuery query = exec.createSelectQuery(ProjectsTable.TABLE)) {
      query.addSelect(ProjectsTable.GROUP, ProjectsTable.NAME);
      query.setDistinct(true);
      query.andWhere(ProjectsTable.PROJECT_TYPE.compareEquals(Project.MAVEN));
      query.orderBy(ProjectsTable.GROUP, true);
      query.orderBy(ProjectsTable.NAME, true);
      
      html.append("<ul>");
      TypedQueryResult result = query.select();
      while (result.next()) {
        String group = result.getResult(ProjectsTable.GROUP);
        String name = result.getResult(ProjectsTable.NAME);
        html.append("<li><a href=\"?group=").append(group).append("&artifact=").append(name).append("\">").append(group).append(".").append(name).append("</a></li>");
      }
      html.append("</ul>");
    }
  }
  
  private void serveMavenArtifact(String group, String artifact, StringBuilder html) {
    html.append("<p><a href=\"./\">main</a>/<a href=\"./maven\">maven</a></p>");
    
    QueryExecutor exec = db.get().getExecutor();
    
    html.append("<h3>").append(group).append(".").append(artifact).append("</h3>");
    
    {
      QualifiedTable j2lv = ComponentRelationsTable.TABLE.qualify("a");
      QualifiedTable l2lv = ComponentRelationsTable.TABLE.qualify("b");
      try (SelectQuery query = exec.createSelectQuery(ProjectsTable.PROJECT_ID.compareEquals(ComponentRelationsTable.SOURCE_ID.qualify(j2lv)), ComponentRelationsTable.TARGET_ID.qualify(j2lv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(l2lv)))) {
        query.addSelect(ProjectsTable.VERSION, ProjectsTable.PROJECT_ID, ComponentRelationsTable.SOURCE_ID.qualify(l2lv), ComponentRelationsTable.TARGET_ID.qualify(l2lv));
        query.andWhere(ProjectsTable.GROUP.compareEquals(group), ProjectsTable.NAME.compareEquals(artifact), ComponentRelationsTable.TYPE.qualify(j2lv).compareEquals(ComponentRelation.JAR_MATCHES_LIBRARY_VERSION), ComponentRelationsTable.TYPE.qualify(l2lv).compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION));
        query.orderBy(ComponentRelationsTable.TARGET_ID.qualify(l2lv), true);
        
        StringBuilder temp = new StringBuilder();
        int jarCount = 0;
        Integer previousLibraryID = null;
        Integer previousLibraryVersionID = null;
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer libraryID = result.getResult(ComponentRelationsTable.SOURCE_ID.qualify(l2lv));
          Integer libraryVersionID = result.getResult(ComponentRelationsTable.TARGET_ID.qualify(l2lv));
          Integer jarID = result.getResult(ProjectsTable.PROJECT_ID);
          String version = result.getResult(ProjectsTable.VERSION);
          
          if (!libraryID.equals(previousLibraryID)) {
            if (previousLibraryVersionID != null) {
              temp.append("</ul></li>");
              previousLibraryVersionID = null;
            }
            if (previousLibraryID != null) {
              temp.append("</ul></li>");
            }
            temp.append("<li><a href=\"./libraries?libraryID=").append(libraryID).append("\">").append("Library ").append(libraryID).append("</a><ul>");
            previousLibraryID = libraryID;
          }
          
          if (!libraryVersionID.equals(previousLibraryVersionID)) {
            if (previousLibraryVersionID != null) {
              temp.append("</ul></li>");
            }
            temp.append("<li><a href=\"./libraries?libraryVersionID=").append(libraryVersionID).append("\">").append("Library Version ").append(libraryVersionID).append("</a><ul>");
            previousLibraryVersionID = libraryVersionID;
          }
          
          temp.append("<li><a href=\"./jars?jarID=").append(jarID).append("\">").append(group).append(".").append(artifact).append(" (").append(version).append(")</a></li>");
          jarCount++;
        }
        if (previousLibraryVersionID != null) {
          temp.append("</ul></li>");
        }
         
        if (previousLibraryID != null) {
          temp.append("</ul></li>");
        }
        
        html.append("<h4>").append(jarCount).append(" Jars</h4>");
        html.append("<ul>");
        html.append(temp.toString());
        html.append("</ul>");
      }
    }
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    StringBuilder html = new StringBuilder(
        "<html>" +
        "<head><title>Sourcerer Artifact Repository</title></head>" +
        "<body>");
    switch (request.getPathInfo()) {
      case "/libraries":
        {
          Integer libraryID = ServletUtils.getIntValue(request, "libraryID");
          if (libraryID == null) {
            Integer libraryVersionID = ServletUtils.getIntValue(request, "libraryVersionID");
            if (libraryVersionID == null) {
              serveLibraryList(html);
            } else {
              serveLibraryVersion(libraryVersionID, html);
            }
            
          } else {
            serveLibrary(libraryID, html);
          }
        }
        break;
      case "/clusters":
        {
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
        }
        break;
      case "/jars":
        {
          Integer jarID = ServletUtils.getIntValue(request, "jarID");
          if (jarID == null) {
            serveJarList(html);
          } else {
            serveJar(jarID, html);
          }
        }
        break;
      case "/fqns":
        {
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
        }
        break;
      case "/maven":
        {
          String group = request.getParameter("group");
          String artifact = request.getParameter("artifact");
          if (group == null || artifact == null) {
            serveMavenArtifactList(html);
          } else {
            serveMavenArtifact(group, artifact, html);
          }
        }
        break;
      default:
        serveMain(html);
    }
    html.append("</body></html>");
    ServletUtils.writeString(response, null, html.toString(), true);
  }
}
