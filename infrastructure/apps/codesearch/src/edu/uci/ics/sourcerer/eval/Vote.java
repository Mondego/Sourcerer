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
package edu.uci.ics.sourcerer.eval;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.Set;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Vote {
  private static edu.uci.ics.sourcerer.eval.client.Vote[] votes = VoteOptions.getVoteOptions();
  
  private String id;
  private String text;
  private Set<String> subVotes;
  
  private Vote(String id, String text, Set<String> subVotes) {
    this.id = id;
    this.text = text;
    this.subVotes = subVotes;
  }
  
  public String getKey() {
    return id;
  }
  
  public boolean matches(String option, String subOption) {
    return text.equals(option) && (subOption == null || subVotes.contains(subOption));
  }
  
  public boolean matches(Set<String> options) {
    if (options.contains(text)) {
      return true;
    } else {
      for (String subVote : subVotes) {
        if (options.contains(subVote)) {
          return true;
        }
      }
    }
    return false;
  }
  
  public static Vote parseLine(String line) {
    String[] parts = line.split(" ");
    
    String value = null;
    Collection<Integer> subVotes = Helper.newLinkedList();
    for (char c : parts[1].toCharArray()) {
      if (value == null) {
        value = "" + c;
      } else if (value != null && c == '-') {
      } else {
        subVotes.add(Integer.parseInt("" + c));
      }
    }
    
    edu.uci.ics.sourcerer.eval.client.Vote vote = null;
    for (edu.uci.ics.sourcerer.eval.client.Vote candidate : votes) {
      if (value.equals(candidate.getValue())) {
        vote = candidate;
      }
    }
    
    if (vote != null) {
      Set<String> subVotesText = Helper.newHashSet();
      String[] subVoteArr = vote.getSubvotes();
      for (int subVote : subVotes) {
        subVotesText.add(subVoteArr[subVote]);
      }
      return new Vote(parts[0], vote.getText(), subVotesText);
    } else {
      logger.severe("Unable to parse line: " + line);
      return null;
    }
  }
  
  public static String[] getTextOptions() {
    String[] options = new String[votes.length];
    for (int i = 0; i < options.length; i++) {
      options[i] = votes[i].getText();
    }
    return options;
  }
  
  public static String[] getSubOptions(String option) {
    for (edu.uci.ics.sourcerer.eval.client.Vote vote : votes) {
      if (option.equals(vote.getText())) {
        return vote.getSubvotes();
      }
    }
    return null;
  }
}
