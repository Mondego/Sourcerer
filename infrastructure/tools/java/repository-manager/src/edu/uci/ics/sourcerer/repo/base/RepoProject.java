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

import java.io.File;

import edu.uci.ics.sourcerer.repo.RepoPath;
import edu.uci.ics.sourcerer.repo.base.compressed.CompressedFileSet;
import edu.uci.ics.sourcerer.repo.base.normal.FileSet;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedProject;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.properties.ProjectProperties;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepoProject {
  private Repository repo;
  private RepoPath content;
  private File properties;
  
  public RepoProject(Repository repo, RepoPath content, File properties) {
    this.repo = repo;
    this.content = content;
    this.properties = properties;
  }
 
  public ExtractedProject getExtractedProject(ExtractedRepository repo) {
    return new ExtractedProject(repo.convertPath(content.getParent()), properties);
  }
  
//  public String getOutputPath(File baseDir) {
//    return baseDir.getPath() + File.separatorChar + getProjectPath();
//  }
//  
  public String getProjectPath() {
    return content.getParent().getRelativePath();
  }
  
  public IFileSet getFileSet() {
    File contentFile = content.toFile();
    if (contentFile.isDirectory()) {
      return new FileSet(contentFile, repo);
    } else {
      if (repo.getTempDir() == null) {
        throw new IllegalStateException("Compressed file sets may only be used if a temp dir is specified.");
      }
      return CompressedFileSet.getFileSet(this);
    }
  }
  
  public Repository getRepository() {
    return repo;
  }
  
  public RepoPath getContent() {
    return content;
  }
  
//  public File getPropertiesFile() {
//    return properties;
//  }
  
  public ProjectProperties getProperties() {
    return ProjectProperties.load(properties);
  }
  
  public String toString() {
    return getProjectPath();
  }
}
