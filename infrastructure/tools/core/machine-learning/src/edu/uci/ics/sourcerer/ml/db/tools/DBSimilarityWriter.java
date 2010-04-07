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

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;

import edu.uci.ics.sourcerer.db.schema.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.db.util.InsertBatcher;
import edu.uci.ics.sourcerer.ml.SimilarUserCalculator;
import edu.uci.ics.sourcerer.ml.TasteFileModelWithStringItemIds;
import edu.uci.ics.sourcerer.util.io.FileUtils;


/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Nov 30, 2009
 *
 */
public abstract class DBSimilarityWriter extends DatabaseAccessor implements ISimilarityWriter{

	SimilarUserCalculator suCalc; 
	protected java.io.File tempDir;
	public boolean clearTable = false;
	
	public DBSimilarityWriter(String dataFileLocation, 
			int neighborhoodSize,
			double similarityThreshold,
			long lowUserId,
			long highUserId,
			DatabaseConnection conn,
			boolean clearTable2) throws FileNotFoundException, TasteException{
		super(conn);
		this.clearTable = clearTable2;
		tempDir = FileUtils.getTempDir();
		suCalc = new SimilarUserCalculator(this);
		suCalc.setLowEntityId(lowUserId);
		suCalc.setHighEntityId(highUserId);
		File dataFile = new File(dataFileLocation);
		// DataModel dm = new TasteFileModelWithStringItemIds(dataFile);
		FileDataModel dm = new FileDataModel(dataFile);
		suCalc.setDataModel(dm);
		suCalc.setNeighborhoodSize(neighborhoodSize);
		suCalc.setSimilarityThreshold(similarityThreshold);
		
		 suCalc.setUserSimilarity(
				 getUserSimilarity(dm)
				 ) ;
	}
	
	public abstract UserSimilarity getUserSimilarity(DataModel dm);
	public abstract void initializeSimilarityTable();
	public abstract void write() throws TasteException;

}
