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
package edu.uci.ics.sourcerer.ml;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import edu.uci.ics.sourcerer.ml.db.tools.ISimilarityWriter;

/**
 * @author <a href="mailto:bajracharya@gmail.com">Sushil Bajracharya</a>
 */
public class SimilarUserCalculator {
	
	private DataModel model;
	private UserSimilarity userSimilarity; // = new PearsonCorrelationSimilarity(model);
	private int neighborhoodSize = 10;
	private UserNeighborhood neighborhood;
	double similarityThreshold = 0.4;
	boolean loaded;
	
	long lowEntityId = -1;
	long highEntityId = -1;
	
	public long getLowEntityId() {
		return lowEntityId;
	}

	public void setLowEntityId(long lowEntityId) {
		this.lowEntityId = lowEntityId;
	}
	
	public long getHighEntityId() {
		return highEntityId;
	}

	public void setHighEntityId(long highEntityId) {
		this.highEntityId = highEntityId;
	}

	ISimilarityWriter writer;
	
	public SimilarUserCalculator(ISimilarityWriter writer){
		this.writer = writer;
	}
	
	public void setNeighborhoodSize(int neighborhoodSize) throws TasteException {
		this.neighborhoodSize = neighborhoodSize;
	}
	
	public void setSimilarityThreshold(double threshold){
		this.similarityThreshold = threshold;
	}
	
	public void loadNeighborhood() throws TasteException{
		if(!loaded) 
			this.neighborhood = 
					new NearestNUserNeighborhood(
							this.neighborhoodSize, 
							this.similarityThreshold, 
							this.userSimilarity, 
							this.model);
		loaded = true;
	}

	public void calculate() throws TasteException{
		loadNeighborhood();
		// iterate through each user, and get similar users from neighborhood
		LongPrimitiveIterator usersIterator = model.getUserIDs();
		long start = System.currentTimeMillis();
		while(usersIterator.hasNext()){
			
			long uid = usersIterator.nextLong();
			
			if(this.lowEntityId > -1 && this.highEntityId > -1){
				
				if(this.highEntityId < this.lowEntityId)
					throw new RuntimeException("Lower ID needs to be smaller than higher ID");
				
				if (uid < this.lowEntityId || uid > this.highEntityId)
					// skip this uid (ie entity)
					continue;
			}
			
			long[] similarUsers = neighborhood.getUserNeighborhood(uid);
			for(long su: similarUsers){
				writer.writeSimilarty(uid + "", su + "", "" + userSimilarity.userSimilarity(uid, su));
			}
			long end = System.currentTimeMillis();
			
			logger.info("Written similarity tw for entity id:" + uid + Util.formatMs((long) (end - start)) + "ms");
			start = System.currentTimeMillis();
		}
	}

	public long[] getSimilarUsers(long userId) throws TasteException{
		loadNeighborhood();
		return neighborhood.getUserNeighborhood(userId);
	}
	
	public void setDataModel(DataModel model) {
		this.model = model;
		loaded = false;
	}
	
	public void setUserSimilarity(UserSimilarity similarity){
		this.userSimilarity = similarity;
		loaded = false;
	}
}
