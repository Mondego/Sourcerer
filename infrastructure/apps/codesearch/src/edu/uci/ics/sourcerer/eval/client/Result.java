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
package edu.uci.ics.sourcerer.eval.client;

import java.io.Serializable;

public class Result implements Serializable {
  private static final long serialVersionUID = -1040540329345550096L;
  
  private String entityID;
  private String formattedFqn;
  private String formattedCode;
  private int number;
  
  private Result() {}

  public static Result getResult(String entityID, String formattedFqn, String formattedCode) {
    Result result = new Result();
    result.entityID = entityID;
    result.formattedFqn = formattedFqn;
    result.formattedCode = formattedCode;
    return result;
  }
  
  public String getEntityID() {
    return entityID;
  }
  
  public String getFormattedFqn() {
    return formattedFqn;
  }
  
  public String getFormattedCode() {
    return formattedCode;
  }
  
  public void setNumber(int number) {
    this.number = number;
  }
  
  public int getNumber() {
    return number;
  }
    
  public String toString() {
    return entityID + ": " + formattedFqn;
  }
  
  public boolean equals(Object o) {
    if (o instanceof Result) {
      return entityID.equals(((Result)o).entityID);
    } else {
      return false;
    }
  }
  
  public int hashCode() {
    return entityID.hashCode();
  }
}
