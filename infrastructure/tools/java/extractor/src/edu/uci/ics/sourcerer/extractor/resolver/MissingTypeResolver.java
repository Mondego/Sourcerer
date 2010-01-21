package edu.uci.ics.sourcerer.extractor.resolver;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.schema.JarEntitiesTable;
import edu.uci.ics.sourcerer.db.schema.JarsTable;
import edu.uci.ics.sourcerer.db.util.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.extractor.io.IUsedJarWriter;
import edu.uci.ics.sourcerer.model.extracted.MissingTypeEX;
import edu.uci.ics.sourcerer.repo.extracted.Extracted;
import edu.uci.ics.sourcerer.repo.extracted.io.ExtractedReader;
import edu.uci.ics.sourcerer.repo.general.IndexedJar;
import edu.uci.ics.sourcerer.repo.general.JarIndex;
import edu.uci.ics.sourcerer.util.Helper;

public class MissingTypeResolver extends DatabaseAccessor {
  public MissingTypeResolver(DatabaseConnection connection) {
    super(connection);
  }

  public Collection<IndexedJar> resolveMissingTypes(JarIndex index, Extracted extracted, IUsedJarWriter writer) {
    // Keep track of all the jars
    Map<String, JarTypeCollection> jars = Helper.newHashMap();
    
    // Go through every missing type and find the jars that contain it
    Collection<String> types = Helper.newHashSet();
    for (MissingTypeEX type : ExtractedReader.getMissingTypeReader(extracted)) {
      if (!types.contains(type.getFqn())) {
        types.add(type.getFqn());
        Collection<String> results = JarEntitiesTable.getJarIDsByFqn(executor, type.getFqn());
        // Try it as a package
        if (results.size() == 0) {
          results = JarEntitiesTable.getJarIDsByPackage(executor, type.getFqn());
        }
        if (results.size() == 0) {
//          logger.info("  Unable to find missing type: " + type.getFqn());
        } else {
          for (String jarID : results ) {
            JarTypeCollection collection = jars.get(jarID);
            if (collection == null) {
              collection = new JarTypeCollection(jarID);
              jars.put(jarID, collection);
            }
            collection.addType(type.getFqn());
          }
        }
      }
    }
    
    Collection<IndexedJar> retval = Helper.newHashSet();
    // Now repeatedly go through and find the jar that resolves the most missing types
    Collection<JarTypeCollection> bestJars = Helper.newLinkedList();
    while (true) {
      int bestSize = 0;
      for (JarTypeCollection jar : jars.values()) {
        if (jar.size() > bestSize) {
          bestJars.clear();
          bestJars.add(jar);
          bestSize = jar.size();
        } else if (jar.size() == bestSize) {
          bestJars.add(jar);
        }
      }
      
      // If bestJars is empty, we're done
      if (bestSize == 0) {
        break;
      } else {
        // For the moment, if we have multiple best jars,
        // just pick the one with the lowest id
        // Should put something smarter in here later
        // (like picking the latest version maven jar first)
        JarTypeCollection bestJar = bestJars.iterator().next();
        // Remove the types from bestJar from all the remaining jars
        jars.remove(bestJar.getJarID());
        for (JarTypeCollection jar : jars.values()) {
          jar.remove(bestJar.getTypes());
        }
        String hash = JarsTable.getHashByID(executor, bestJar.getJarID());
        if (hash == null) {
          logger.log(Level.SEVERE, "Unable to find jar hash: " + bestJar.getJarID());
        } else {
          IndexedJar indexed = index.getIndexedJar(hash);
          if (indexed == null) {
            logger.log(Level.SEVERE, "Unable to find jar in index: " + hash);
          } else {
            // Write that this jar is being used
            writer.writeUsedJar(indexed.getHash(), bestJar.getTypes().toArray(new String[bestSize]));
            retval.add(indexed);
          }
        }
      }
    }
    
    return retval;
  }
  
  private static class JarTypeCollection {
    private String jarID;
    private Collection<String> types;
    
    public JarTypeCollection(String jarID) {
      this.jarID = jarID;
      types = Helper.newHashSet();
    }
    
    public String getJarID() {
      return jarID;
    }
    
    public void addType(String type) {
      types.add(type);
    }
    
    public Collection<String> getTypes() {
      return types;
    }
    
    public void remove(Collection<String> toRemove) {
      types.removeAll(toRemove);
    }
    
    public int size() {
      return types.size();
    }
  }
}
