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
package edu.uci.ics.sourcerer.tools.java.repo.model.extracted.internal;

import edu.uci.ics.sourcerer.tools.core.repo.model.internal.ProjectLocationImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.internal.AbstractJavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.internal.JarFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.internal.JavaProjectImpl;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ExtractedJavaRepositoryImpl extends AbstractJavaRepository<ExtractedJavaProjectImpl, ExtractedJavaBatchImpl, ExtractedJarFileImpl> implements ModifiableExtractedJavaRepository {
  public ExtractedJavaRepositoryImpl(RepoFileImpl repoRoot) {
    super(repoRoot);
  }
  
  @Override
  protected ObjectDeserializer<ExtractedJarFileImpl> makeDeserializer() {
    return ExtractedJarFileImpl.makeDeserializer(repoRoot);
  }
  
  @Override
  protected ExtractedJarFileImpl loadJar(RepoFileImpl dir) {
    return ExtractedJarFileImpl.make(dir);
  }
  
  @Override
  protected ExtractedJavaProjectImpl createProject(ProjectLocationImpl loc) {
    return ExtractedJavaProjectImpl.make(this, loc);
  }

  @Override
  public ExtractedJavaBatchImpl newBatch(RepoFileImpl dir, Integer batch) {
    return new ExtractedJavaBatchImpl(this, dir, batch);
  }
  
  @Override
  public ExtractedJavaProjectImpl getMatchingProject(JavaProject project) {
    JavaProjectImpl cast = (JavaProjectImpl) project;
    ProjectLocationImpl loc = cast.getLocation();
    ExtractedJavaProjectImpl result = getProject(loc.getBatchNumber(), loc.getCheckoutNumber());
    if (result == null) {
      clearCache();
      result = addProject(loc.getBatchNumber(), loc.getCheckoutNumber());
      result.getProperties().copy(project.getProperties());
      result.getProperties().save();
    }
    return result;
  }

  @Override
  public ExtractedJarFileImpl getMatchingJarFile(JarFile jar) {
    JarFileImpl cast = (JarFileImpl) jar;
    ExtractedJarFileImpl result = getJarFile(jar.getProperties().HASH.getValue());
    if (result == null) {
      RepoFileImpl output = cast.getFile().getRoot().reroot(repoRoot);
      result = ExtractedJarFileImpl.make(output, jar.getProperties());
      switch (result.getProperties().SOURCE.getValue()) {
        case JAVA_LIBRARY:
          libraryJarIndex.put(result.getProperties().HASH.getValue(), result);
          clearLibraryJarCache();
          break;
        case MAVEN:
          mavenJarIndex.put(result.getProperties().HASH.getValue(), result);
          clearMavenJarCache();
          break;
        case PROJECT:
          projectJarIndex.put(result.getProperties().HASH.getValue(), result);
          clearProjectJarCache();
          break;
      }
    }
    return result;
  }
}
