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
package edu.uci.ics.sourcerer.repo.base.compressed;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.uci.ics.sourcerer.repo.base.AbstractFileSet;
import edu.uci.ics.sourcerer.repo.base.JarFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.internal.core.RepoFile;
import edu.uci.ics.sourcerer.util.io.FieldConverter;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CompressedFileSet extends AbstractFileSet {
  private RepoProject project;
  private RepoFile base;
  private String baseCanonical;
 
  public CompressedFileSet(RepoProject project) {
    super(project);
    this.project = project;
    base = RepoFile.make(project.getRepository().getTempDir());
    try {
      baseCanonical = base.toFile().getCanonicalPath();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error getting canonical path");
      baseCanonical = base.toFile().getAbsolutePath();
    }
    populateFileSet();
  }
    
  protected void buildRepoMapHelper() {
    ZipFile zip = null;
    try {
      if (project.getContent().toFile().length() == 0) {
        project.getContent().toFile().delete();
      } else {
        zip = new ZipFile(project.getContent().toFile());
        for (Enumeration<? extends ZipEntry> en = zip.entries(); en.hasMoreElements();) {
          ZipEntry entry = en.nextElement();
          String path = entry.getName();
          if (path.endsWith(".jar")) {
            addJarFile(new JarFile(entry.getComment(), new CompressedRepoFile(path.replace(' ', '*'))));
          } else if (path.endsWith(".java")) {
            addJavaFile(new CompressedJavaFile(new CompressedRepoFile(path.replace(' ', '*')), zip.getInputStream(entry)));
          }
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading zip: " + project, e);
    } finally {
      FileUtils.close(zip);
    }
  }
    
  protected class CompressedRepoFile extends RepoFile {
    private boolean tempExtracted;
    
    public CompressedRepoFile(String path) {
      super(base, path);
      tempExtracted = false;
    }

    @Override
    public File toFile() {
      if (tempExtracted) {
        return super.toFile();
      } else {
        File file = super.toFile();
        ZipFile zip = null;
        FileOutputStream fos = null;
        try {
          zip = new ZipFile(project.getContent().toFile());
          String entryName = getRelativePath().replace('*', ' ');
          ZipEntry entry = zip.getEntry(entryName);
          if (entry != null) {
            InputStream is = zip.getInputStream(entry);
            fos = new FileOutputStream(file);
            byte[] buff = new byte[2048];
            for (int read = is.read(buff); read != -1; read = is.read(buff)) {
              fos.write(buff, 0, read);
            }
          } else {
            logger.log(Level.SEVERE, "Unable to find entry: " + entryName);
            return null;
          }
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Unable to write temp file: " + file.getPath(), e);
          return null;
        } finally {
          FileUtils.close(zip);
          FileUtils.close(fos);
        }
        tempExtracted = true;
        return file;
      }
    }
    
    @Override
    public String writeToString() {
      return getRelativePath();
    }
  }
  
  {
    FieldConverter.registerConverterHelper(CompressedRepoFile.class, new FieldConverter.FieldConverterHelper() {
      @Override
      protected Object makeFromScanner(Scanner scanner) throws IllegalAccessException {
        return new CompressedRepoFile(scanner.next());
      }
    });
  }
  
  @Override
  public String convertToRelativePath(String path) {
    return FileUtils.convertToRelativePath(baseCanonical, path);
  }
}
