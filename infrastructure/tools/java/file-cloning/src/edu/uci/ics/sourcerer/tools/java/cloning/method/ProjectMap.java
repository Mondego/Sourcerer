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
package edu.uci.ics.sourcerer.tools.java.cloning.method;

import java.util.Collection;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.cloning.method.fingerprint.FingerprintFactory;
import edu.uci.ics.sourcerer.tools.java.cloning.method.fingerprint.FingerprintFile;
import edu.uci.ics.sourcerer.tools.java.cloning.method.fqn.FqnFile;
import edu.uci.ics.sourcerer.tools.java.cloning.method.hash.HashedFile;
import edu.uci.ics.sourcerer.tools.java.cloning.pairwise.ProjectMatchSet;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ProjectMap {
  private KeyFactory keyFactory;
  private FingerprintFactory fingerprintFactory;
  private Map<String, Project> projectMap;

  public ProjectMap() {
    keyFactory = new KeyFactory();
    fingerprintFactory = new FingerprintFactory();
    projectMap = Helper.newHashMap();
  }
  
  private Project getProject(String path) {
    Project project = projectMap.get(path);
    if (project == null) {
      project = new Project(path);
      projectMap.put(path, project);
    }
    return project;
  }
  
  public Collection<Project> getProjects() {
    return projectMap.values();
  }
  
  public void filterFiles() {
    for (Project project : projectMap.values()) {
      project.filterFiles();
    }
  }
  
  public ProjectMatchSet getProjectMatchSet() {
    return new ProjectMatchSet(getProjects());
  }
  
  public void addFile(FqnFile file) {
    getProject(file.getProject()).getFile(file.getPath()).setFqnKey(keyFactory.getFqnKey(file));
  }
  
  public void addFile(HashedFile file) {
    getProject(file.getProject()).getFile(file.getPath()).setHashKey(keyFactory.getHashKey(file));
  }
  
  public void addFile(FingerprintFile file) {
    getProject(file.getProject()).getFile(file.getPath()).setFingerprintKey(fingerprintFactory.getNameFingerprintKey(file));
  }

  public File getFile(String project, String file) {
    return getProject(project).getFile(file);
  }
  
  public KeyFactory getKeyFactory() {
    return keyFactory;
  }
  
  public FingerprintFactory getFingerprintFactory() {
    return fingerprintFactory;
  }
}
