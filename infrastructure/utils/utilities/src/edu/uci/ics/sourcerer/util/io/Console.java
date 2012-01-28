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
package edu.uci.ics.sourcerer.util.io;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Console implements AutoCloseable {
  private BufferedReader br;
  private PrintWriter pw;
  
  private Console() {
    br = new BufferedReader(new InputStreamReader(System.in));
    pw = new PrintWriter(System.out);
  }
  
  public static Console create() {
    return new Console();
  }
  
  public String readLine(String format, Object ... args) {
    try {
      pw.format(format, args);
      pw.flush();
      return br.readLine();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading line.", e);
      return null;
    }
  }
  
  @Override
  public void close() {
    IOUtils.close(br, pw);
  }
}
