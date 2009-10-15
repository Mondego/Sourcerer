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
package edu.uci.ics.sourcerer.repo.extracted;

import java.io.File;
import java.util.Collection;

import edu.uci.ics.sourcerer.repo.AbstractRepository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.PropertyOld;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedRepository extends AbstractRepository {
  private Collection<ExtractedLibrary> libraries;
  private Collection<ExtractedJar> jars;
  private Collection<ExtractedProject> projects;
  
  private ExtractedRepository(File repoRoot) {
    super(repoRoot);
  }
  
  @Override
  protected void addFile(File checkout) {
    projects.add(new ExtractedProject(checkout, checkout.getParentFile().getName() + File.separatorChar + checkout.getName()));
  }
  
  private void populateLibraries() {
    libraries = Helper.newLinkedList();
    File libsDir = getLibsDir();
    for (File lib : libsDir.listFiles()) {
      if (lib.isDirectory()) {
        libraries.add(new ExtractedLibrary(lib));
      }
    }
  }

  private void populateJars() {
    jars = Helper.newLinkedList();
    File jarsDir = getJarsDir();
    for (File jar : jarsDir.listFiles()) {
      if (jar.isDirectory()) {
        jars.add(new ExtractedJar(jar));
      }
    }
  }
 
  public static ExtractedRepository getRepository() {
    return getRepository(PropertyManager.getProperties().getValueAsFile(PropertyOld.REPO_ROOT));
  }
  
  public static ExtractedRepository getRepository(File repoRoot) {
    return new ExtractedRepository(repoRoot);
  }
  
  public static ExtractedRepository getUninitializedRepository(File repoRoot) {
    return new ExtractedRepository(repoRoot);
  }
  
  public Collection<ExtractedLibrary> getLibraries() {
    if (libraries == null) {
      populateLibraries();
    }
    return libraries;
  }
  
  public Collection<ExtractedJar> getJars() {
    if (jars == null) {
      populateJars();
    }
    return jars;
  }
  
  public Collection<ExtractedProject> getProjects() {
    if (projects == null) {
      projects = Helper.newLinkedList();
      populateRepository();
    }
    return projects;
  }
}
