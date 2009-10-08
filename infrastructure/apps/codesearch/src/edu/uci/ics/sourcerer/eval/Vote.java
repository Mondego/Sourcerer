package edu.uci.ics.sourcerer.eval;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.Set;

import edu.uci.ics.sourcerer.util.Helper;

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
