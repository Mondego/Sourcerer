/*
 * Sourcerer: An infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package edu.uci.ics.sourcerer.ml.db;

import javax.sql.DataSource;


/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 1, 2009
 *
 */
public class MySqlTanimotoCoefficientUserSimilarity extends AbstractJDBCUserSimilarity {
	
//	public MySqlJDBCUserSimilarity() throws TasteException {
//	    this(DEFAULT_DATASOURCE_NAME);
//	 }
//	
//	public MySqlJDBCUserSimilarity(String dataSourceName) throws TasteException {
//	    this(lookupDataSource(dataSourceName));
//	  }
	
	public MySqlTanimotoCoefficientUserSimilarity(DataSource dataSource) {
	    this(dataSource,
	         DEFAULT_SIMILARITY_TABLE,
	         DEFAULT_USER_A_ID_COLUMN,
	         DEFAULT_USER_B_ID_COLUMN,
	         DEFAULT_SIMILARITY_COLUMN);
	  }

	 public MySqlTanimotoCoefficientUserSimilarity(DataSource dataSource,
	                                 String similarityTable,
	                                 String userAIDColumn,
	                                 String userBIDColumn,
	                                 String similarityColumn) {
	    super(dataSource,
	          similarityTable,
	          userAIDColumn,
	          userBIDColumn,
	          similarityColumn,
	          "SELECT " + similarityColumn + " FROM " + similarityTable + " WHERE " + userAIDColumn + "=? AND " +
	          userBIDColumn + "=?");
	  }

	  @Override
	  protected int getFetchSize() {
	    // Need to return this for MySQL Connector/J to make it use streaming mode
	    return Integer.MIN_VALUE;
	  }
}
