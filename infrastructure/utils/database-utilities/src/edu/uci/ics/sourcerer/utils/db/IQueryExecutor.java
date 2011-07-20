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

import edu.uci.ics.sourcerer.utils.db.sql.IComparisonCondition;
import edu.uci.ics.sourcerer.utils.db.sql.IQueryResult;
import edu.uci.ics.sourcerer.utils.db.sql.ITypedQueryResult;
import edu.uci.ics.sourcerer.utils.db.sql.ISelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.IStatement;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.sql.ISetStatement;
import edu.uci.ics.sourcerer.utils.db.sql.ITable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public interface IQueryExecutor extends Closeable {
  public ITableLocker getTableLocker();
  public IRowInserter getInFileInserter(File tempDir, DatabaseTable table);

  // Raw Updates
  public void executeUpdate(String sql);
  public String executeUpdateWithKey(String sql);
  public IQueryResult executeUpdateWithKeys(String sql);

  // Raw Executes
  public String executeSingle(String sql);
  public int exucuteSingleInt(String sql);
  public IQueryResult execute(String sql);
  
  // Typed Updates
  public IStatement makeCreateTableStatement(DatabaseTable table);
  public IStatement makeDropTableStatement(DatabaseTable ... tables);
  public ISetStatement makeSetStatement(DatabaseTable table);
  
  // Typed Executes
  public ISelectQuery makeSelectQuery(ITable fromTable);
  public ISelectQuery makeSelectQuery(IComparisonCondition ... joinConditions);
}
