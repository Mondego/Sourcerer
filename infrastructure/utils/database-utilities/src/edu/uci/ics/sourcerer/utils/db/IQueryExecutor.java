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
import java.util.Collection;

import edu.uci.ics.sourcerer.utils.db.sql.ISelectFromClause;
import edu.uci.ics.sourcerer.utils.db.sql.ISetClause;
import edu.uci.ics.sourcerer.utils.db.sql.ITable;
import edu.uci.ics.sourcerer.utils.db.sql.IWhereClause;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public interface IQueryExecutor extends Closeable {
  public ITableLocker getTableLocker();
  public IRowInserter getInFileInserter(File tempDir, ITable table);

  // Raw Updates
  public void executeUpdate(String sql);
  public IQueryResult executeUpdateWithKeys(String sql);
  public String executeUpdateWithKey(String sql);
  
  // Typed Updates
  public void executeUpdate(ITable table, ISetClause set, IWhereClause where);
  public void deleteRows(ITable table, IWhereClause where);
  
  // Raw Executes
  public String executeSingle(String sql);
  public int exucuteSingleInt(String sql);
  
  // Typed Executes
  public int getRowCount(ITable table, IWhereClause where);
  public <T> T selectSingle(ITable table, IColumn<T> column, IWhereClause where);
  public <T> Collection<T> select(ITable table, ISelectFromClause from, IWhereClause where);
}
