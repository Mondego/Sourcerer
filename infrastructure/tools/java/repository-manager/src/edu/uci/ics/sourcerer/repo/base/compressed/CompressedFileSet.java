// Sourcerer: an infrastructure for large-scale source code analysis.
// Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
package edu.uci.ics.sourcerer.repo.base.compressed;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.uci.ics.sourcerer.repo.base.AbstractFileSet;
import edu.uci.ics.sourcerer.repo.base.IJavaFile;
import edu.uci.ics.sourcerer.repo.base.JavaFile;
import edu.uci.ics.sourcerer.repo.base.RepoProject;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CompressedFileSet extends AbstractFileSet<CompressedJarFile, CompressedVirtualJavaFile> {
  private RepoProject project;
  private CompressedFileSet(RepoProject project) {
    this.project = project;
  }
  
  public static CompressedFileSet getFileSet(RepoProject project) {
    CompressedFileSet set = new CompressedFileSet(project);
    if (set.populateFileSet()) {
      return set;
    } else {
      return null;
    }
  }
  public String getBasePath() {
    return project.getRepository().getTempDir().getPath();
//    return project.getRepository().getRoot().getPath();
  }
  
  private boolean populateFileSet() {
    ZipFile zip = null;
    try {
      if (project.getContent().length() == 0) {
        project.getContent().delete();
        return false;
      }
      zip = new ZipFile(project.getContent());
      for (Enumeration<? extends ZipEntry> en = zip.entries(); en.hasMoreElements();) {
        ZipEntry entry = en.nextElement();
        String path = entry.getName();
        if (path.endsWith(".jar")) {
          addJarFile(new CompressedJarFile(project.getRepository(), path, entry.getSize(), entry.getComment()));
        } else if (path.endsWith(".java")) {
          addJavaFile(new CompressedVirtualJavaFile(path, zip.getInputStream(entry)));
        }
      }
      return true;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading zip: " + project.getContent().getPath(), e);
      return false;
    } finally {
      try {
        if (zip != null) {
          zip.close();
        }
      } catch (IOException e) {}
    }
  }

  public Iterable<ReadableCompressedJarFile> getCompressedJarFiles() {
    return new Iterable<ReadableCompressedJarFile>(){
      @Override
      public Iterator<ReadableCompressedJarFile> iterator() {
        try {
          return new Iterator<ReadableCompressedJarFile>() {
            ZipFile zip = new ZipFile(project.getContent());
            @SuppressWarnings("unchecked")
            Iterator<CompressedJarFile> iter = ((Collection<CompressedJarFile>)(Object)getJarFiles()).iterator();
            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }
           
            @Override
            public ReadableCompressedJarFile next() {
              try {
                CompressedJarFile next = iter.next();
                ZipEntry entry = zip.getEntry(next.getZipPath());
                return new ReadableCompressedJarFile(next, zip.getInputStream(entry));
              } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading from zip file: " + project.getContent().getPath(), e);
                return null;
              }
            }
          
            @Override
            public boolean hasNext() {
              boolean hasNext = iter.hasNext();
              if (!hasNext) {
                try {
                  zip.close();
                } catch(IOException e) {}
              }
              return hasNext;
            }
          };
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Unable to open content file: " + project.getContent().getPath());
          return null;
        }
      }
    };
  }
  
  @Override
  protected Iterable<IJavaFile> convertJavaToConcrete(final Collection<CompressedVirtualJavaFile> files) {
    return new Iterable<IJavaFile>() {
      @Override
      public Iterator<IJavaFile> iterator() {
        try {
          return new Iterator<IJavaFile>() {
            Iterator<CompressedVirtualJavaFile> javaFiles = files.iterator();
            ZipFile zip = new ZipFile(project.getContent());
            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }
            
            @Override
            public IJavaFile next() {
              try {
                CompressedVirtualJavaFile next = javaFiles.next();
                ZipEntry entry = zip.getEntry(next.getPath());
                File tempFile = createTempFile(zip.getInputStream(entry), next);
                return new JavaFile(next.getDir(), next.getPackage(), tempFile);
              } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading from zip: " + project.getContent());
                return null;
              }
            }
          
            @Override
            public boolean hasNext() {
              boolean hasNext = javaFiles.hasNext();
              if (!hasNext) {
                try {
                  zip.close();
                } catch (IOException e) {}
              }
              return hasNext;
            }
          };
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Unable to open content file: " + project.getContent().getPath());
          return null;
        }
      }
    };
  }
  
  private File createTempFile(InputStream is, CompressedVirtualJavaFile file) {
    File tmp = new File(project.getRepository().getTempDir(), file.getPath());
    tmp.getParentFile().mkdirs();
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(tmp);
      byte[] buff = new byte[2048];
      for (int read = is.read(buff); read != -1; read = is.read(buff)) {
        fos.write(buff, 0, read);
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write temp file: " + tmp.getPath(), e);
      return null;
    } finally {
      try {
        fos.close();
      } catch (IOException e) {}
      try {
        is.close();
      } catch (IOException e) {}
    }
    return tmp;
  }
}
