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


import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.common.IOUtils;
import org.apache.mahout.cf.taste.impl.common.jdbc.AbstractJDBCComponent;
import org.apache.mahout.cf.taste.impl.model.jdbc.ConnectionPoolDataSource;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 1, 2009
 *
 * A {@link UserSimilarity} which draws pre-computed item-item similarities from
 * a database table via JDBC.
 */
public class AbstractJDBCUserSimilarity extends AbstractJDBCComponent implements UserSimilarity{
	private static final Logger log = LoggerFactory.getLogger(AbstractJDBCUserSimilarity.class);

	  static final String DEFAULT_SIMILARITY_TABLE = "similarity_tanimoto";
	  static final String DEFAULT_USER_A_ID_COLUMN = "lhs_eid";
	  static final String DEFAULT_USER_B_ID_COLUMN = "rhs_eid";
	  static final String DEFAULT_SIMILARITY_COLUMN = "similarity";

	  private final DataSource dataSource;
	  private final String similarityTable;
	  private final String userAIDColumn;
	  private final String userBIDColumn;
	  private final String similarityColumn;
	  private final String getUserUserSimilaritySQL;

	  protected AbstractJDBCUserSimilarity(DataSource dataSource,
	                                       String getItemItemSimilaritySQL) {
	    this(dataSource,
	         DEFAULT_SIMILARITY_TABLE,
	         DEFAULT_USER_A_ID_COLUMN,
	         DEFAULT_USER_B_ID_COLUMN,
	         DEFAULT_SIMILARITY_COLUMN,
	         getItemItemSimilaritySQL);
	  }

	  protected AbstractJDBCUserSimilarity(DataSource dataSource,
	                                       String similarityTable,
	                                       String userAIDColumn,
	                                       String userBIDColumn,
	                                       String similarityColumn,
	                                       String getUserUserSimilaritySQL) {
	    checkNotNullAndLog("similarityTable", similarityTable);
	    checkNotNullAndLog("userAIDColumn", userAIDColumn);
	    checkNotNullAndLog("userBIDColumn", userBIDColumn);
	    checkNotNullAndLog("similarityColumn", similarityColumn);

	    checkNotNullAndLog("getUserUserSimilaritySQL", getUserUserSimilaritySQL);

	    if (!(dataSource instanceof ConnectionPoolDataSource)) {
	      log.warn("You are not using ConnectionPoolDataSource. Make sure your DataSource pools connections " +
	          "to the database itself, or database performance will be severely reduced.");
	    }

	    this.dataSource = dataSource;
	    this.similarityTable = similarityTable;
	    this.userAIDColumn = userAIDColumn;
	    this.userBIDColumn = userBIDColumn;
	    this.similarityColumn = similarityColumn;
	    this.getUserUserSimilaritySQL = getUserUserSimilaritySQL;
	  }

	  protected String getSimilarityTable() {
	    return similarityTable;
	  }

	  protected String getUserAIDColumn() {
	    return userAIDColumn;
	  }

	  protected String getUserBIDColumn() {
	    return userBIDColumn;
	  }

	  protected String getSimilarityColumn() {
	    return similarityColumn;
	  }

	  @Override
	  public double userSimilarity(long userID1, long userID2) throws TasteException {

	    if (userID1 == userID2) {
	      return 1.0;
	    }
	    // Order as smaller - larger
	    if (userID1 > userID2) {
	      long temp = userID1;
	      userID1 = userID2;
	      userID2 = temp;
	    }

	    Connection conn = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;

	    try {
	      conn = dataSource.getConnection();
	      stmt = conn.prepareStatement(getUserUserSimilaritySQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	      stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
	      stmt.setFetchSize(getFetchSize());
	      stmt.setLong(1, userID1);
	      stmt.setLong(2, userID2);

	      log.debug("Executing SQL query: {}", getUserUserSimilaritySQL);
	      rs = stmt.executeQuery();

	      if (rs.next()) {
	        return rs.getDouble(1);
	      } else {
	        throw new NoSuchItemException();
	      }

	    } catch (SQLException sqle) {
	      log.warn("Exception while retrieving user", sqle);
	      throw new TasteException(sqle);
	    } finally {
	      IOUtils.quietClose(rs, stmt, conn);
	    }
	  }

	  @Override
	  public void refresh(Collection<Refreshable> alreadyRefreshed) {
	    // do nothing
	  }

	@Override
	public void setPreferenceInferrer(PreferenceInferrer inferrer) {
		throw new UnsupportedOperationException();
	}

}

// Parts copied from - 
//  org.apache.mahout.cf.taste.impl.similarity.jdbc.AbstractJDBCItemSimilarity