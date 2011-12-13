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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.io.EntryWriter;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.SimpleDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarCollection implements Iterable<Jar> {
  public static Argument<File> JAR_COLLECTION_CACHE = new RelativeFileArgument("jar-collection-cache", "jar-collection-cache", Arguments.CACHE, "Cache for jar collection.").permit();
  private final ArrayList<Jar> jars;
  private final VersionedFqnNode rootFragment;
  
  private JarCollection() {
    jars = new ArrayList<>();
    rootFragment = VersionedFqnNode.createRoot();
  }
  
  private void add(JarFile jar) {
    Jar newJar = new Jar(jar);
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jar.getFile().toFile()))) {
      for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
        if (entry.getName().endsWith(".class")) {
          String fqn = entry.getName();
          fqn = fqn.substring(0, fqn.lastIndexOf('.'));
          newJar.addFqn(rootFragment.getChild(fqn, '/'), Fingerprint.create(zis, entry.getSize()));
        }
      }
    } catch (IOException | IllegalArgumentException e) {
      logger.log(Level.SEVERE, "Error reading jar file: " + jar, e);
    }
    jars.add(newJar);
  }
  
  public static JarCollection make(TaskProgressLogger task) {
    task.start("Building jar collection");
    
    JarCollection jars = new JarCollection();
    JavaRepository repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    
    task.report("Checking for cache...");
    File cacheDir = JAR_COLLECTION_CACHE.getValue();
    File cache = new File(cacheDir, Fingerprint.FINGERPRINT_MODE.getValue() + ".cache");
    if (cache.exists()) {
      task.start("Cache found, loading", "jars loaded", 500);
      try (SimpleDeserializer deserializer = IOUtils.makeSimpleDeserializer(cache)) {
        for (Jar jar : deserializer.deserializeToIterable(Jar.makeDeserializer(jars.rootFragment, repo), true)) {
          task.progress();
          jars.jars.add(jar);
        }
        return jars;
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error loading jar collection cache", e);
        jars = new JarCollection();
      } finally {
        task.finish();
        task.finish();
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
    try (SimpleSerializer serializer = IOUtils.makeSimpleSerializer(FileUtils.ensureWriteable(cache));
         EntryWriter<Jar> writer = serializer.getEntryWriter(Jar.class)) {
      for (Jar jar : jars) {
        writer.write(jar);
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing jar collection cache", e);
    }
    task.finish();
    
    return jars;
  }
  
  @Override
  public Iterator<Jar> iterator() {
    return jars.iterator();
  }

  public int size() {
    return jars.size();
  }
  
  public VersionedFqnNode getRoot() {
    return rootFragment;
  }
  
  public void printStatistics(TaskProgressLogger task) {
    task.start("Printing jar collection statistics");
    
    task.report("Collection contains " + jars.size() + " jars");
    
    task.start("Printing FQN suffix tree statistics");
    
    int fragmentCount = 0;
    int fqnCount = 0;
    for (VersionedFqnNode fragment : rootFragment.getPostOrderIterable()) {
      fragmentCount++;
      if (fragment.getVersions().getJars().size() > 0) {
        fqnCount++;
      }
    }
    task.report("Suffix tree contains " + fragmentCount + " nodes");
    task.report("Suffix tree contains " + fqnCount + " leaves (FQNs)");
    task.report("Suffix tree contains " + (fragmentCount - fqnCount) + " internal nodes (packages)");
    
    task.finish();
    
    task.finish();
  }
}
