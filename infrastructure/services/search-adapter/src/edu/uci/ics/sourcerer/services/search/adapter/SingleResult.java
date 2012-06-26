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
package edu.uci.ics.sourcerer.services.search.adapter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SingleResult {
  private int rank;
  private float score;
  private long entityID;
  private String fqn;
  private int paramCount;
  private String params;
  private String returnFqn;
  
  SingleResult() {}

  public int getRank() {
    return rank;
  }
  
  void setRank(int rank) {
    this.rank = rank;
  }
  
  public float getScore() {
    return score;
  }
  
  void setScore(float score) {
    this.score = score;
  }
  
  public Long getEntityID() {
    return entityID;
  }

  void setEntityID(long entityID) {
    this.entityID = entityID;
  }

  public String getFqn() {
    return fqn;
  }

  void setFqn(String fqn) {
    this.fqn = fqn;
  }

  public int getParamCount() {
    return paramCount;
  }

  void setParamCount(int paramCount) {
    this.paramCount = paramCount;
  }
  
  public String getParams() {
    return params;
  }
  
  void setParams(String params) {
    this.params = params;
  }
  
  public String getReturnFqn() {
    return returnFqn;
  }
  
  void setReturnFqn(String returnFqn) {
    this.returnFqn = returnFqn;
  }
}
