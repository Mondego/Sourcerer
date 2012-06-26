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
package edu.uci.ics.sourcerer.services.file;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uci.ics.sourcerer.services.file.adapter.FileAdapter;
import edu.uci.ics.sourcerer.services.file.adapter.FileAdapter.Result;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.io.arguments.ArgumentManager;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.servlet.ServletUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("serial")
public class FileServer extends HttpServlet {
  @Override
  public void init() throws ServletException {
    ArgumentManager.PROPERTIES_STREAM.setValue(getServletContext().getResourceAsStream("/WEB-INF/lib/file-server.properties"));
    JavaRepositoryFactory.INPUT_REPO.permit();
    DatabaseConnectionFactory.DATABASE_URL.permit();
    DatabaseConnectionFactory.DATABASE_USER.permit();
    DatabaseConnectionFactory.DATABASE_PASSWORD.permit();
    ArgumentManager.initializeProperties();
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
    Integer projectID = ServletUtils.getIntValue(request, "projectID");
    if (projectID != null) {
      result = FileAdapter.lookupResultByProjectID(projectID);
    } else {
      // Lookup by fileID
      Integer fileID = ServletUtils.getIntValue(request, "fileID");
      if (fileID != null) {
        result = FileAdapter.lookupResultByFileID(fileID);
      } else {
        // Lookup by entityID
        Integer entityID = ServletUtils.getIntValue(request, "entityID");
        if (entityID != null) {
          result = FileAdapter.lookupResultByEntityID(entityID);
        } else {
          // Lookup by relationID
          Integer relationID = ServletUtils.getIntValue(request, "relationID");
          if (relationID != null) {
            result = FileAdapter.lookupResultByRelationID(relationID);
          } else {
            // Lookup by commentID
            String commentID = request.getParameter("commentID");
            if (commentID != null) {
              result = FileAdapter.lookupResultByCommentID(relationID);
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
