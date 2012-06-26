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
package edu.uci.ics.sourcerer.services.slice;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uci.ics.sourcerer.services.slicer.SlicerFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.io.arguments.ArgumentManager;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.servlet.ServletUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
@SuppressWarnings("serial")
public class SliceServer extends HttpServlet {
  @Override
  public void init() throws ServletException {
    ArgumentManager.PROPERTIES_STREAM.setValue(getServletContext().getResourceAsStream("/WEB-INF/lib/slice-server.properties"));
    JavaRepositoryFactory.INPUT_REPO.permit();
    SlicerFactory.FILE_SERVER_URL.permit();
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
    Integer entityID = ServletUtils.getIntValue(request, "entityID");
    if (entityID != null) {
      byte[] result = SlicerFactory.createSlicer().slice(Collections.singleton(entityID)).toZipFile();
      if (result == null) {
        ServletUtils.writeErrorMsg(response, "Unable to slice: " + entityID);
      } else {
        ServletUtils.writeByteArray(response, entityID + ".zip", result);
      }
    } else {
      ServletUtils.writeErrorMsg(response, "Please provide an entityID");
    }
  }
}
