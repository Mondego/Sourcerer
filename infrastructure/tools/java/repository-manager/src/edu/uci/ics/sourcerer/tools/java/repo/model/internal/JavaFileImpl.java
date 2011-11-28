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
package edu.uci.ics.sourcerer.tools.java.repo.model.internal;

import java.util.Scanner;

import edu.uci.ics.sourcerer.tools.core.repo.model.internal.ContentDirectoryImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.ContentFileImpl;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFile;
import edu.uci.ics.sourcerer.util.io.CustomSerializable;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaFileImpl implements JavaFile, CustomSerializable {
  private String pkg;
  private ContentFileImpl file;

  protected JavaFileImpl(String pkg, ContentFileImpl file) {
    if (pkg == null || pkg.equals("")) {
      this.pkg = null;
    } else {
      this.pkg = pkg;
    }
    this.file = file;
  }
  
  @Override
  public RepoFileImpl getFile() {
    return file.getFile();
  }

  @Override
  public ContentDirectoryImpl getParentDirectory() {
    return file.getParentDirectory();
  }

  @Override
  public String getPackage() {
    return pkg;
  }
  
  public boolean morePopularThan(JavaFileImpl other) {
    return file.getParentDirectory().getCount() > other.getParentDirectory().getCount();
  }

  /* Serialization Related Methods */
  public static ObjectDeserializer<JavaFileImpl> makeDeserializer(final ContentDirectoryImpl root) {
    final ObjectDeserializer<RepoFileImpl> fileDeserializer = root.getFile().makeDeserializer();
    return new ObjectDeserializer<JavaFileImpl>() {
      @SuppressWarnings("unchecked")
      ObjectDeserializer<String> stringDeserializer = (ObjectDeserializer<String>) ObjectDeserializer.makeDeserializer(String.class);
      
      @Override
      public JavaFileImpl deserialize(Scanner scanner) {
        String pkg = stringDeserializer.deserialize(scanner);
        RepoFileImpl file = fileDeserializer.deserialize(scanner);
        ContentFileImpl contentFile = root.make(file.getParent()).makeFile(file);
        return new JavaFileImpl(pkg, contentFile);
      }
    };
  }
  
  @Override
  public String serialize() {
    return pkg + " " + file.getFile().serialize();
  }
  
  @Override
  public String toString() {
    return file.toString();
  }
}
