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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.uci.ics.sourcerer.tools.core.repo.model.internal.AbstractRepoProject;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.ProjectLocationImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProjectProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJavaRepository;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class ExtractedJavaProjectImpl extends AbstractRepoProject<ExtractedJavaRepositoryImpl, ExtractedJavaProjectProperties> implements ModifiableExtractedJavaProject {
  private final RepoFileImpl zip;
  
  private ExtractedJavaProjectImpl(ExtractedJavaRepositoryImpl repo, ProjectLocationImpl loc) {
    super(repo, loc);
    zip = loc.getProjectRoot().getChild(COMPRESSED_OUTPUT.getValue());
  }

  static ExtractedJavaProjectImpl make(ExtractedJavaRepositoryImpl repo, ProjectLocationImpl loc) {
    ExtractedJavaProjectImpl project = new ExtractedJavaProjectImpl(repo, loc);
    if (!Boolean.TRUE.equals(project.getProperties().EXTRACTED.getValue())) {
      loc.getProjectRoot().delete();
    }
    return project;
  }
  
  @Override
  public ModifiableExtractedJavaRepository getRepository() {
    return repo;
  }
  
  @Override
  public void compress() {
    Collection<File> compressed = new LinkedList<>();
    
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip.toFile()))) {
      zos.setMethod(ZipOutputStream.DEFLATED);
      zos.setLevel(9);
      
      // Look at the files in the directory, and only compress those that end in .txt or .xml
      for (File file : loc.getProjectRoot().toFile().listFiles()) {
        if (file.isFile() && (file.getName().endsWith(".txt") || file.getName().endsWith(".xml"))) {
          ZipEntry entry = new ZipEntry(file.getName());
          zos.putNextEntry(entry);
          FileUtils.writeFileToStream(file, zos);
          zos.closeEntry();
          compressed.add(file);
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error compressing output.", e);
      return;
    }
    
    // Delete the compressed files
    for (File file : compressed) {
      if (!file.delete()) {
        logger.severe("Unable to delete: " + file.getPath());
      }
    }
  }
  
  @Override
  public RepoFileImpl getCompressedFile() {
    return zip;
  }
  
  @Override
  public boolean isCompressed() {
    return zip.exists();
  }
  
  @Override
  public void reset(JavaProject project) {
    loc.getProjectRoot().delete();
    loc.getProjectRoot().makeDirs();
    ExtractedJavaProjectProperties props = getProperties();
    props.clear();
    props.copy(project.getProperties());
    props.save();
  }
  
  @Override
  protected ExtractedJavaProjectProperties makeProperties(RepoFileImpl propFile) {
    return new ExtractedJavaProjectProperties(propFile);
  }
  
  @Override
  public RepoFileImpl getExtractionDir() {
    return loc.getProjectRoot();
  }
}
