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
package edu.uci.ics.sourcerer.db.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.uci.ics.sourcerer.util.Pair;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class ResultTranslator <T> {
  public String getTable() {
    return null;
  }
  
  public String getSelect() {
    return null;
  }
  
  public abstract T translate(ResultSet result) throws SQLException;
  
  public static final ResultTranslator<String> SIMPLE_RESULT_TRANSLATOR = new ResultTranslator<String>() {
    public String translate(ResultSet result) throws SQLException {
      return result.getString(1);
    }
  };
  
  public static final ResultTranslator<Pair<String, String>> PAIR_RESULT_TRANSLATOR = new ResultTranslator<Pair<String, String>>() {
    public Pair<String, String> translate(ResultSet result) throws SQLException {
      return new Pair<String, String>(result.getString(1), result.getString(2));
    }
  };  
}

