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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Votes {
  private String user;
  private Map<String, Vote> votes;
  
  public Votes(String user) {
    this.user = user;
    votes = Helper.newHashMap();
  }
  
  public String getUser() {
    return user;
  }
  
  public void addVotes(File log) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(log));
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        Vote vote = Vote.parseLine(line);
        votes.put(vote.getKey(), vote);
      }
      br.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading votes file: " + log.getPath(), e);
    }
  }
  
  public int getUnionCount(Set<String> top, String option, String subOption) {
    int count = 0;
    for (String key : top) {
      Vote vote = votes.get(key);
      if (vote.matches(option, subOption)) {
        count++;
      }
    }
    return count;
  }
  
  public Set<String> getRelevant(Collection<String> results, Set<String> relevantOptions) {
    Set<String> relevant = Helper.newHashSet();
    for (String result : results) {
      Vote vote = votes.get(result);
      if (vote != null) {
        if (vote.matches(relevantOptions)) {
          relevant.add(result);
        }
      }
    }
    return relevant;
  }
}
