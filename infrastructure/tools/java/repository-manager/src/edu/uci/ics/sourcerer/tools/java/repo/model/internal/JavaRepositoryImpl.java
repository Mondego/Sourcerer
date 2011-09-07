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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.core.repo.model.ContentFile;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.ProjectLocationImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarSource;
import edu.uci.ics.sourcerer.tools.java.repo.model.ModifiableJavaRepository;
import edu.uci.ics.sourcerer.util.CounterSet;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaRepositoryImpl extends AbstractJavaRepository<JavaProjectImpl, JavaBatchImpl, JarFileImpl> implements ModifiableJavaRepository {
  protected JavaRepositoryImpl(RepoFileImpl repoRoot) {
    super(repoRoot);
  }
  
  @Override
  protected ObjectDeserializer<JarFileImpl> makeDeserializer() {
    return JarFileImpl.makeDeserializer(repoRoot);
  }
  
  @Override
  protected JarFileImpl makeJar(RepoFileImpl dir) {
    return JarFileImpl.make(dir);
  }
  
  @Override
  protected JavaProjectImpl createProject(ProjectLocationImpl loc) {
    return new JavaProjectImpl(this, loc);
  }
  
  private boolean aggregating = false;
  private Map<String, Collection<ContentFile>> aggregationMap;
  
  public JarFileImpl getJarFile(ContentFile file) {
    String hash = FileUtils.computeHash(file.getFile().toFile());
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
            files = Helper.newLinkedList();
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
    aggregating = true;
    aggregationMap = Helper.newHashMap();
    for (JavaProjectImpl project : getProjects()) {
      project.getContent().init(true, false);
    }
    
    RepoFileImpl projectDir = repoRoot.getChild(JARS_DIRECTORY).getChild(PROJECT_JARS_DIRECTORY);
    int biggestA = 0;
    int biggestB = 0;
    for (RepoFileImpl a : projectDir.getChildren()) {
      if (a.isDirectory()) {
        try {
          biggestA = Math.max(biggestA, Integer.parseInt(a.getName()));
        } catch (NumberFormatException e) {}
        biggestB = 0;
        for (RepoFileImpl b : a.getChildren()) {
          try {
            biggestB = Math.max(biggestB, Integer.parseInt(b.getName()));
          } catch (NumberFormatException e) {}
          JarFileImpl jar = JarFileImpl.make(b);
          if (jar != null) {
            projectJarIndex.put(jar.getProperties().HASH.getValue(), jar);
          }
        }
      }
    }
    
    RepoFileImpl aDir = projectDir.getChild(Integer.toString(biggestA));
    for (Map.Entry<String, Collection<ContentFile>> entry : aggregationMap.entrySet()) {
      if (biggestB >= 999) {
        aDir = projectDir.getChild(Integer.toString(biggestA++));
        biggestB = 0;
      }
      RepoFileImpl dir = aDir.getChild(Integer.toString(biggestB++));
      
      // Make the directory
      dir.makeDirs();

      // Copy the file
      FileUtils.copyFile(entry.getValue().iterator().next().getFile().toFile(), dir.getChild(JarFileImpl.JAR_NAME).toFile());
      
      // Populate the properties
      JarFileImpl jar = JarFileImpl.make(dir);
      
      JarProperties properties = jar.getProperties();
      
      // Find the most popular name
      CounterSet<String> bestName = new CounterSet<String>();
      for (ContentFile file : entry.getValue()) {
        bestName.increment(file.getFile().getName());
      }
      properties.NAME.setValue(bestName.getMax().getObject());
      properties.HASH.setValue(entry.getKey());
      properties.SOURCE.setValue(JarSource.PROJECT);
      properties.save();
    }
    aggregating = false;
    aggregationMap = null;
    reset();
    clearProjectJarCache();
    for (JavaProjectImpl project : getProjects()) {
      project.getContent().init(false, true);
    }
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
    JarFileImpl newJar = JarFileImpl.make(dir);
    
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
    if (mavenJarIndex.containsKey(hash)) {
      logger.info("Repository already contains a copy of " + group + "." + artifact);
    } else {
      String subDir = group.replace('.', '/') + "/" + artifact + "/" + version;
  
      RepoFileImpl dir = repoRoot.getChild(JARS_DIRECTORY).getChild(MAVEN_JARS_DIRECTORY).getChild(subDir);
      
      // Make the directory
      dir.makeDirs();
      
      // Copy the files
      FileUtils.copyFile(jar, dir.getChild(JarFileImpl.JAR_NAME).toFile());
      if (source != null && source.exists()) {
        FileUtils.copyFile(source, dir.getChild(JarFileImpl.SOURCE_JAR_NAME).toFile());
      }
      
      // Populate the properties
      JarFileImpl newJar = JarFileImpl.make(dir);
      
      JarProperties properties = newJar.getProperties();
      properties.NAME.setValue(artifact);
      properties.GROUP.setValue(group);
      properties.SOURCE.setValue(JarSource.MAVEN);
      properties.HASH.setValue(hash);
      properties.save();
    }
  }
  
  @Override
  public JavaBatchImpl newBatch(RepoFileImpl dir, Integer batch) {
    return new JavaBatchImpl(this, dir, batch);
  }
}