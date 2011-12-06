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
package edu.uci.ics.sourcerer.tools.java.utilization.model;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import edu.uci.ics.sourcerer.util.io.CustomSerializable;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Fingerprint implements CustomSerializable {
  public static final Argument<Boolean> FINGERPRINT_USE_HASH = new BooleanArgument("fingerprint-use-hash", true, "Use hash to determine fingerpint");
  private long length;
  private String hash;
  
  private Fingerprint(long length, String hash) {
    this.length = length;
    this.hash = hash;
  }
  
  protected static Fingerprint make(InputStream is, long length) throws IOException {
    String hash = null;
    if (FINGERPRINT_USE_HASH.getValue()) {
      hash = FileUtils.computeHash(is);
    }
    return new Fingerprint(length, hash);
  }
  
  @Override
  public int hashCode() {
    return hash.hashCode();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof Fingerprint) {
      Fingerprint other = (Fingerprint) o;
      if (hash == null) {
        return length == other.length && other.hash == null;
      } else {
        return length == other.length && hash.equals(other.hash);
      }
    } else {
      return false;
    }
  }
  
  @Override
  public String serialize() {
    return length + " " + hash;
  }
  
  public static ObjectDeserializer<Fingerprint> makeDeserializer() {
    return new ObjectDeserializer<Fingerprint>() {
      @Override
      public Fingerprint deserialize(Scanner scanner) {
        if (scanner.hasNextLong()) {
          long length = scanner.nextLong();
          if (scanner.hasNext()) {
            String hash = scanner.next();
            if (hash.equals("null")) {
              hash = null;
            }
            return new Fingerprint(length, hash);
          } else {
            logger.severe("Fingerprint missing hash");
            return null;
          }
        } else {
          logger.severe("Fingerprint missing length");
          return null;
        }
      }};
  }
}
