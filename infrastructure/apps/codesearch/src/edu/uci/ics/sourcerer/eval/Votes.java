package edu.uci.ics.sourcerer.eval;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

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
}
