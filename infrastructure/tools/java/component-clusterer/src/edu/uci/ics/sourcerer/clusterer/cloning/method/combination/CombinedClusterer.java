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
package edu.uci.ics.sourcerer.clusterer.cloning.method.combination;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Map;

import edu.uci.ics.sourcerer.clusterer.cloning.basic.Confidence;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.File;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.KeyMatch;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.Project;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.ProjectMap;
import edu.uci.ics.sourcerer.clusterer.cloning.pairwise.FileMatching;
import edu.uci.ics.sourcerer.clusterer.cloning.pairwise.FileMatching.Type;
import edu.uci.ics.sourcerer.clusterer.cloning.pairwise.MatchStatus;
import edu.uci.ics.sourcerer.clusterer.cloning.pairwise.ProjectMatchSet;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CombinedClusterer {
  public static final Property<String> COMBINED_STATS = new StringProperty("combined-stats", "combined-stats.txt", "Stats calculated when creating the combined keys.");
  public static void computeCombinedKeys(ProjectMap projects) {
    logger.info("Computing combined keys...");
    ProjectMatchSet matches = projects.getProjectMatchSet();
    
    int totalProjects = projects.getProjects().size();
    
    int files = 0;
    int lowKey = 0;
    int mediumKey = 0;
    int highKey = 0;
    // Iterate through every project and file and construct a new matching key
    for (Project project : projects.getProjects()) {
      for (File file : project.getFiles()) {
        if (!file.hasAllKeys()) {
          continue;
        }
        files++;
        // For every file, collect the files it matches
        FileMatching matching = new FileMatching();
        for (KeyMatch match : file.getHashKey().getMatches()) {
          matching.getMatchStatus(match.getFile()).hash = match.getConfidence();
        }
        for (KeyMatch match : file.getFqnKey().getMatches()) {
          matching.getMatchStatus(match.getFile()).fqn = match.getConfidence();
        }
        for (KeyMatch match : file.getFingerprintKey().getMatches()) {
          matching.getMatchStatus(match.getFile()).fingerprint = match.getConfidence();
        }
        
        CombinedKey key = new CombinedKey();
        file.setCombinedKey(key);
        // Now for each of these files, decide on a confidence level
        for (Map.Entry<File, MatchStatus> entry : matching.getMatchStatusSet()) {
          if (file != entry.getKey()) {
            MatchStatus status = entry.getValue();
            /*
             * What are the possible combinations (ignoring hash)?
             * 
             * -------+-------------+-------
             * FQN    | FINGERPRINT | RESULT
             * -------+-------------+-------
             * HIGH   | HIGH        | HIGH
             * HIGH   | MEDIUM      | HIGH
             * HIGH   | LOW         | LOW
             * HIGH   | NULL        | NULL
             * -------+-------------+-------
             * MEDIUM | HIGH        | HIGH
             * MEDIUM | MEDIUM      | MH
             * MEDIUM | LOW         | LOW
             * MEDIUM | NULL        | NULL
             * -------+-------------+-------
             * LOW    | HIGH        | HIGH
             * LOW    | MEDIUM      | MH
             * LOW    | LOW         | LOW
             * LOW    | NULL        | NULL
             * -------+-------------+-------
             * NULL   | HIGH        | MH
             * NULL   | MEDIUM      | LM
             * NULL   | LOW         | NULL
             * NULL   | NULL        | N/A
             * -------+-------------+-------
             * 
             * LM: low if project correlation bad, medium if good
             * MH: medium if project correlation bad, high if good
             */
            if (status.hash != null) {
              // If the match is identical, it's high confidence
              key.addMatch(new KeyMatch(entry.getKey(), Confidence.HIGH));
            } else if ((status.fqn == Confidence.HIGH && (status.fingerprint == Confidence.HIGH || status.fingerprint == Confidence.MEDIUM))
                || ((status.fqn == Confidence.MEDIUM || status.fqn == Confidence.LOW) && status.fingerprint == Confidence.HIGH)) {
              key.addMatch(new KeyMatch(entry.getKey(), Confidence.HIGH));
            } else if (status.fqn != null && status.fingerprint == Confidence.LOW) { 
              key.addMatch(new KeyMatch(entry.getKey(), Confidence.LOW));
            } else {
              // Get an idea of the copying between the two projects
              FileMatching projectPair = matches.getFileMatch(project, entry.getKey().getProject());
              
              // Is there a core of identical files?
              boolean goodCorrelation = false;
              if (projectPair.getCount(Type.HASH, Confidence.HIGH) > 0) {
                goodCorrelation = true;
              } else if (projectPair.getCount(Type.FQN, Confidence.LOW) > 5) {
                goodCorrelation = true;
              } else if (projectPair.getCount(Type.FINGERPRINT, Confidence.MEDIUM) > 5) {
                goodCorrelation = true;
              }
              if ((status.fqn != null && status.fingerprint == Confidence.MEDIUM) || status.fingerprint == Confidence.HIGH) {
                key.addMatch(new KeyMatch(entry.getKey(), goodCorrelation ? Confidence.HIGH : Confidence.MEDIUM));
              } else if (status.fqn == null && status.fingerprint == Confidence.MEDIUM) {
                key.addMatch(new KeyMatch(entry.getKey(), goodCorrelation ? Confidence.MEDIUM : Confidence.LOW));
              }
            }
          }
        }
        if (key.isUnique(Confidence.HIGH)) {
          highKey++;
        }
        if (key.isUnique(Confidence.MEDIUM)) {
          mediumKey++;
        }
        if (key.isUnique(Confidence.LOW)) {
          lowKey++;
        }
      }
    }
    logger.info("  " + totalProjects + " and " + files + " files processed");
    logger.info("    " + highKey + " high unique");
    logger.info("    " + mediumKey + " medium unique");
    logger.info("    " + lowKey + " low unique");
  }
}
