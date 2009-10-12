package edu.uci.ics.sourcerer.eval;

import edu.uci.ics.sourcerer.eval.client.Vote;

public class VoteOptions {
  public static Vote[] getVoteOptions() {
    Vote[] options = new Vote[3];
    options[0] = new Vote("2", "The result fully solves the problem", 
        "Code can be copied with little modifiction",
        "Contains everything I needed to know",
        "Contains all the APIs needed",
        "Contains additional related information");
    options[1] = new Vote("1", "The result partially solves the problem",
        "Following references looks promising",
        "Contains something I needed to know",
        "Contains some of the APIs needed");
    options[2] = new Vote("0", "The result is not useful at all",
        "Too long, did not read",
        "An implementation of the functionality rather than an API usage example");
    return options;
  }
}
