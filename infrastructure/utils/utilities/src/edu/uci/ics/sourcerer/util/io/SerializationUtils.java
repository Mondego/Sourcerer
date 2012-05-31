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

import java.util.Scanner;
import java.util.logging.Level;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SerializationUtils {
  public static String serializeString(String val) {
    if (val == null) {
      return "null";
    } else if (val.equals("null")) {
      logger.log(Level.SEVERE, "null collission");
    }
    int idx = val.indexOf(' ');
    if (idx == -1) {
      return val;
    } else {
      LineBuilder result = new LineBuilder();
      result.append(val.substring(0, idx++));
      int parts = 1;
      while (idx > 0) {
        parts++;
        int next = val.indexOf(' ', idx);
        if (next == -1) {
          result.append(val.substring(idx));
        } else {
          result.append(val.substring(idx, next));
        }
        idx = next;
        while (idx > 0 && val.charAt(idx) == ' ') {
          if (++idx >= val.length()) {
            idx = -1;
          }
        }
      }
      return parts + " " + result.toString();
    }
  }
  
  public static String deserializeString(Scanner scanner) {
    if (scanner.hasNextInt()) {
      LineBuilder result = new LineBuilder();
      for (int i = scanner.nextInt(); i > 0; i--) {
        if (scanner.hasNext()) {
          result.append(scanner.next());
        } else {
          logger.severe("More input expected for: " + result.toString());
        }
      }
      return result.toString();
    } else {
      String next = scanner.next();
      if ("null".equals(next)) {
        return null;
      } else {
        return next;
      }
    }
  }
  
  public static Integer deserializeInteger(Scanner scanner) {
    if (scanner.hasNextInt()) {
      return scanner.nextInt();
    } else {
      String next = scanner.next();
      if ("null".equals(next)) {
        return null;
      } else {
        logger.log(Level.SEVERE, "Int expected by deserialization, instead got " + next);
        return null;
      }
    }
  }
}
