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

import edu.uci.ics.sourcerer.eval.client.Vote;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class VoteOptions {
  public static String getVoteAbbreviation(String vote) {
    if (vote.equals("The result fully solves the problem")) {
      return "FUL";
    } else if (vote.equals("Code can be copied with little modifiction")) {
      return "COP";
    } else if (vote.equals("Contains everything I needed to know")) {
      return "EVE";
    } else if (vote.equals("Contains all the APIs needed")) {
      return "AAP";
    } else if (vote.equals("Contains additional related information")) {
      return "ADD";
    } else if (vote.equals("The result partially solves the problem")) {
      return "PSP";
    } else if (vote.equals("Following references looks promising")) {
      return "REF";
    } else if (vote.equals("Contains something I needed to know")) {
      return "STK";
    } else if (vote.equals("Contains some of the APIs needed")) {
      return "SAP";
    } else if (vote.equals("The result is not useful at all")) {
      return "NOT";
    } else if (vote.equals("Too long, did not read")) {
      return "LON";
    } else if (vote.equals("An implementation of the functionality rather than an API usage example")) {
      return "IMP";
    } else {
      return null;
    }
  }
  
  public static String getVoteFromAbbreviation(String abbreviation) {
    if (abbreviation.equals("FUL")) {
      return "The result fully solves the problem";
    } else if (abbreviation.equals("COP")) {
      return "Code can be copied with little modifiction";
    } else if (abbreviation.equals("EVE")) {
      return "Contains everything I needed to know";
    } else if (abbreviation.equals("AAP")) {
      return "Contains all the APIs needed";
    } else if (abbreviation.equals("ADD")) {
      return "Contains additional related information";
    } else if (abbreviation.equals("PSP")) {
      return "The result partially solves the problem";
    } else if (abbreviation.equals("REF")) {
      return "Following references looks promising";
    } else if (abbreviation.equals("STK")) {
      return "Contains something I needed to know";
    } else if (abbreviation.equals("SAP")) {
      return "Contains some of the APIs needed";
    } else if (abbreviation.equals("NOT")) {
      return "The result is not useful at all";
    } else if (abbreviation.equals("LON")) {
      return "Too long, did not read";
    } else if (abbreviation.equals("IMP")) {
      return "An implementation of the functionality rather than an API usage example";
    } else {
      return null;
    }
  }
  
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
