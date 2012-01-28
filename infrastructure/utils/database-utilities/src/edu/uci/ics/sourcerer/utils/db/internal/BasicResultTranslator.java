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
package edu.uci.ics.sourcerer.utils.db.internal;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Pair;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class BasicResultTranslator <T> {
  public abstract T translate(ResultSet result) throws SQLException;
  
  public static final BasicResultTranslator<String> SIMPLE_STRING_TRANSLATOR = new BasicResultTranslator<String>() {
    public String translate(ResultSet result) throws SQLException {
      return result.getString(1);
    }
  };
  
  public static final BasicResultTranslator<Integer> SIMPLE_INT_TRANSLATOR = new BasicResultTranslator<Integer>() {
    public Integer translate(ResultSet result) throws SQLException {
      String val = result.getString(1);
      if (val == null) {
        return null;
      } else {
        try {
          return Integer.valueOf(val);
        } catch (NumberFormatException e) {
          logger.log(Level.SEVERE, val + " is not an integer");
          return null;
        }
      }
    }
  };
  
  public static final BasicResultTranslator<Pair<String, String>> PAIR_RESULT_TRANSLATOR = new BasicResultTranslator<Pair<String, String>>() {
    public Pair<String, String> translate(ResultSet result) throws SQLException {
      return new Pair<String, String>(result.getString(1), result.getString(2));
    }
  };  
}

