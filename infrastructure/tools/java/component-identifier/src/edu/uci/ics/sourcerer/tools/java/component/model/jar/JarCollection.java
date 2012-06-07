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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.InvalidFileFormatException;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger.Checkpoint;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarCollection implements Iterable<Jar> {
  public static final Argument<File> JAR_COLLECTION_CACHE = new RelativeFileArgument("jar-collection-cache", "jar-collection-cache", Arguments.CACHE, "Cache for jar collection.").permit();
  
  private final Map<String, Jar> jars;
  private final VersionedFqnNode rootFragment;
  
  private JarCollection() {
    jars = new HashMap<>();
    rootFragment = VersionedFqnNode.createRoot();
  }
    
  public static JarCollection create() {
    return create(JavaRepositoryFactory.INPUT_REPO, JAR_COLLECTION_CACHE);
  }
  
  public static JarCollection create(Collection<String> jarHashes) {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Building jar collection");
    
    JarCollection jars = new JarCollection();
    JavaRepository repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    
    task.start("Adding jars", "jars added", 500);
    for (String hash : jarHashes) {
      JarFile jar = repo.getJarFile(hash);
      if (jar == null) {
        logger.warning("Unknown jar: " + hash);
      } else {
        task.progress();
        jars.add(jar);
      }
    }
    task.finish();
    
    task.finish();
    
    return jars;
  }
  
  public static JarCollection create(Argument<File> repoDir, Argument<File> cacheDirArg) {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Building jar collection");
    
    JarCollection jars = new JarCollection();
    JavaRepository repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(repoDir);
    
    task.report("Checking for cache...");
    File cacheDir = cacheDirArg.getValue();
    File cache = new File(cacheDir, Fingerprint.FINGERPRINT_MODE.getValue() + ".cache");
    if (cache.exists()) {
      Checkpoint checkpoint = task.checkpoint();
      task.report(" Cache found");
      task.start("Loading jars", "jars loaded", 0);
      try (BufferedReader reader = IOUtils.createBufferedReader(cache)) {
        // Read the number of jars
        int count = Integer.parseInt(reader.readLine());
        // Read the jars
        Jar[] jarMapping = new Jar[count];
        for (int i = 0; i < count; i++) {
          String hash = reader.readLine();
          Jar jar = new Jar(repo.getJarFile(hash));
          jarMapping[i] = jar;
          jars.jars.put(hash, jar);
          task.progress();
        }
        task.finish();
        // Load the tree
        jars.rootFragment.createLoader(jarMapping).load(reader);
        task.finish();
        return jars;
      } catch (IOException | NullPointerException | InvalidFileFormatException | IllegalArgumentException e) {
        logger.log(Level.SEVERE, "Error loading jar collection cache", e);
        checkpoint.activate();
        jars = new JarCollection();
      }
    } else {
      task.report("Cache not found, loading...");
    }
    
    task.start("Adding maven jars", "jars added", 500);
    for (JarFile jar : repo.getMavenJarFiles()) {
      jars.add(jar);
      task.progress();
    }
    task.finish();
    
    task.start("Adding project jars", "jars added", 500);
    for (JarFile jar : repo.getProjectJarFiles()) {
      jars.add(jar);
      task.progress();
    }
    task.finish();
    
    task.report(jars.size() + " jars added to collection");
    task.finish();
    
    task.start("Saving cache");
    try (BufferedWriter writer = IOUtils.makeBufferedWriter(FileUtils.ensureWriteable(cache))) {
      // Write out the jar count
      writer.write(Integer.toString(jars.size()));
      writer.newLine();
      // Write out the jars
      Map<Jar, Integer> jarMapping = new HashMap<>();
      int count = 0;
      for (Jar jar : jars) {
        jarMapping.put(jar, count++);
        writer.write(jar.getJar().getProperties().HASH.getValue());
        writer.newLine();
      }
      // Write out the tree
      jars.rootFragment.createSaver(jarMapping).save(writer);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing jar collection cache", e);
    }
    task.finish();
    
    return jars;
  }
  
  public void save(File file) {
    // Let's see if we can save things more speedily
    try (BufferedWriter writer = IOUtils.makeBufferedWriter(file)) {
      Map<Jar, Integer> jarMap = new HashMap<>();
      int count = 0;
      // Write out a line for each jar, just containing the hash
      for (Map.Entry<String, Jar> entry : jars.entrySet()) {
        writer.write(entry.getKey());
        writer.newLine();
        jarMap.put(entry.getValue(), count++);
      }
      // Write out the entire tree, starting at the root
      
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error saving jar collection");
    }
  }
  
  private void add(JarFile jar) {
    Jar newJar = new Jar(jar);
    Map<String, Long> names = new HashMap<>();
    // If there are duplicates, always go with the first entry
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jar.getFile().toFile()))) {
      for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
        if (entry.getName().endsWith(".class")) {
          Long length = names.get(entry.getName());
          if (length == null) {
            names.put(entry.getName(), entry.getSize());
          }
        }
      }
    } catch (IOException | IllegalArgumentException e) {
      logger.log(Level.SEVERE, "Error reading jar file: " + jar, e);
      return;
    }
    
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jar.getFile().toFile()))) {
      for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
        if (entry.getName().endsWith(".class")) {
          // Should i include this entry?
          Long length = names.get(entry.getName());
          if (length != null && length.longValue() == entry.getSize()) {
            names.remove(entry.getName());
            String fqn = entry.getName();
            fqn = fqn.substring(0, fqn.lastIndexOf('.'));
            newJar.addFqn(rootFragment.getChild(fqn, '/').getVersion(Fingerprint.create(zis, entry.getSize())));
          }
        }
      }
      // Make sure it's non-empty
      if (!newJar.getFqns().isEmpty()) {
        jars.put(jar.getProperties().HASH.getValue(), newJar);
      }
    } catch (IOException | IllegalArgumentException e) {
      // If there were errors, just throw the jar out
      logger.log(Level.SEVERE, "Error reading jar file: " + jar, e);
      logger.severe("Would like to remove");
    }
    
  }
  
  public Jar getJar(String hash) {
    return jars.get(hash);
  }
  
  public Collection<Jar> getJars() {
    return jars.values();
  }
  
  @Override
  public Iterator<Jar> iterator() {
    return jars.values().iterator();
  }

  public int size() {
    return jars.size();
  }
  
  public VersionedFqnNode getRoot() {
    return rootFragment;
  }
}
