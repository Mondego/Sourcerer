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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.ContentFile;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.AbstractFileSet;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.ContentFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFileSet;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.EntryWriter;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.PackageExtractor;
import edu.uci.ics.sourcerer.util.io.SimpleDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaFileSetImpl extends AbstractFileSet implements JavaFileSet {
  private final JavaRepositoryImpl repo;
  private Collection<ContentFileImpl> javaFiles;
  private Collection<JavaFileImpl> filteredJavaFiles;
  private Collection<JarFileImpl> jarFiles;
  
  protected JavaFileSetImpl(JavaProjectImpl project) {
    super(project);
    this.repo = project.getRepository();
  }
  
  @Override
  protected void initHelper(boolean reloadNow) {
    if (reloadNow) {
      if (javaFiles == null) {
        javaFiles = Helper.newArrayList();
      } else {
        javaFiles.clear();
      }
      if (filteredJavaFiles == null) {
        filteredJavaFiles = Helper.newArrayList();
      } else {
        filteredJavaFiles.clear();
      }
      if (jarFiles == null) {
        jarFiles = Helper.newArrayList();
      } else {
        jarFiles.clear();
      }
    } else {
      javaFiles = null;
      filteredJavaFiles = null;
      jarFiles = null;
    }
  }
  
  @Override
  protected void populateFileSetHelper() {
    Map<String, Collection<JavaFileImpl>> map = Helper.newHashMap();
    for (ContentFileImpl file : javaFiles) {
      String pkg = PackageExtractor.extractPackage(file.getFile().toFile());
      if (pkg != null) {
        String key = pkg + file.getFile().getName();
        Collection<JavaFileImpl> files = map.get(key);
        if (files == null) {
          files = Helper.newLinkedList();
          map.put(key, files);
        }
        files.add(new JavaFileImpl(pkg, file));
      } else {
//        logger.info("skipping " + file);
      }
    }
    
    for (Collection<JavaFileImpl> files : map.values()) {
      if (files.size() == 1) {
        filteredJavaFiles.addAll(files);
      } else {
        JavaFileImpl best = null;
        for (JavaFileImpl file : files) {
          if (best == null || file.morePopularThan(best)) {
            best = file;
          }
        }
        filteredJavaFiles.add(best);
      }
    }
  }
  
  @Override
  protected void writeExtendedCache(SimpleSerializer serializer) throws IOException {
    EntryWriter<JavaFileImpl> writer = serializer.getEntryWriter(JavaFileImpl.class);
    for (JavaFileImpl file : filteredJavaFiles) {
      writer.write(file);
    }
    writer.close();
  }

  @Override
  protected void readExtendedCache(SimpleDeserializer deserializer) throws IOException {
    filteredJavaFiles = deserializer.deserializeToCollection(JavaFileImpl.makeDeserializer(getRoot()));
  }
  
  @Override
  protected void fileAdded(ContentFileImpl file) {
    if (file.getFile().getName().endsWith(".java")) {
      javaFiles.add(file);
    } else if (file.getFile().getName().endsWith(".jar")) {
      JarFileImpl jar = repo.getJarFile(file);
      if (jar != null) {
        jarFiles.add(jar);
      } else {
//        logger.log(Level.SEVERE, "Jar not aggregated: " + file);
      }
    }
  }
  
  @Override
  public Collection<? extends ContentFile> getJavaFiles() {
    if (javaFiles == null) {
      init(true, false);
    }
    return Collections.unmodifiableCollection(javaFiles);
  }
  
  @Override
  public Collection<? extends JavaFile> getFilteredJavaFiles() {
    if (filteredJavaFiles == null) {
      init(true, false);
    }
    return Collections.unmodifiableCollection(filteredJavaFiles);
  }

  @Override
  public Collection<? extends JarFile> getJarFiles() {
    if (jarFiles == null) {
      init(true, false);
    }
    return Collections.unmodifiableCollection(jarFiles);
  }
}
