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
package edu.uci.ics.sourcerer.ml.db.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.util.JdbcDataSource;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 7, 2009
 * 
 */
public class FilteredUsageCalculator {

	UsageFilter filter;
	BitSet fqnsToSkip;
	BitSet entitiesToSkip;

	HashMap<String, FqnData> fqnData = new HashMap<String, FqnData>();

	IUsageWriter writer;

	public void setWriter(IUsageWriter writer) {
		this.writer = writer;
	}

	public void init(int distinctFqnsUsedByEntities, // = 2;
			// only allow APIs that are used more than this number
			int fqnUseCount, // = 3;
			// eid, fqn, count
			String entityEachUsedFqnFile,
			// eid, count
			String entityAllUsedFqnsCountFile,
			// fqn, count
			String usedFqnsCountFile,
			// fqn
			String popularFqnsFile) {

		filter = new UsageFilter(distinctFqnsUsedByEntities, fqnUseCount,
				popularFqnsFile, usedFqnsCountFile, entityEachUsedFqnFile,
				entityAllUsedFqnsCountFile);

		initEntitiesToSkip();
		logger.info("Done calculating entities to skip.");
		initFqnsToSkip();
		logger.info("Done calculating Fqns to skip.");
	}

	private void initFqnsToSkip() {

		if (filter == null)
			return;

		fqnsToSkip = new BitSet(10000000);

		if (filter.popularFqnsFile != null) {
			initPopularFqnsToSkip();
		}

		// load fqns from all jar usage count
		if (filter.usedFqnsCountFile != null) {
			initLessUsedFqnsToSkip();
		}

	}

	private void initEntitiesToSkip() {

		entitiesToSkip = new BitSet(10000000);

		String fileName = filter.entityAllUsedFqnsCountFile;

		FileInputStream in;
		BufferedReader br;
		try {
			in = new FileInputStream(fileName);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				if (strLine == null)
					continue;

				// process line
				String[] _cols = strLine.split("\t");
				if (_cols == null || _cols.length != 2)
					continue;

				long _entityId = Long.parseLong(_cols[0]);
				int _fqnsUsed = Integer.parseInt(_cols[1]);

				// making sure ids are withing limit of narrow casting
				// limits the # of entities to 2,147,483,647
				assert (_entityId < Integer.MAX_VALUE);

				if (_fqnsUsed < filter.distinctFqnsUsedByEntities) {
					entitiesToSkip.set((int) (_entityId - 1));
				}
			}

			br.close();
			in.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "File not found: " + fileName + "."
					+ " Entities will less usage won't be skipped");

		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOE with file: " + fileName + "."
					+ " Entities will less usage won't be skipped");
		}

	}

	private void initPopularFqnsToSkip() {

		String fileName = filter.popularFqnsFile;

		FileInputStream in;
		BufferedReader br;
		try {
			in = new FileInputStream(fileName);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				if (strLine == null || strLine.length() <= 0)
					continue;

				long _fqnId = Long.parseLong(strLine);
				// making sure ids are withing limit of narrow casting
				// limits the # of entities to 2,147,483,647
				assert (_fqnId < Integer.MAX_VALUE);
				fqnsToSkip.set((int) (_fqnId - 1));
			}

			br.close();
			in.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "File not found: " + fileName + "."
					+ " Highly used FQNs won't be skipped");

		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOE with file: " + fileName + "."
					+ " Highly used FQNs won't be skipped");
		}

	}

	private void initLessUsedFqnsToSkip() {
		int userEntitiesCountThreshold = filter.fqnUseCount;
		String fileName = filter.usedFqnsCountFile;

		FileInputStream in;
		BufferedReader br;
		try {
			in = new FileInputStream(fileName);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				if (strLine == null || strLine.length() <= 0)
					continue;

				// process line
				String[] _cols = strLine.split("\t");
				if (_cols == null || _cols.length != 3)
					continue;

				int _fCount = Integer.parseInt(_cols[2]);

				if (_fCount >= userEntitiesCountThreshold) {
					// don't skip this fqn
					continue;
				} else {
					long _fqnId = Long.parseLong(_cols[0]);
					// making sure ids are withing limit of narrow casting
					// limits the # of entities to 2,147,483,647
					assert (_fqnId < Integer.MAX_VALUE);
					fqnsToSkip.set((int) (_fqnId - 1));
				}
			}
			br.close();
			in.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "File not found: " + fileName + "."
					+ " Least used FQNs won't be skipped");
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOE with file: " + fileName + "."
					+ " Least used FQNs won't be skipped");
		}

	}

	public void writeUsage() {

		boolean error = false;

		assert writer != null;
		// open entity usage file
		FileInputStream in;
		BufferedReader br;
		try {
			in = new FileInputStream(filter.entityEachUsedFqnFile);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				if (strLine == null || strLine.length() <= 0)
					continue;

				// process line
				String[] _cols = strLine.split("\t");
				if (_cols == null || _cols.length != 3)
					continue;

				long _fqnId = Long.parseLong(_cols[1]);
				long _entityId = Long.parseLong(_cols[0]);
				int _count = Integer.parseInt(_cols[2]);

				if (skipApi(_fqnId) || skipEntity(_entityId)) {
					continue;
				}

				writer.writeUsage(_entityId, _fqnId, _count);
			}

			br.close();
			in.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "File not found: "
					+ filter.entityEachUsedFqnFile + "."
					+ " Cannot produce usage file");
			error = true;
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOE with file: "
					+ filter.entityEachUsedFqnFile + "."
					+ " Cannot produce usage file");
			error = true;
		}

		if (error) {
			logger.info("Done without writing usage.");
		} else {

			logger.info("Done writing Usage.");
		}
	}

	private boolean skipApi(Long apiFqnId) {
		if (fqnsToSkip == null)
			return false;
		else {
			assert apiFqnId <= Integer.MAX_VALUE;

			if (fqnsToSkip.get((int) (apiFqnId - 1)))
				return true;
			else
				return false;
		}

	}

	private boolean skipEntity(Long entityId) {
		if (entitiesToSkip == null)
			return false;
		else {
			assert entityId <= Integer.MAX_VALUE;

			if (entitiesToSkip.get((int) (entityId - 1)))
				return true;
			else
				return false;
		}

	}

}

class UsageFilter {

	public UsageFilter(int distinctFqnsUsedByEntities2, int fqnUseCount2,
			String popularFqnsFile2, String usedFqnsCountFile2,
			String entityEachUsedFqnFile2, String entityAllUsedFqnsFile2) {
		this.distinctFqnsUsedByEntities = distinctFqnsUsedByEntities2;
		this.fqnUseCount = fqnUseCount2;

		this.usedFqnsCountFile = usedFqnsCountFile2;
		this.popularFqnsFile = popularFqnsFile2;
		this.entityEachUsedFqnFile = entityEachUsedFqnFile2;
		this.entityAllUsedFqnsCountFile = entityAllUsedFqnsFile2;
	}

	// only allow entities using more that this # of entities (APIs)
	int distinctFqnsUsedByEntities = 2;
	// only allow APIs that are used more than this number
	int fqnUseCount = 3;

	// fqn, count
	String usedFqnsCountFile;

	// fqn
	String popularFqnsFile;

	// eid, fqnid, count
	String entityEachUsedFqnFile;

	// eid, counts
	String entityAllUsedFqnsCountFile;

}
