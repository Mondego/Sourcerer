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
package edu.uci.ics.sourcerer.tools.link.downloader;

import java.io.File;
import java.io.IOException;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.PServerConnection;

import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CVS {
  public static boolean checkout(String url, File target) {
    final TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Checking out " + url + " to " + target.getPath());
    CVSRoot root = CVSRoot.parse(url);
    
    PServerConnection conn = new PServerConnection(root);
    try {
      conn.open();
      Client client = new Client(conn, new StandardAdminHandler());
      client.setLocalPath(target.getAbsolutePath());

      CheckoutCommand command = new CheckoutCommand();
      command.setModule(".");
      
      GlobalOptions options = new GlobalOptions();
      boolean success = client.executeCommand(command, options);
      task.finish();
      return success;
    } catch (AuthenticationException | CommandException e) {
      task.exception(e);
      return false;
    } finally {
      try {
        conn.close();
      } catch (IOException e) {}
    }
  }
}
