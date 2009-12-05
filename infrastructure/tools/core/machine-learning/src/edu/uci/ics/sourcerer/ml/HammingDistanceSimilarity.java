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


import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import java.util.Collection;

/**
 * @author <a href="mailto:bajracharya@gmail.com">Sushil Bajracharya</a>
 */
public final class HammingDistanceSimilarity  implements UserSimilarity {

  private final DataModel dataModel;

  public HammingDistanceSimilarity (DataModel dataModel) {
    this.dataModel = dataModel;
  }

  /**
   * @throws UnsupportedOperationException
   */
  @Override
  public void setPreferenceInferrer(PreferenceInferrer inferrer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public double userSimilarity(long userID1, long userID2) throws TasteException {

    FastIDSet prefs1 = dataModel.getItemIDsFromUser(userID1);
    FastIDSet prefs2 = dataModel.getItemIDsFromUser(userID2);

    int prefs1Size = prefs1.size();
    int prefs2Size = prefs2.size();
    int intersectionSize = prefs1Size < prefs2Size ?
        prefs2.intersectionSize(prefs1) :
        prefs1.intersectionSize(prefs2);
    if (intersectionSize == 0) {
      return Double.NaN;
    }
    
    //int numItems = dataModel.getNumItems();
    //int numItems = Math.max(prefs1Size, prefs2Size);

    //int numItems = prefs1Size;
    
    double distance1 =  (double) (prefs1Size - intersectionSize)/(double) 2;
    double distance2 =  (double) (prefs2Size - intersectionSize)/(double) 2;
    double distance = (distance1 + distance2);
    double similarity = 1.0 / (1.0 + distance); 
    ////System.out.println( prefs1Size  + ", " + prefs2Size + ", " + intersectionSize + ", " + similarity);
    return similarity;
  }

 

  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    alreadyRefreshed = RefreshHelper.buildRefreshed(alreadyRefreshed);
    RefreshHelper.maybeRefresh(alreadyRefreshed, dataModel);
  }

  @Override
  public String toString() {
    return "HammingDistanceSimilarity [dataModel:" + dataModel + ']';
  }

}
