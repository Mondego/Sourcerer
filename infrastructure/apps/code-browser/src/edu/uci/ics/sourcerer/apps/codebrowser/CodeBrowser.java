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
package edu.uci.ics.sourcerer.apps.codebrowser;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uci.ics.sourcerer.db.tools.FileAccessor;
import edu.uci.ics.sourcerer.db.tools.FileAccessor.Result;
import edu.uci.ics.sourcerer.model.db.RelationDB;
import edu.uci.ics.sourcerer.tools.java.highlighter.LinkLocationSet;
import edu.uci.ics.sourcerer.tools.java.highlighter.SyntaxHighlighter;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.server.ServletUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("serial")
public class CodeBrowser extends HttpServlet {
  @Override
  public void init() throws ServletException {
    PropertyManager.PROPERTIES_STREAM.setValue(getServletContext().getResourceAsStream("/WEB-INF/lib/code-browser.properties"));
    PropertyManager.initializeProperties();
  }
  
  @Override
  public void destroy() {
    logger.log(Level.INFO, "Destroying");
    FileAccessor.destroy();
    logger.log(Level.INFO, "Done Destroying");
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
    // Should the files download or be shown in browser?
    boolean download = "t".equals(request.getParameter("dl"));
    
    Result result = null;
    Integer fileID = null;
    
    Integer projectID = getIntValue(request, "projectID");
    if (projectID != null) {
//      result = FileAccessor.lookupResultByProjectID(projectID);
    } else {
      fileID = getIntValue(request, "fileID");
      if (fileID != null) {
        result = FileAccessor.lookupResultByFileID(fileID);
      } else {
        Integer entityID = getIntValue(request, "entityID");
        if (entityID != null) {
//          result = FileAccessor.lookupResultByEntityID(entityID);
        } else {
          Integer relationID = getIntValue(request, "relationID");
          if (relationID != null) {
//            result = FileAccessor.lookupResultByRelationID(relationID);
          } else {
            String commentID = request.getParameter("commentID");
            if (commentID != null) {
//              result = FileAccessor.lookupResultByCommentID(relationID);
            }
          }
        }
      }
    }

    if (result == null) {
      ServletUtils.writeErrorMsg(response, "Invalid action");
    } else if (!result.success()) {
      ServletUtils.writeErrorMsg(response, result.getErrorMessage());
    } else {
      LinkLocationSet links = LinkLocationSet.make();
      
      for (RelationDB relation : FileAccessor.getDisplayRelations(fileID)) {
        if (relation.getOffset() != null) {
          links.addLinkLocation(relation.getOffset(), relation.getLength(), "?entityID=" + relation.getRhsEid());
        }
      }
      
      StringBuilder builder = new StringBuilder();
      builder.append("<html>\n");
      builder.append("<head>");
      builder.append("<title>").append(result.getName()).append("</title>");
      builder.append("<style>" +
      		"body { font-family: monospace; } " +
      		".comment { color: #3F7F5F; } " +
      		".javadoc-comment { color: #7F7F9F; } " +
      		".keyword { color: #7F0055; font-weight:bold; } " +
      		".string { color: #2A00FF; } " +
      		".character { color: #2A00FF; } " +
      		".annotation { color: #646464; font-weight: bold; } " +
      		".javadoc-tag { color: #7F9FBF; font-weight: bold; }" +
      		"</style>");
      builder.append("</head>");
      builder.append("<body>");
      builder.append(SyntaxHighlighter.highlightSyntax(new String(result.getResult()), links));
      builder.append("</body>");
      builder.append("</html>");

      ServletUtils.writeString(response, download ? result.getName() : null, builder.toString(), true);
    }
  }
}
