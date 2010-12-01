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
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.uci.ics.sourcerer.repo.base.AbstractFileSet;
import edu.uci.ics.sourcerer.repo.base.JarFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.general.RepoFile;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CompressedFileSet extends AbstractFileSet {
  private RepoProject project;
  private RepoFile base;
  
  private CompressedFileSet(RepoProject project) {
    super(project.getRepository());
    this.project = project;
    base = RepoFile.make(project.getRepository().getTempDir());
  }
  
  public static CompressedFileSet getFileSet(RepoProject project) {
    CompressedFileSet set = new CompressedFileSet(project);
    if (set.populateFileSet()) {
      return set;
    } else {
      return null;
    }
  }
  
  private boolean populateFileSet() {
    ZipFile zip = null;
    try {
      if (project.getContent().toFile().length() == 0) {
        project.getContent().toFile().delete();
        return false;
      }
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
      return true;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading zip: " + project, e);
      return false;
    } finally {
      FileUtils.close(zip);
    }
  }
  
  protected File extractFileToTemp(String relativePath) {
    File tmp = new File(project.getRepository().getTempDir(), relativePath);
    File parentFile = tmp.getParentFile();
    if (parentFile == null) {
      logger.severe("Unable to get parent file: " + tmp.getPath());
      return null;
    } else {
      parentFile.mkdirs();
    }
    
    
    return tmp;
  }
    
  private class CompressedRepoFile extends RepoFile {
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
          ZipEntry entry = zip.getEntry(getRelativePath());
          if (entry != null) {
            InputStream is = zip.getInputStream(entry);
            fos = new FileOutputStream(file);
            byte[] buff = new byte[2048];
            for (int read = is.read(buff); read != -1; read = is.read(buff)) {
              fos.write(buff, 0, read);
            }
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
  }
  
  @Override
  public String convertToRelativePath(String path) {
    return FileUtils.convertToRelativePath(base.toFile().getAbsolutePath(), path);
  }
}
