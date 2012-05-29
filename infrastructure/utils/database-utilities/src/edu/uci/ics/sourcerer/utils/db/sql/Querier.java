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
package edu.uci.ics.sourcerer.utils.db.sql;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;
import java.io.Closeable;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class Querier<Input, Result> implements Closeable {
  protected QueryExecutor exec;
  protected Query query;
  private boolean initialized = false;
  
  public Querier(QueryExecutor exec) {
    this.exec = exec;
  }
  
  public final Result select(Input input) {
    if (!initialized) {
      try {
        query = initialize();
        initialized = true;
      } catch (RuntimeException e) {
        logger.log(Level.SEVERE, "Unable to initialize querier.", e);
        return null;
      } 
    }
    return selectHelper(input);
  }
  
  public abstract Query initialize();
  
  protected abstract Result selectHelper(Input input);
  
  @Override
  public void close() {
    if (initialized) {
      IOUtils.close(query);
      initialized = false;
    }
  }
}
