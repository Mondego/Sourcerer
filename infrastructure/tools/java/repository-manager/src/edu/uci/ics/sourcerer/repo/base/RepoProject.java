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
package edu.uci.ics.sourcerer.repo.base;

import edu.uci.ics.sourcerer.repo.base.compressed.CompressedFileSet;
import edu.uci.ics.sourcerer.repo.base.normal.FileSet;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedProject;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.general.ProjectProperties;
import edu.uci.ics.sourcerer.repo.internal.core.RepoFile;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepoProject {
  private Repository repo;
  private RepoFile projectRoot;
  private RepoFile content;
  private RepoFile properties;
  
  public RepoProject(Repository repo, RepoFile projectRoot, RepoFile content) {
    this.repo = repo;
    this.projectRoot = projectRoot;
    this.content = content;
    properties = projectRoot.getChild("project.properties");
  }
 
  public ExtractedProject getExtractedProject(ExtractedRepository repo) {
    return new ExtractedProject(repo.rebasePath(projectRoot), properties.toFile());
  }
  
  public IFileSet getFileSet() {
    if (content.isDirectory()) {
      return new FileSet(this);
    } else {
      if (repo.getTempDir() == null) {
        throw new IllegalStateException("Compressed file sets may only be used if a temp dir is specified.");
      }
      return new CompressedFileSet(this);
    }
  }
  
  public Repository getRepository() {
    return repo;
  }
  
  public RepoFile getProjectRoot() {
    return projectRoot;
  }
  
  public RepoFile getContent() {
    return content;
  }
  
  public RepoFile getProperties() {
    return properties;
  }
  
  public ProjectProperties loadProperties() {
    return ProjectProperties.loadProperties(properties);
  }
  
  public String toString() {
    return projectRoot.toString();
  }
}
