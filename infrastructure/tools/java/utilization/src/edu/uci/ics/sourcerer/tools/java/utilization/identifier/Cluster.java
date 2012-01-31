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
package edu.uci.ics.sourcerer.tools.java.utilization.identifier;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeSet;

import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarSet;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.io.CustomSerializable;
import edu.uci.ics.sourcerer.util.io.LineBuilder;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Cluster implements CustomSerializable {
  

  private JarSet jars;
  private JarSet exemplars;
  private final Collection<VersionedFqnNode> coreFqns;
  private final Collection<VersionedFqnNode> extraFqns;
  private Collection<VersionedFqnNode> exemplarFqns;
  
  Cluster() {
    this.coreFqns = new HashSet<>();
    this.extraFqns = new TreeSet<>();
    jars = JarSet.create();
    exemplars = jars;
    exemplarFqns = Collections.emptySet();
  }
  
  void addCoreFqn(VersionedFqnNode fqn) {
    coreFqns.add(fqn);
    jars = jars.merge(fqn.getVersions().getJars());
  }
  
  void mergeCluster(Cluster cluster) {
    if (cluster.jars.getIntersectionSize(jars) < cluster.jars.size()) {
      logger.severe("Unexpected: merge should only be permitted with full overlap.");
    }
    if (!cluster.extraFqns.isEmpty()) {
      logger.severe("Unexpected: merge target should not have any extra fqns.");
    }
    extraFqns.addAll(cluster.coreFqns);
  }
  
  void addExemplarFqn(VersionedFqnNode fqn) {
    if (exemplarFqns.isEmpty()) {
      exemplarFqns = new HashSet<>();
    }
    exemplarFqns.add(fqn);
  }
  
  void addExemplar(Jar jar) {
    exemplars.add(jar);
  }
  
  public Collection<VersionedFqnNode> getCoreFqns() {
    return coreFqns;
  }
  
  public Collection<VersionedFqnNode> getExtraFqns() {
    return extraFqns;
  }
  
  public Collection<VersionedFqnNode> getExemplarFqns() {
    return exemplarFqns;
  }
  
  public JarSet getJars() {
    return jars;
  }
  
  @Override
  public String toString() {
    return "core:{" + coreFqns.toString() + "} extra:{" + extraFqns.toString() +"}";
  }
  
  @Override
  public String serialize() {
    LineBuilder builder = new LineBuilder();
    builder.append(jars.size());
    for (Jar jar : jars) {
      builder.append(jar.getJar().getProperties().HASH.getValue());
    }
    builder.append(coreFqns.size());
    for (VersionedFqnNode fqn : coreFqns) {
      builder.append(fqn.getFqn());
    }
    builder.append(extraFqns.size());
    for (VersionedFqnNode fqn : extraFqns) {
      builder.append(fqn.getFqn());
    }
    return builder.toString();
  }
  
  public static ObjectDeserializer<Cluster> makeDeserializer(final JarCollection jars) {
    return new ObjectDeserializer<Cluster>() {
      @Override
      public Cluster deserialize(Scanner scanner) {
        if (scanner.hasNextInt()) {
          Cluster cluster = new Cluster();
          int jarCount = scanner.nextInt();
          for (int i = 0; i < jarCount; i++) {
            if (scanner.hasNext()) {
              String hash = scanner.next();
              Jar jar = jars.getJar(hash);
              if (jar == null) {
                logger.severe("Unable to locate jar: " + hash);
              } else {
                cluster.jars = cluster.jars.add(jar);
              }
            } else {
              logger.severe("Missing expected jar for cluster deserialization");
              return null;
            }
          }
          
          if (scanner.hasNextInt()) {
            int coreCount = scanner.nextInt();
            for (int i = 0; i < coreCount; i++) {
              if (scanner.hasNext()) {
                cluster.coreFqns.add(jars.getRoot().getChild(scanner.next(), '.'));
              } else {
                logger.severe("Missing expected core fqn for cluster deserialization");
                return null;
              }
            }            
          } else {
            logger.severe("Missing core fqn count for cluster deserialization");
            return null;
          }
          
          if (scanner.hasNextInt()) {
            int extraCount = scanner.nextInt();
            for (int i = 0; i < extraCount; i++) {
              if (scanner.hasNext()) {
                cluster.extraFqns.add(jars.getRoot().getChild(scanner.next(), '.'));
              } else {
                logger.severe("Missing expected extra fqn for cluster deserialization");
                return null;
              }
            }            
          } else {
            logger.severe("Missing core fqn count for cluster deserialization");
            return null;
          }
          return cluster;
        } else {
          logger.severe("Missing jar count for cluster deserialization");
          return null;
        }
      }};
  }
}
