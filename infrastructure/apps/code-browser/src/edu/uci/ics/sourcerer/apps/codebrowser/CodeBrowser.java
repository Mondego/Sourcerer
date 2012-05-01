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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uci.ics.sourcerer.tools.java.db.exported.FileAccessor;
import edu.uci.ics.sourcerer.tools.java.db.exported.FileAccessor.Link;
import edu.uci.ics.sourcerer.tools.java.db.exported.FileAccessor.Result;
import edu.uci.ics.sourcerer.tools.java.highlighter.TagInfo;
import edu.uci.ics.sourcerer.tools.java.highlighter.TagType;
import edu.uci.ics.sourcerer.tools.java.highlighter.SyntaxHighlighter;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.io.arguments.ArgumentManager;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.servlet.ServletUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("serial")
public class CodeBrowser extends HttpServlet {
  @Override
  public void init() throws ServletException {
    ArgumentManager.PROPERTIES_STREAM.setValue(getServletContext().getResourceAsStream("/WEB-INF/lib/code-browser.properties"));
    JavaRepositoryFactory.INPUT_REPO.permit();
    DatabaseConnectionFactory.DATABASE_URL.permit();
    DatabaseConnectionFactory.DATABASE_USER.permit();
    DatabaseConnectionFactory.DATABASE_PASSWORD.permit();
    ArgumentManager.initializeProperties();
  }
  
  @Override
  public void destroy() {
//    logger.log(Level.INFO, "Destroying");
//    FileAccessor.destroy();
//    logger.log(Level.INFO, "Done Destroying");
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
    
    Integer projectID = getIntValue(request, "projectID");
    if (projectID != null) {
    } else {
      Integer fileID = getIntValue(request, "fileID");
      if (fileID != null) {
        result = FileAccessor.lookupResultByFileID(fileID);
      } else {
        Integer entityID = getIntValue(request, "entityID");
        if (entityID != null) {
          result = FileAccessor.lookupResultByEntityID(entityID);
        } else {
          Integer relationID = getIntValue(request, "relationID");
          if (relationID != null) {
            result = FileAccessor.lookupResultByRelationID(relationID);
          } else {
            String commentID = request.getParameter("commentID");
            if (commentID != null) {
              result = FileAccessor.lookupResultByCommentID(relationID);
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
      String code = new String(result.getFullResult());
      Integer fileID = result.getFileID();
      
      TagInfo links = TagInfo.make();
      
      for (Link link : FileAccessor.getImportsByFileID(fileID)) {
        links.addLinkLocation(TagType.IMPORT_LINK, link.getOffset(), link.getLength(), "link", "?entityID=" + link.getEntityID(), null);
      }
      
      Map<Integer, Integer> declaredFields = new HashMap<>();
      
      for (Link field : FileAccessor.getFieldsByFileID(fileID)) {
        links.addColorLocation(field.getOffset(), field.getLength(), "field");
        declaredFields.put(field.getOffset(), field.getLength());
      }
      
      for (Link link : FileAccessor.getRelationLinksByFileID(fileID)) {
        if (link.getOffset() != null) {
          if (link.getType() == Relation.USES) {
//            if (join.getEntity().getType().isInternalMeaningful()) {
            links.addLinkLocation(TagType.TYPE_LINK, link.getOffset(), link.getLength(), "link", "?entityID=" + link.getEntityID(), link.getFqn());
//            }
          } else if (link.getType() == Relation.READS) {
            links.addLinkLocation(TagType.FIELD_LINK, link.getOffset(), link.getLength(), "field", "?entityID=" + link.getEntityID(), link.getFqn());
          } else if (link.getType() == Relation.WRITES) {
            Integer len = declaredFields.get(link.getOffset());
            if (len == null || !len.equals(link.getLength())) {
              links.addLinkLocation(TagType.FIELD_LINK, link.getOffset(), link.getLength(), "field", "?entityID=" + link.getEntityID(), link.getFqn());
            }
          } else if (link.getType() == Relation.CALLS) {
            int off = link.getOffset();
            while (!Character.isJavaIdentifierPart(code.charAt(off))) {
              off++;
            }
            int paren = code.indexOf(')', off);
            links.addLinkLocation(TagType.METHOD_LINK, off, paren - off, "method", "?entityID=" + link.getEntityID(), link.getFqn());
          }
        }
      }
      
      if (result.getOffset() != null) {
        links.setMainAnchorLocation(result.getOffset());
      }
      
      StringBuilder builder = new StringBuilder();
      builder.append("<html>\n");
      builder.append("<head>");
      builder.append("<title>").append(result.getName()).append("</title>");
      builder.append("<style>" +
      		"body { font-family: monospace; } " +
      		"a.link:link { color: black; text-decoration: none; } " +
          "a.link:visited { color: black; text-decoration: none; } " +
          "a.link:hover { color: black; text-decoration: underline; } " +
          "a.method:link { color: black; font-style: italic; text-decoration: none; } " +
          "a.method:visited { color: black; font-style: italic; text-decoration: none; } " +
          "a.method:hover { color: black; font-style: italic; text-decoration: underline; } " +
      		".comment { color: #3F7F5F; } " +
      		".javadoc-comment { color: #7F7F9F; } " +
      		".keyword { color: #7F0055; font-weight:bold; } " +
      		".string { color: #2A00FF; } " +
      		".character { color: #2A00FF; } " +
      		".annotation { color: #646464; font-weight: bold; } " +
      		".annotation a.link:link { color: #646464; text-decoration: none; } " +
      		".annotation a.link:visited { color: #646464; text-decoration: none; } " +
      		".annotation a.link:hover { color: #646464; text-decoration: underline; } " +
      		
      		".javadoc-tag { color: #7F9FBF; font-weight: bold; } " +
      		".field { color: #0000C0; } " +
      		"a.field:link { color: #0000C0; text-decoration: none; } " +
      		"a.field:visited { color: #0000C0; text-decoration: none; } " +
      		"a.field:hover { color: #0000C0; text-decoration: underline; } " +
      		"</style>");
      builder.append("</head>");
      builder.append("<body>");
      builder.append(SyntaxHighlighter.highlightSyntax(code, links));
      builder.append("<script>\n" +
          "document.getElementById('main').scrollIntoView(true);\n" +
          "</script>");
      builder.append("</body>");
      builder.append("</html>");

      ServletUtils.writeString(response, download ? result.getName() : null, builder.toString(), true);
    }
  }
}
