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
package edu.uci.ics.sourcerer.tools.java.repo.model.internal;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.internal.AbstractRepoProject;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.AbstractRepository;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.BatchImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractJavaRepository<Project extends AbstractRepoProject<? extends AbstractRepository<Project, Batch>, ?>, Batch extends BatchImpl<Project>, Jar extends IJar> extends AbstractRepository<Project, Batch> {
  public static final Argument<String> JARS_DIRECTORY = new StringArgument("jars-dir", "jars", "Repository subdirectory containing jar files.").permit();
  public static final Argument<String> PROJECT_JARS_DIRECTORY = new StringArgument("project-jars-dir", "project", "Directory containing project jars.").permit();
  public static final Argument<String> PROJECT_JAR_INDEX = new StringArgument("project-jar-index", "project-index.txt", "Project jar index file.").permit();
  public static final Argument<String> MAVEN_JARS_DIRECTORY = new StringArgument("maven-jars-dir", "maven", "Directory containing maven jars.").permit();
  public static final Argument<String> MAVEN_JAR_INDEX = new StringArgument("maven-jar-index", "maven-index.txt", "Maven jar index file.").permit();
  public static final Argument<String> LIBRARY_JARS_DIRECTORY = new StringArgument("library-jars-dir", "lib", "Directory containing Java library jars.").permit();
  public static final Argument<String> LIBRARY_JAR_INDEX = new StringArgument("library-jar-index", "library-index.txt", "Library jar index file.").permit();
  
  protected Map<String, Jar> mavenJarIndex;
  protected Map<String, Jar> projectJarIndex;
  protected Map<String, Jar> libraryJarIndex;
  
  private RepoFileImpl mavenJarIndexFile;
  private RepoFileImpl projectJarIndexFile;
  private RepoFileImpl libraryJarIndexFile;
  
  protected AbstractJavaRepository(RepoFileImpl repoRoot) {
    super(repoRoot);
    RepoFileImpl jars = this.repoRoot.getChild(JARS_DIRECTORY);
    jars.makeDirs();
    mavenJarIndexFile = jars.getChild(MAVEN_JAR_INDEX);
    projectJarIndexFile = jars.getChild(PROJECT_JAR_INDEX);
    libraryJarIndexFile = jars.getChild(LIBRARY_JAR_INDEX);
  }
  
  protected abstract ObjectDeserializer<Jar> makeDeserializer();
  
  protected abstract Jar loadJar(RepoFileImpl dir);
  
  protected void reset() {
    mavenJarIndex = null;
    projectJarIndex = null;
    libraryJarIndex = null;
  }
  
  protected void clearMavenJarCache() {
    mavenJarIndexFile.delete();
  }
  
  protected void clearProjectJarCache() {
    projectJarIndexFile.delete();
  }
  
  protected void clearLibraryJarCache() {
    libraryJarIndexFile.delete();
  }
  
  protected void loadMavenJarIndex() {
    TaskProgressLogger task = TaskProgressLogger.get();
    if (mavenJarIndexFile.exists() && !CLEAR_CACHES.getValue()) {
      try (SimpleDeserializer deserializer = IOUtils.makeSimpleDeserializer(mavenJarIndexFile.toFile())) {
        task.start("Deserializing maven index");
        mavenJarIndex = deserializer.deserializeMap(String.class, makeDeserializer(), false);
        task.finish();
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error loading jar index.", e);
        createMavenJarIndex();
      }
    } else {
      createMavenJarIndex();
    }
  }
  
  private void createMavenJarIndex() {
    mavenJarIndex = new HashMap<>();
    
    Deque<RepoFileImpl> stack = new LinkedList<>();
    stack.push(repoRoot.getChild(JARS_DIRECTORY).getChild(MAVEN_JARS_DIRECTORY));
    while (!stack.isEmpty()) {
      RepoFileImpl dir = stack.pop();
      Jar jar = loadJar(dir);
      if (jar != null) {
        mavenJarIndex.put(jar.getProperties().HASH.getValue(), jar);
      }
      for (RepoFileImpl child : dir.getChildren()) {
        if (child.isDirectory()) {
          stack.add(child);
        }
      }
    }
    
    if (!mavenJarIndex.isEmpty()) {
      SimpleSerializer serializer = null;
      try {
        serializer = IOUtils.makeSimpleSerializer(mavenJarIndexFile.toFile());
        serializer.serialize(mavenJarIndex);
        return;
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to serialize maven jar index.", e);
      } finally {
        IOUtils.close(serializer);
      }
      mavenJarIndexFile.delete();
    }
  }
  
  protected void loadProjectJarIndex() {
    if (projectJarIndexFile.exists() && !CLEAR_CACHES.getValue()) {
      SimpleDeserializer deserializer = null;
      try {
        deserializer = IOUtils.makeSimpleDeserializer(projectJarIndexFile.toFile());
        projectJarIndex = deserializer.deserializeMap(String.class, makeDeserializer(), false);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error loading jar index.", e);
        createProjectJarIndex();
      } finally {
        IOUtils.close(deserializer);
      }
    } else {
      createProjectJarIndex();
    }
  }
  
  private void createProjectJarIndex() {
    projectJarIndex = new HashMap<>();
    
    RepoFileImpl projectDir = repoRoot.getChild(JARS_DIRECTORY).getChild(PROJECT_JARS_DIRECTORY);
    for (RepoFileImpl a : projectDir.getChildren()) {
      if (a.isDirectory()) {
        for (RepoFileImpl b : a.getChildren()) {
          Jar jar = loadJar(b);
          if (jar != null) {
            projectJarIndex.put(jar.getProperties().HASH.getValue(), jar);
          }
        }
      }
    }
    
    if (!projectJarIndex.isEmpty()) {
      SimpleSerializer serializer = null;
      try {
        serializer = IOUtils.makeSimpleSerializer(projectJarIndexFile.toFile());
        serializer.serialize(projectJarIndex);
        return;
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to serialize project jar index.", e);
      } finally {
        IOUtils.close(serializer);
      }
      projectJarIndexFile.delete();
    }
  }
  
  protected void loadLibraryJarIndex() {
    if (libraryJarIndexFile.exists() && !CLEAR_CACHES.getValue()) {
      SimpleDeserializer deserializer = null;
      try {
        deserializer = IOUtils.makeSimpleDeserializer(libraryJarIndexFile.toFile());
        libraryJarIndex = deserializer.deserializeMap(String.class, makeDeserializer(), false);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error loading library jars.", e);
        createLibraryJarIndex();
      } finally {
        IOUtils.close(deserializer);
      }
    } else {
      createLibraryJarIndex();
    }
  }
  
  private void createLibraryJarIndex() {
    libraryJarIndex = new HashMap<>();
    
    RepoFileImpl dir = repoRoot.getChild(JARS_DIRECTORY).getChild(LIBRARY_JARS_DIRECTORY);
    for (RepoFileImpl child : dir.getChildren()) {
      if (child.isDirectory()) {
        Jar jar = loadJar(child);
        if (jar != null) {
          libraryJarIndex.put(jar.getProperties().HASH.getValue(), jar);
        }
      }
    }
    
    if (!libraryJarIndex.isEmpty()) {
      SimpleSerializer serializer = null;
      try {
        serializer = IOUtils.makeSimpleSerializer(libraryJarIndexFile.toFile());
        serializer.serialize(libraryJarIndex);
        return;
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to serialize library jars.", e);
      } finally {
        IOUtils.close(serializer);
      }
      libraryJarIndexFile.delete();
    }
  }
  
  public Jar getJarFile(String hash) {
    TaskProgressLogger task = TaskProgressLogger.get();
    if (libraryJarIndex == null) {
      task.start("Loading library index");
      loadLibraryJarIndex();
      task.finish();
    }
    Jar jar = libraryJarIndex.get(hash);
    if (jar == null) {
      if (mavenJarIndex == null) {
        task.start("Loading maven index");
        loadMavenJarIndex();
        task.finish();
      }
      jar = mavenJarIndex.get(hash);
      if (jar == null) {
        if (projectJarIndex == null) {
          task.start("Loading project index");
          loadProjectJarIndex();
          task.finish();
        }
        jar = projectJarIndex.get(hash);
      }
    }
    return jar;
  }
  
  public Collection<Jar> getMavenJarFiles() {
    if (mavenJarIndex == null) {
      loadMavenJarIndex();
    }
    return Collections.unmodifiableCollection(mavenJarIndex.values());
  }

  public Collection<Jar> getProjectJarFiles() {
    if (projectJarIndex == null) {
      loadProjectJarIndex();
    }
    return Collections.unmodifiableCollection(projectJarIndex.values());
  }
  
  public Collection<Jar> getLibraryJarFiles() {
    if (libraryJarIndex == null) {
      loadLibraryJarIndex();
    }
    return Collections.unmodifiableCollection(libraryJarIndex.values());
  }
}
