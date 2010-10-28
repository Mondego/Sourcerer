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
package edu.uci.ics.sourcerer.server.file;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uci.ics.sourcerer.db.tools.FileAccessor;
import edu.uci.ics.sourcerer.db.tools.FileAccessor.Result;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.server.ServletUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("serial")
public class FileServer extends HttpServlet {
  @Override
  public void init() throws ServletException {
    PropertyManager.PROPERTIES_STREAM.setValue(getServletContext().getResourceAsStream("/WEB-INF/lib/file-server.properties"));
    PropertyManager.initializeProperties();
  }
  
  @Override
  public void destroy() {
    logger.log(Level.INFO, "Destroying");
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // Should the files download or be shown in browser?
    boolean download = "t".equals(request.getParameter("dl"));
    
    Result result = null;
    
    // Lookup by projectID
    String projectID = request.getParameter("projectID");
    if (projectID != null) {
      result = FileAccessor.lookupResultByProjectID(projectID);
    } else {
      // Lookup by fileID
      String fileID = request.getParameter("fileID");
      if (fileID != null) {
        result = FileAccessor.lookupResultByFileID(fileID);
      } else {
        // Lookup by entityID
        String entityID = request.getParameter("entityID");
        if (entityID != null) {
          result = FileAccessor.lookupResultByEntityID(entityID);
        } else {
          // Lookup by relationID
          String relationID = request.getParameter("relationID");
          if (relationID != null) {
            result = FileAccessor.lookupResultByRelationID(relationID);
          } else {
            // Lookup by commentID
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
    } else {
      if (result.success()) {
        ServletUtils.writeByteArray(response, download ? result.getName() : null, result.getResult());
      } else {
        ServletUtils.writeErrorMsg(response, result.getErrorMessage());
      }
    }
  }
}
