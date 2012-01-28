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
package edu.uci.ics.sourcerer.tools.java.utilization.model.jar;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.util.io.CustomSerializable;
import edu.uci.ics.sourcerer.util.io.LineBuilder;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;
/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Jar implements CustomSerializable {
  private final JarFile jar;
  private final Collection<VersionedFqnNode> fqns;
  
  Jar(JarFile jar) {
    this.jar = jar;
    fqns = new ArrayList<>();
  }
  
  void addFqn(VersionedFqnNode fqn, Fingerprint fingerprint) {
    fqns.add(fqn);
    fqn.addJar(this, fingerprint);
  }
  
  public JarFile getJar() {
    return jar;
  }
  
  public Collection<VersionedFqnNode> getFqns() {
    return fqns;
  }
  
  @Override
  public String toString() {
    return jar.toString();
  }

  @Override
  public String serialize() {
    LineBuilder builder = new LineBuilder();
    builder.append(jar.getProperties().HASH.getValue());
    for (VersionedFqnNode fqn : fqns) {
      builder.append(fqn.getFqn());
      
    }
    return builder.toString();
  }
  
  public static ObjectDeserializer<Jar> makeDeserializer(final VersionedFqnNode rootFragment, final JavaRepository repo) {
    return new ObjectDeserializer<Jar>() {
      ObjectDeserializer<Fingerprint> fingerprintDeserializer = Fingerprint.makeDeserializer();
      @Override
      public Jar deserialize(Scanner scanner) {
        if (scanner.hasNext()) {
          String hash = scanner.next();
          JarFile jarFile = repo.getJarFile(hash);
          if (jarFile == null) {
            logger.severe("Jar with hash " + hash + " cannot be found in " + repo);
            return null;
          } else {
            Jar jar = new Jar(jarFile);
            while (scanner.hasNext()) {
              jar.addFqn(rootFragment.getChild(scanner.next(), '.'), fingerprintDeserializer.deserialize(scanner));
            }
            return jar;
          }
        } else {
          logger.severe("Missing hash for jar deserialization");
          return null;
        }
      }
    };
  }
}
