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
package edu.uci.ics.sourcerer.tools.java.cloning.method.fingerprint;

import static edu.uci.ics.sourcerer.tools.java.cloning.method.fingerprint.FingerprintClusterer.REQUIRE_FINGERPRINT_NAME_MATCH;
import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Counter;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class NameFingerprintIndex implements FingerprintIndex<NameFingerprintKey> {
  private InvertedIndex<NameFingerprintKey> fieldIndex = new InvertedIndex<NameFingerprintKey>();
  private InvertedIndex<NameFingerprintKey> methodIndex = new InvertedIndex<NameFingerprintKey>();
  int fileCount = 0;
  private Set<String> excludedFields;
  private Set<String> excludedMethods;
  
  @Override
  public void add(NameFingerprintKey fingerprint) {
    if (fingerprint.getSize() >= FingerprintClusterer.MINIMUM_FINGERPRINT_SIZE.getValue()) {
      fieldIndex.addFingerprint(fingerprint, fingerprint.getFields());
      methodIndex.addFingerprint(fingerprint, fingerprint.getMethods());
      fileCount++;
    }
  }
  
  @Override
  public Collection<JaccardIndex> getJaccardIndices(NameFingerprintKey fingerprint) {
    if (fingerprint.getSize() >= FingerprintClusterer.MINIMUM_FINGERPRINT_SIZE.getValue()) {
      Map<NameFingerprintKey, Counter<NameFingerprintKey>> result = Helper.newHashMap();
      fieldIndex.collectFingerprints(result, fingerprint.getFields());
      methodIndex.collectFingerprints(result, fingerprint.getMethods());
      
      Collection<JaccardIndex> retval = Helper.newArrayList();
      for (Counter<NameFingerprintKey> counter : result.values()) {
        if (fingerprint != counter.getObject()) {
          if (REQUIRE_FINGERPRINT_NAME_MATCH.getValue()) {
            if (fingerprint.getName().equals(counter.getObject().getName())) {
              double intersection = counter.getCount();
              double union = fingerprint.getSize(excludedFields, excludedMethods) + counter.getObject().getSize(excludedFields, excludedMethods) - 2 - intersection;
              double index = intersection / union;
              retval.add(new JaccardIndex(counter.getObject(), index));
            }
          } else {
            double intersection = counter.getCount();
            if (fingerprint.getName().equals(counter.getObject().getName())) {
              intersection += 1;
            }
            double union = fingerprint.getSize(excludedFields, excludedMethods) + counter.getObject().getSize(excludedFields, excludedMethods) - intersection;
            double index = intersection / union;
            retval.add(new JaccardIndex(counter.getObject(), index));
          }
        } else {
          // Verify that the jaccard index for a perfect match is 1
          if (REQUIRE_FINGERPRINT_NAME_MATCH.getValue()) {
            if (fingerprint.getName().equals(counter.getObject().getName())) {
              double intersection = counter.getCount();
              double union = fingerprint.getSize(excludedFields, excludedMethods) + counter.getObject().getSize(excludedFields, excludedMethods) - 2 - intersection;
              double index = intersection / union;
              if (index < 1) {
                logger.log(Level.SEVERE, "Equality mismatch!");
                logger.log(Level.SEVERE, "  " + intersection + " " + union + " " + index);
                logger.log(Level.SEVERE, fingerprint.toString());
              }
            }
          } else {
            double intersection = counter.getCount();
            if (fingerprint.getName().equals(counter.getObject().getName())) {
              intersection += 1;
            }
            double union = fingerprint.getSize(excludedFields, excludedMethods) + counter.getObject().getSize(excludedFields, excludedMethods) - intersection;
            double index = intersection / union;
            if (index < 1) {
              logger.log(Level.SEVERE, "Equality mismatch!");
              logger.log(Level.SEVERE, "  " + intersection + " " + union + " " + index);
              logger.log(Level.SEVERE, fingerprint.toString());
            }
          }
        }
      }
      return retval;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public void clearPopularNames() {
    logger.info("Clearing popular names from fingerprint index...");
    logger.info("  " + fileCount + " files included");
    int maxSize = 1000;
    logger.info("  Excluding names that occur in > " + maxSize + " files");
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(FingerprintClusterer.EXCLUDED_NAMES_FILE);
    printer.addHeader("Excluded field names");
    excludedFields = fieldIndex.clearPopularNames(printer, maxSize);
    logger.info("  " + excludedFields.size() + " fields excluded");
    printer.addHeader("Excluded method names");
    excludedMethods = methodIndex.clearPopularNames(printer, maxSize);
    logger.info("  " + excludedMethods.size() + " methods excluded");
    printer.close();
  }
}
