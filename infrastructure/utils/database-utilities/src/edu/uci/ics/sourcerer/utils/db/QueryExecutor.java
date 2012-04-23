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
package edu.uci.ics.sourcerer.utils.db;

import java.io.Closeable;
import java.io.File;

import edu.uci.ics.sourcerer.utils.db.sql.ComparisonCondition;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.sql.DeleteStatement;
import edu.uci.ics.sourcerer.utils.db.sql.QueryResult;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.SetStatement;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public interface QueryExecutor extends Closeable {
  public TableLocker getTableLocker();
  public BatchInserter makeInFileInserter(File tempDir, DatabaseTable table);

  // Raw Updates
  public void executeUpdate(String sql);
  public String executeUpdateWithKey(String sql);
  public QueryResult executeUpdateWithKeys(String sql);

  // Raw Executes
  public String executeSingle(String sql);
  public int exucuteSingleInt(String sql);
  public QueryResult execute(String sql);
  
  // Typed Updates
  public void createTable(DatabaseTable table);
  public void createTables(DatabaseTable ... tables);
  public void dropTables(DatabaseTable ... tables);
  public void insert(Insert insert);
  public Integer insertWithKey(Insert insert);
  public SetStatement createSetStatement(DatabaseTable table);
  public DeleteStatement createDeleteStatement(DatabaseTable table);
  
  // Typed Executes
  public SelectQuery createSelectQuery(DatabaseTable fromTable);
  public SelectQuery createSelectQuery(ComparisonCondition ... joinConditions);
}
