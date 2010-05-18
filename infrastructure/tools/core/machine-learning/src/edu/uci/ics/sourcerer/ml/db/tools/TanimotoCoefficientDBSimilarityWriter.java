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

import java.io.FileNotFoundException;

import javax.swing.text.StyleContext.SmallAttributeSet;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import edu.uci.ics.sourcerer.db.schema.EntitySimilarityHammingDistanceTable;
import edu.uci.ics.sourcerer.db.schema.EntitySimilarityTanimotoCoefficientTable;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.db.util.InsertBatcher;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 3, 2009
 *
 */
public class TanimotoCoefficientDBSimilarityWriter extends DBSimilarityWriter {
	EntitySimilarityTanimotoCoefficientTable simTCTable;
	InsertBatcher batcher; 
	public TanimotoCoefficientDBSimilarityWriter(String dataFileLocation,
			int neighborhoodSize, double similarityThreshold, long lowUserId,
			long highUserId, DatabaseConnection conn, boolean clearTable)
			throws FileNotFoundException, TasteException {
		super(dataFileLocation, neighborhoodSize, similarityThreshold, lowUserId,
				highUserId, conn, clearTable);
		simTCTable = new EntitySimilarityTanimotoCoefficientTable(executor, executor.getTableLocker());
		simTCTable.initializeInserter(tempDir);
	}
	
	@Override
	public void initializeSimilarityTable(){
		if(clearTable){
			executor.dropTables(simTCTable);
			simTCTable.createTable(executor);
		}
		
	}
	
	@Override
	public void write() throws TasteException{
		suCalc.calculate();
		simTCTable.flushInserts();
	}
	
	@Override
	public UserSimilarity getUserSimilarity(DataModel dm) {
		return
		// new CachingUserSimilarity(
		new TanimotoCoefficientSimilarity(dm)
		// , dm)
		;
	}
	
	@Override
	public void writeSimilarty(String lhsEid, String rhsEid, String similarity) {
		simTCTable.insert(lhsEid, rhsEid, similarity);
	}
	
	

}
