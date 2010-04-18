package edu.uci.ics.sourcerer.db.util;
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

import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 2, 2009
 *
 */
public class JdbcDataSource {

	// TODO enable logging, fix exceptions
	
	// private static final Logger LOG = Logger.getLogger(JdbcDataSource.class
	// .getName());

	private Callable<Connection> factory;

	private long connLastUsed = 0;

	private Connection conn;

	private HashMap<String, Integer> fieldNameVsType = new HashMap<String, Integer>();

	private boolean convertType = false;

	private int batchSize = FETCH_SIZE;

	public void init(Properties initProps) {
		// Object o = initProps.get(CONVERT_TYPE);
		//    
		// if (o != null)
		// convertType = Boolean.parseBoolean(o.toString());

		createConnectionFactory(initProps);

		String bsz = initProps.getProperty("batchSize");
		if (bsz != null) {
			try {
				batchSize = Integer.parseInt(bsz);
				if (batchSize == -1)
					batchSize = Integer.MIN_VALUE;
			} catch (NumberFormatException e) {
				// LOG.log(Level.WARNING, "Invalid batch size: " + bsz);
			}
		}


	}

	private void createConnectionFactory(final Properties initProps) {

		final String url = initProps.getProperty(URL);
		final String driverClassName = initProps.getProperty(DRIVER);

		if (url == null)
			throw new RuntimeException("JDBC URL cannot be null");

		// throw new
		// DataImportHandlerException(DataImportHandlerException.SEVERE,
		// "JDBC URL cannot be null");

		if (driverClassName != null) {
			try {
				Class.forName(driverClassName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Could not load driver: "
						+ driverClassName + e.getMessage());
			}
		} else {
			throw new RuntimeException("Driver must be specified");
		}

		factory = new Callable<Connection>() {
			public Connection call() throws Exception {

				// LOG.info("Creating a connection for entity "
				// + " with URL: "
				// + url);

				// long start = System.currentTimeMillis();
				Connection c = null;
				try {
					c = DriverManager.getConnection(url, initProps);
				} catch (SQLException e) {

				}
				// LOG.info("Time taken for getConnection(): "
				// + (System.currentTimeMillis() - start));
				return c;
			}
		};
	}

	public Iterator<Map<String, Object>> getData(String query) {
		ResultSetIterator r = new ResultSetIterator(query);
		return r.getIterator();
	}

	private void logError(String msg, Exception e) {
		// LOG.log(Level.WARNING, msg, e);
	}

	private List<String> readFieldNames(ResultSetMetaData metaData)
			throws SQLException {
		List<String> colNames = new ArrayList<String>();
		int count = metaData.getColumnCount();
		for (int i = 0; i < count; i++) {
			colNames.add(metaData.getColumnLabel(i + 1));
		}
		return colNames;
	}

	private class ResultSetIterator {
		ResultSet resultSet;

		Statement stmt = null;

		List<String> colNames;

		Iterator<Map<String, Object>> rSetIterator;

		public ResultSetIterator(String query) {

			try {
				Connection c = getConnection();
				stmt = c.createStatement(ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				stmt.setFetchSize(batchSize);
				// LOG.finer("Executing SQL: " + query);
				// long start = System.currentTimeMillis();
				if (stmt.execute(query)) {
					resultSet = stmt.getResultSet();
				}
				// LOG.finest("Time taken for sql :"
				// + (System.currentTimeMillis() - start));
				colNames = readFieldNames(resultSet.getMetaData());
			} catch (Exception e) {
				throw new RuntimeException("Unable to execute query: " + query,
						e);
			}
			if (resultSet == null) {
				rSetIterator = new ArrayList<Map<String, Object>>().iterator();
				return;
			}

			rSetIterator = new Iterator<Map<String, Object>>() {
				public boolean hasNext() {
					return hasnext();
				}

				public HashMap<String, Object> next() {
					return getARow();
				}

				public void remove() {/* do nothing */
				}
			};
		}

		private Iterator<Map<String, Object>> getIterator() {
			return rSetIterator;
		}

		private HashMap<String, Object> getARow() {
			if (resultSet == null)
				return null;
			HashMap<String, Object> result = new HashMap<String, Object>();
			for (String colName : colNames) {
				try {

					result.put(colName, resultSet.getObject(colName));

//					if (!convertType) {
//						// Use underlying database's type information
//						result.put(colName, resultSet.getObject(colName));
//						continue;
//					}
//
//					Integer type = fieldNameVsType.get(colName);
//					if (type == null)
//						type = 12;
//					switch (type) {
//					case Types.INTEGER:
//						result.put(colName, resultSet.getInt(colName));
//						break;
//					case Types.FLOAT:
//						result.put(colName, resultSet.getFloat(colName));
//						break;
//					case Types.BIGINT:
//						result.put(colName, resultSet.getLong(colName));
//						break;
//					case Types.DOUBLE:
//						result.put(colName, resultSet.getDouble(colName));
//						break;
//					case Types.DATE:
//						result.put(colName, resultSet.getDate(colName));
//						break;
//					case Types.BOOLEAN:
//						result.put(colName, resultSet.getBoolean(colName));
//						break;
//					default:
//						result.put(colName, resultSet.getString(colName));
//						break;
//					}

				} catch (SQLException e) {
					logError("Error reading data ", e);
					throw new RuntimeException(
							"Error reading data from database", e);
				}
			}
			return result;
		}

		private boolean hasnext() {
			if (resultSet == null)
				return false;
			try {
				if (resultSet.next()) {
					return true;
				} else {
					close();
					return false;
				}
			} catch (SQLException e) {
				logError("Error reading data ", e);
				close();
				return false;
			}
		}

		private void close() {
			try {
				if (resultSet != null)
					resultSet.close();
				if (stmt != null)
					stmt.close();

			} catch (Exception e) {
				logError("Exception while closing result set", e);
			} finally {
				resultSet = null;
				stmt = null;
			}
		}
	}

	private Connection getConnection() throws Exception {
		long currTime = System.currentTimeMillis();
		if (currTime - connLastUsed > CONN_TIME_OUT) {
			synchronized (this) {
				Connection tmpConn = factory.call();
				close();
				connLastUsed = System.currentTimeMillis();
				return conn = tmpConn;
			}

		} else {
			connLastUsed = currTime;
			return conn;
		}
	}

	protected void finalize() {
		try {
			conn.close();
		} catch (Exception e) {
		}
	}

	public void close() {
		try {
			conn.close();
		} catch (Exception e) {
		}

	}

	private static final long CONN_TIME_OUT = 10 * 1000; // 10 seconds

	private static final int FETCH_SIZE = 500;

	public static final String URL = "url";

	public static final String DRIVER = "driver";

//	public static final String CONVERT_TYPE = "convertType";
}
