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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.ContentFile;
import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceRepository;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.ProjectLocationImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarSource;
import edu.uci.ics.sourcerer.tools.java.repo.model.ModifiableJavaRepository;
import edu.uci.ics.sourcerer.util.CounterSet;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class JavaRepositoryImpl extends AbstractJavaRepository<JavaProjectImpl, JavaBatchImpl, JarFileImpl> implements ModifiableJavaRepository {
  private JavaRepositoryImpl(RepoFileImpl repoRoot) {
    super(repoRoot);
  }
  
  protected static JavaRepositoryImpl load(RepoFileImpl repoRoot) {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Loading Java repository from: " + repoRoot.toFile().getPath());
    try {
      JavaRepositoryImpl repo = new JavaRepositoryImpl(repoRoot);
      // Verify the repository type
      String type = repo.properties.REPOSITORY_TYPE.getValue();
      // If it's a new repo
      if (type == null) {
        repo.properties.REPOSITORY_TYPE.setValue(ModifiableSourceRepository.class.getName());
        repo.properties.save();
        return repo;
      } else if (type.equals(ModifiableSourceRepository.class.getName())) {
        return repo;
      } else {
        logger.severe("Invalid repository type: " + type);
        return null;
      }
    } finally {
      task.finish();
    }
  }
  
  @Override
  protected ObjectDeserializer<JarFileImpl> makeDeserializer() {
    return JarFileImpl.makeDeserializer(repoRoot);
  }
  
  @Override
  protected JarFileImpl loadJar(RepoFileImpl dir) {
    return JarFileImpl.load(dir);
  }
  
  @Override
  protected JavaProjectImpl createProject(ProjectLocationImpl loc) {
    return new JavaProjectImpl(this, loc);
  }
  
  private boolean aggregating = false;
  private Map<String, Collection<ContentFile>> aggregationMap;
  
  public JarFileImpl getJarFile(ContentFile file) {
    String hash = FileUtils.computeHash(file.getFile().toFile());
    if (hash == null) {
      return null;
    }
    if (mavenJarIndex == null) {
      loadMavenJarIndex();
    }
    JarFileImpl jar = mavenJarIndex.get(hash);
    if (jar == null) {
      if (projectJarIndex == null) {
        loadProjectJarIndex();
      }
      jar = projectJarIndex.get(hash);
      if (jar == null) {
        if (libraryJarIndex == null) {
          loadLibraryJarIndex();
        }
        jar = libraryJarIndex.get(hash);
        if (jar == null && aggregating) {
          Collection<ContentFile> files = aggregationMap.get(hash);
          if (files == null) {
            files = new LinkedList<>();
            aggregationMap.put(hash, files);
          }
          files.add(file);
        }
      }
    }
    return jar;
  }

  @Override
  public void aggregateJarFiles() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Aggregating jar files");
    
    aggregating = true;
    aggregationMap = new HashMap<>();
    
    task.start("Processing projects", "projects processed", 500);
    for (JavaProjectImpl project : getProjects()) {
      task.progress();
      project.getContent().init(true, false);
    }
    task.finish();
    
    RepoFileImpl projectDir = repoRoot.getChild(JARS_DIRECTORY).getChild(PROJECT_JARS_DIRECTORY);
    
    task.start("Finding next project jar path");
    RepoFileImpl biggestA = null;
    int biggestAVal = -1;
    
    for (RepoFileImpl a : projectDir.getChildren()) {
      if (a.isDirectory()) {
        try {
          int aVal = Integer.parseInt(a.getName());
          if (aVal > biggestAVal) {
            biggestAVal = aVal;
            biggestA = a;
          }
        } catch (NumberFormatException e) {}
      }
    }
    
    int biggestBVal = -1;
    if (biggestA == null) {
      biggestA = projectDir.getChild("0");
      biggestAVal = 0;
    } else {
      for (RepoFileImpl b : biggestA.getChildren()) {
        try {
          biggestBVal = Math.max(biggestBVal, Integer.parseInt(b.getName()));
        } catch (NumberFormatException e) {}
      }
    }
    if (biggestAVal == 0 && biggestBVal == -1) {
      task.report("None found, starting at 0/0");
    } else {
      task.report("Found " + biggestAVal + "/" + biggestBVal);
    }
    task.finish();

    task.start("Aggregating " + aggregationMap.size() + " jars", "jars aggregated", 100);
    int total = 0;
    for (Map.Entry<String, Collection<ContentFile>> entry : aggregationMap.entrySet()) {
      task.progress();
      if (biggestBVal >= 999) {
        biggestA = projectDir.getChild(Integer.toString(++biggestAVal));
        biggestBVal = -1;
      }
      RepoFileImpl dir = biggestA.getChild(Integer.toString(++biggestBVal));
      
      // Make the directory
      dir.makeDirs();

      // Copy the file
      FileUtils.copyFile(entry.getValue().iterator().next().getFile().toFile(), dir.getChild(JarFileImpl.JAR_NAME).toFile());
      
      // Populate the properties
      JarFileImpl jar = JarFileImpl.create(dir);
      
      JarProperties properties = jar.getProperties();
      
      // Find the most popular name
      CounterSet<String> bestName = new CounterSet<String>();
      for (ContentFile file : entry.getValue()) {
        total++;
        bestName.increment(file.getFile().getName());
      }
      properties.NAME.setValue(bestName.getMax().getObject());
      properties.HASH.setValue(entry.getKey());
      properties.SOURCE.setValue(JarSource.PROJECT);
      properties.save();
    }
    task.report(total + " jars encountered");
    task.finish();
    aggregating = false;
    aggregationMap = null;
    reset();
    clearProjectJarCache();
    for (JavaProjectImpl project : getProjects()) {
      project.getContent().init(false, true);
    }
    task.finish();
  }

  // TODO: make this handle overwriting
  @Override
  public void addLibraryJarFile(File jar, File source) {
    RepoFileImpl dir = repoRoot.getChild(JARS_DIRECTORY).getChild(LIBRARY_JARS_DIRECTORY).getChild(jar.getName());
    
    // Make the directory
    dir.makeDirs();
    
    // Copy the files
    FileUtils.copyFile(jar, dir.getChild(JarFileImpl.JAR_NAME).toFile());
    if (source != null && source.exists()) {
      FileUtils.copyFile(source, dir.getChild(JarFileImpl.SOURCE_JAR_NAME).toFile());
    }
    
    // Populate the properties
    JarFileImpl newJar = JarFileImpl.create(dir);
    
    JarProperties properties = newJar.getProperties();
    properties.NAME.setValue(jar.getName());
    properties.SOURCE.setValue(JarSource.JAVA_LIBRARY);
    properties.HASH.setValue(FileUtils.computeHash(jar));
    properties.save();
    
    clearLibraryJarCache();
    if (libraryJarIndex != null) {
      libraryJarIndex.put(properties.HASH.getValue(), newJar);
    }
  }
  
  @Override
  public void addMavenJarFile(File jar, File source, String group, String artifact, String version) {
    if (mavenJarIndex == null) {
      loadMavenJarIndex();
    }
    String hash = FileUtils.computeHash(jar);
    while (mavenJarIndex.containsKey(hash)) {
      logger.info("Difficulty matching: " + group + "." + artifact + " (" + version + ")");
      JarFileImpl mavenJar = mavenJarIndex.get(hash);
      logger.info("  Found duplicate at: " + mavenJar);
      JarProperties properties = mavenJar.getProperties();
      // If it's not exactly equal, just add the duplicate, giving the original priority
      if (group.equals(properties.GROUP.getValue()) && artifact.equals(properties.NAME.getValue()) && version.equals(properties.VERSION.getValue())) {
        return;
      }
      // Add a 0 to the end of the hash
      hash += "0";
    } 
    String subDir = group.replace('.', '/') + "/" + artifact + "/" + version;

    RepoFileImpl dir = repoRoot.getChild(JARS_DIRECTORY).getChild(MAVEN_JARS_DIRECTORY).getChild(subDir);

    // Verify there's no overlap
    if (dir.exists()) {
      logger.log(Level.SEVERE, "Directory already exists for: " + group + "." + artifact + " (" + version + ")");
    } else {
      // Make the directory
      dir.makeDirs();
      
      // Copy the files
      FileUtils.copyFile(jar, dir.getChild(JarFileImpl.JAR_NAME).toFile());
      if (source != null && source.exists()) {
        FileUtils.copyFile(source, dir.getChild(JarFileImpl.SOURCE_JAR_NAME).toFile());
      }
      
      // Populate the properties
      JarFileImpl newJar = JarFileImpl.create(dir);

      JarProperties properties = newJar.getProperties();
      properties.NAME.setValue(artifact);
      properties.GROUP.setValue(group);
      properties.SOURCE.setValue(JarSource.MAVEN);
      properties.HASH.setValue(hash);
      properties.VERSION.setValue(version);
      properties.save();
    }
  }
  
  @Override
  public JavaBatchImpl newBatch(RepoFileImpl dir, Integer batch) {
    return new JavaBatchImpl(this, dir, batch);
  }
}