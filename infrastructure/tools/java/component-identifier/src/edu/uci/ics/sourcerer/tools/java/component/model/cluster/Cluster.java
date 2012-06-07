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
package edu.uci.ics.sourcerer.tools.java.component.model.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.component.model.jar.FqnVersion;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.JarSet;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.CachedReference;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Cluster {
  public static final Comparator<Cluster> ASCENDING_SIZE_COMPARATOR =
    new Comparator<Cluster>() {
      @Override
      public int compare(Cluster o1, Cluster o2) {
        int cmp = Integer.compare(o1.getJars().size(), o2.getJars().size());
        if (cmp == 0) {
          return Integer.compare(o1.hashCode(), o2.hashCode());
        } else {
          return cmp;
        }
      }
    };
  public static final Comparator<Cluster> DESCENDING_SIZE_COMPARATOR = 
      new Comparator<Cluster>() {
        @Override
        public int compare(Cluster o1, Cluster o2) {
          int cmp = -Integer.compare(o1.getJars().size(), o2.getJars().size());
          if (cmp == 0) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
          } else {
            return cmp;
          }
        }
      };

  private JarSet jars;
  private final Collection<VersionedFqnNode> coreFqns;
  private Collection<VersionedFqnNode> versionFqns;
  private CachedReference<Map<Set<FqnVersion>, ClusterVersion>> versions = new CachedReference<Map<Set<FqnVersion>, ClusterVersion>>() {
    @Override
    protected Map<Set<FqnVersion>, ClusterVersion> create() {
      Map<Set<FqnVersion>, ClusterVersion> versions = new HashMap<>();
      for (Jar jar : jars) {
        Set<FqnVersion> fqns = new HashSet<>();
        for (FqnVersion fqn : jar.getFqns()) {
          if (coreFqns.contains(fqn.getFqn()) || versionFqns.contains(fqn.getFqn())) {
            fqns.add(fqn);
          }
        }
        ClusterVersion version = versions.get(fqns);
        if (version == null) {
          version = ClusterVersion.create(Cluster.this, fqns);
          versions.put(fqns, version);
        }
        version.addJar(jar);
      }
      return versions;
    }
  };
  private Cluster() {
    this.coreFqns = new HashSet<>();
  }
  
  public static Cluster create(VersionedFqnNode fqn) {
    Cluster cluster = new Cluster();

    cluster.jars = fqn.getJars();
    cluster.coreFqns.add(fqn);
    cluster.versionFqns = Collections.emptySet();
    
    return cluster;
  }

  public void mergeCore(Cluster cluster) {
    coreFqns.addAll(cluster.coreFqns);
  }
  
  public void addVersionedCore(VersionedFqnNode fqn) {
    if (versionFqns.isEmpty()) {
      versionFqns = new ArrayList<>();
    }
    versionFqns.add(fqn);
    versions.clear();
  }
  
  public Collection<VersionedFqnNode> getCoreFqns() {
    return coreFqns;
  }
  
  public Collection<VersionedFqnNode> getVersionFqns() {
    return versionFqns;
  }

  public Collection<ClusterVersion> getVersions() {
    return versions.get().values();
  }
  
//  public ClusterVersion getVersion(Set<FqnVersion> version) {
//    return versions.get().get(version);
//  }
  
  public JarSet getJars() {
    return jars;
  }
  
  @Override
  public String toString() {
    return "core:{" + coreFqns.toString() + "} version:{" + versionFqns.toString() +"}";
  }
  
//  @Override
//  public String serialize() {
//    LineBuilder builder = new LineBuilder();
//    builder.append(jars.size());
//    for (Jar jar : jars) {
//      builder.append(jar.getJar().getProperties().HASH.getValue());
//    }
//    builder.append(coreFqns.size());
//    for (VersionedFqnNode fqn : coreFqns) {
//      builder.append(fqn.getFqn());
//    }
//    builder.append(versionFqns.size());
//    for (VersionedFqnNode fqn : versionFqns) {
//      builder.append(fqn.getFqn());
//    }
//    return builder.toString();
//  }
//  
//  public static ObjectDeserializer<Cluster> makeDeserializer(final JarCollection jars) {
//    return new ObjectDeserializer<Cluster>() {
//      @Override
//      public Cluster deserialize(Scanner scanner) {
//        if (scanner.hasNextInt()) {
//          Cluster cluster = new Cluster();
//          int jarCount = scanner.nextInt();
//          for (int i = 0; i < jarCount; i++) {
//            if (scanner.hasNext()) {
//              String hash = scanner.next();
//              Jar jar = jars.getJar(hash);
//              if (jar == null) {
//                logger.severe("Unable to locate jar: " + hash);
//              } else {
//                cluster.jars = cluster.jars.add(jar);
//              }
//            } else {
//              logger.severe("Missing expected jar for cluster deserialization");
//              return null;
//            }
//          }
//          
//          if (scanner.hasNextInt()) {
//            int coreCount = scanner.nextInt();
//            for (int i = 0; i < coreCount; i++) {
//              if (scanner.hasNext()) {
//                cluster.coreFqns.add(jars.getRoot().getChild(scanner.next(), '.'));
//              } else {
//                logger.severe("Missing expected core fqn for cluster deserialization");
//                return null;
//              }
//            }            
//          } else {
//            logger.severe("Missing core fqn count for cluster deserialization");
//            return null;
//          }
//          
//          if (scanner.hasNextInt()) {
//            int versionedCount = scanner.nextInt();
//            for (int i = 0; i < versionedCount; i++) {
//              if (scanner.hasNext()) {
//                cluster.versionFqns.add(jars.getRoot().getChild(scanner.next(), '.'));
//              } else {
//                logger.severe("Missing expected extra fqn for cluster deserialization");
//                return null;
//              }
//            }            
//          } else {
//            logger.severe("Missing core fqn count for cluster deserialization");
//            return null;
//          }
//          return cluster;
//        } else {
//          logger.severe("Missing jar count for cluster deserialization");
//          return null;
//        }
//      }};
//  }
}
