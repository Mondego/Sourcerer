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

public class Vote implements Serializable {
  private static final long serialVersionUID = -4747355221416262368L;
  private String value;
  private String text;
  private String[] subVotes;
  private boolean[] subVotesVotes;
  
  public Vote() {}
  public Vote(String value, String text, String... subVotes) {
    this.value = value;
    this.text = text;
    this.subVotes = subVotes;
    this.subVotesVotes = new boolean[subVotes.length];
  }
  
  public String getValue() {
    return value;
  }
  
  public String getVoteResult() {
    StringBuilder output = new StringBuilder();
    output.append(value).append("-");
    for (int i = 0; i < subVotesVotes.length; i++) {
      if (subVotesVotes[i]) {
        output.append(i);
      }
    }
    return output.toString();
  }

  public String getText() {
    return text;
  }
  
  public String[] getSubvotes() {
    return subVotes;
  }
  
  public void clearSubVotesVotes() {
    for (int i = 0; i < subVotesVotes.length; i++) {
      subVotesVotes[i] = false;
    }
  }
  
  public void setSubVoteVote(int index) {
    subVotesVotes[index] = true;
  }
  
  public String toString() {
    return value + ": " + text;
  }
}
