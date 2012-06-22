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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.ContentFile;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.AbstractFileSet;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.AbstractRepository;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.ContentFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFileSet;
import edu.uci.ics.sourcerer.util.io.EntryWriter;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.PackageExtractor;
import edu.uci.ics.sourcerer.util.io.SimpleDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaFileSetImpl extends AbstractFileSet implements JavaFileSet {
  public static final Argument<String> JAVA_FILE_CACHE = new StringArgument("java-file-cache-file", "java-file-cache.txt", "Cache of the file set's java files.").permit();
  
  private final JavaRepositoryImpl repo;
  private Collection<ContentFileImpl> javaFiles;
  private Collection<JavaFileImpl> filteredJavaFiles;
  private Map<String, JarFileImpl> jarFiles;
  
  private final File cache;
  
  protected JavaFileSetImpl(JavaProjectImpl project) {
    super(project);
    this.repo = project.getRepo();
    cache = new File(cacheDir, JAVA_FILE_CACHE.getValue());
    new File(project.getLocation().getProjectRoot().toFile(), JAVA_FILE_CACHE.getValue()).delete();
  }
  
  @Override
  public void init(boolean loadNow, boolean clearCache) {
    if (loadNow) {
      if (javaFiles == null) {
        javaFiles = new ArrayList<>();
      } else {
        javaFiles.clear();
      }
      if (filteredJavaFiles == null) {
        filteredJavaFiles = new ArrayList<>();
      } else {
        filteredJavaFiles.clear();
      }
      if (jarFiles == null) {
        jarFiles = new HashMap<>();
      } else {
        jarFiles.clear();
      }
    } else {
      javaFiles = null;
      filteredJavaFiles = null;
      jarFiles = null;
    }
    super.init(loadNow, clearCache);
  }
  
  @Override
  protected void populateFileSet() {
    super.populateFileSet();
    if (AbstractRepository.CLEAR_CACHES.getValue() || !cache.exists() || !readCache()) {
      Map<String, Collection<JavaFileImpl>> map = new HashMap<>();
      for (ContentFileImpl file : javaFiles) {
        String pkg = PackageExtractor.extractPackage(file.getFile().toFile());
        if (pkg != null) {
          String key = pkg + file.getFile().getName();
          Collection<JavaFileImpl> files = map.get(key);
          if (files == null) {
            files = new LinkedList<>();
            map.put(key, files);
          }
          files.add(new JavaFileImpl(pkg, file));
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
      
      try (SimpleSerializer writer = IOUtils.makeSimpleSerializer(cache)) {
        EntryWriter<JavaFileImpl> er = writer.getEntryWriter(JavaFileImpl.class);
        for (JavaFileImpl file : filteredJavaFiles) {
          er.write(file);
        }
        er.close();
        return;
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to write file cache", e);
      }
      FileUtils.delete(cache);
    }
  }
  
  private boolean readCache() {
    try (SimpleDeserializer reader = IOUtils.makeSimpleDeserializer(cache)) {
      filteredJavaFiles = reader.deserializeToCollection(JavaFileImpl.makeDeserializer(getRoot()));
      return true;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to load java file cache: " + getRoot(), e);
      return false;
    }
  }
  
  @Override
  protected void fileAdded(ContentFileImpl file) {
    if (file.getFile().getName().endsWith(".java")) {
      javaFiles.add(file);
    } else if (file.getFile().getName().endsWith(".jar")) {
      JarFileImpl jar = repo.getJarFile(file);
      if (jar != null) {
        String hash = jar.getProperties().HASH.getValue();
        if (!jarFiles.containsKey(hash)) {
          jarFiles.put(hash, jar);
        }
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
    return Collections.unmodifiableCollection(jarFiles.values());
  }
}
