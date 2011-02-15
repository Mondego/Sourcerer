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
package edu.uci.ics.sourcerer.repo.tools;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.general.RepoFile;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepositoryCompressor {
  public static final Property<String> COMPRESSED_FILTERED_REPO_FILE = new StringProperty("compressed-filtered-repo-file", "filtered-repo.zip", "The compressed file containing the filtered repository.");
  /**
   * Creates a compressed version of the repository. Each
   * project is individually compressed and then the entire
   * repository is zipped together.
   */
  public static void compressFilteredRepository(Repository repo) {
    // Get a temporary root for the compressed repository
    RepoFile newRoot = RepoFile.make(new File(FileUtils.getTempDir(), "newRoot"));
    
    logger.info("Beginning filtered repository compression...");
    int count = 0;
    int total = repo.getProjectCount();
    // Go through every project and compress it
    for (RepoProject project : repo.getProjects()) {
      logger.info("Processing project " + ++count + " of " + total);
      // Make sure the project has at least one file
      IFileSet files = project.getFileSet();
      if (files.getTotalFileCount() > 0) {
        ZipOutputStream zos = null;
        try {
          zos = new ZipOutputStream(new FileOutputStream(newRoot.rebaseFile(project.getProjectRoot()).getChildFile("project.zip")));
//          zos.setLevel(9);
          // Add the properties file
          FileInputStream fis = null;
          // Messy, but only want the entire writing to fail if the output stream has issues
          try {
            fis = new FileInputStream(project.getProperties().toFile());
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to open file: " + project.getProperties().toString(), e);
            fis = null;
          }
          if (fis != null) {
            try {
              zos.putNextEntry(new ZipEntry(project.getProperties().getName()));
              FileUtils.writeStreamToStream(fis, zos);
            } finally {
              FileUtils.close(fis);
            }
            // Add the individual files
            for (IJavaFile file : files.getJavaFiles()) {
              RepoFile rFile = file.getFile();
              try {
                fis = new FileInputStream(rFile.toFile());
              } catch (IOException e) {
                logger.log(Level.SEVERE, "Unable to open file: " + rFile.toString());
              }
              if (fis != null) {
                try {
                  zos.putNextEntry(new ZipEntry("content/" + rFile.getRelativePath()));
                  FileUtils.writeStreamToStream(fis, zos);
                } finally {
                  FileUtils.close(fis);
                }
              }
            }
          }
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Unable to write project zip.", e);
        } finally {
          FileUtils.close(zos);
        }
      }
    }
    
    // Compress the repository
    FileUtils.zipFile(newRoot.toFile(), COMPRESSED_FILTERED_REPO_FILE);
    
    // Clean temp files
    FileUtils.cleanTempDir();
  }
}
