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
package edu.uci.ics.sourcerer.tools.java.component.model.jar;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import edu.uci.ics.sourcerer.util.io.CustomSerializable;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.EnumArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class Fingerprint implements CustomSerializable {
  public static final Argument<Mode> FINGERPRINT_MODE = new EnumArgument<>("fingerprint-mode", Mode.class, "What fingerprint mode to use");
  
  public enum Mode {
    NONE,
    LENGTH,
    HASH,
    NAME,
    TYPE,
    ;
  }

  Fingerprint() {}
  
  private static class LengthFingerprint extends Fingerprint {
    private final long length;
    
    private LengthFingerprint(long length) {
      this.length = length;
    }
    
    @Override
    public int hashCode() {
      return (int)(length ^ (length >>> 32));
    }
    
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o instanceof LengthFingerprint) {
        LengthFingerprint other = (LengthFingerprint) o;
        return length == other.length;
      } else {
        return false;
      }
    }
    
    @Override
    public String serialize() {
      return Long.toString(length);
    }
  }
  
  private static class HashFingerprint extends Fingerprint {
    private final long length;
    private final String hash;
    
    private HashFingerprint(long length, String hash) {
      this.length = length;
      this.hash = hash;
    }
    
    @Override
    public int hashCode() {
      return hash.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o instanceof HashFingerprint) {
        HashFingerprint other = (HashFingerprint) o;
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
  }
 
  private static final Fingerprint BASE_FINGERPRINT = new Fingerprint() {
    @Override
    public String serialize() {
      return "";
    }
  };
  
  static Fingerprint create(InputStream is, long length) throws IOException {
    switch (FINGERPRINT_MODE.getValue()) {
      case NONE:
        return BASE_FINGERPRINT;
      case LENGTH:
        return new LengthFingerprint(length);
      case HASH:
        return new HashFingerprint(length, FileUtils.computeHash(is));
      case NAME:
        return NameFingerprint.create(is);
      case TYPE:
        return TypeFingerprint.create(is);
      default:
        logger.severe("Unknown fingerprint mode: " + FINGERPRINT_MODE.getValue());
        return null;
    }
  }
  
  public static ObjectDeserializer<Fingerprint> makeDeserializer() {
    switch (FINGERPRINT_MODE.getValue()) {
      case NONE:
        return new ObjectDeserializer<Fingerprint>() {
          @Override
          public Fingerprint deserialize(Scanner scanner) {
            return BASE_FINGERPRINT;
          }
        };
      case LENGTH:
        return new ObjectDeserializer<Fingerprint>() {
          @Override
          public Fingerprint deserialize(Scanner scanner) {
            if (scanner.hasNextLong()) {
              return new LengthFingerprint(scanner.nextLong());
            } else {
              logger.severe("Fingerprint missing length");
              return null;
            }
          }};
      case HASH:
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
                return new HashFingerprint(length, hash);
              } else {
                logger.severe("Fingerprint missing hash");
                return null;
              }
            } else {
              logger.severe("Fingerprint missing length");
              return null;
            }
          }
        };
      case NAME:
        return NameFingerprint.makeDeserializer();
      case TYPE:
        return TypeFingerprint.makeDeserializer();
      default:
        logger.severe("Unknown fingerprint mode: " + FINGERPRINT_MODE.getValue());
        return null;
    }
  }
}
