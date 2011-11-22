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

import java.util.Scanner;

import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.internal.IJar;
import edu.uci.ics.sourcerer.tools.java.repo.model.internal.JarFileImpl;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedJarFileImpl implements ModifiableExtractedJarFile, IJar {
  private RepoFileImpl dir;
  
  private ExtractedJarProperties properties;
  
  private ExtractedJarFileImpl(RepoFileImpl dir) {
    this.dir = dir;
    this.properties = new ExtractedJarProperties(dir.getChild(JarFileImpl.JAR_PROPERTIES));
  }
  
  static ExtractedJarFileImpl make(RepoFileImpl dir, JarProperties properties) {
    ExtractedJarFileImpl jar = new ExtractedJarFileImpl(dir);
    jar.properties.copy(properties);
    jar.properties.save();
    return jar;
  }
  
  static ExtractedJarFileImpl make(RepoFileImpl dir) {
    ExtractedJarFileImpl jar = new ExtractedJarFileImpl(dir);
    if (Boolean.TRUE.equals(jar.properties.EXTRACTED.getValue())) {
      return jar;
    } else {
      dir.delete();
      return null;
    }
  }
  
  @Override
  public void reset(JarFile jar) {
    dir.delete();
    dir.makeDirs();
    properties.clear();
    properties.copy(jar.getProperties());
    properties.save();
  }
  
  @Override
  public ExtractedJarProperties getProperties() {
    return properties;
  }
  
  @Override
  public RepoFileImpl getExtractionDir() {
    return dir;
  }
  
  @Override
  public String toString() {
    return properties.NAME.getValue();
  }
  
  /* Serialization Related Methods */
  
  @Override
  public String serialize() {
    return dir.serialize();
  }
  
  public static ObjectDeserializer<ExtractedJarFileImpl> makeDeserializer(RepoFileImpl dir) {
    final ObjectDeserializer<RepoFileImpl> dirDeserializer = dir.makeDeserializer();
    return new ObjectDeserializer<ExtractedJarFileImpl>() {
      @Override
      public ExtractedJarFileImpl deserialize(Scanner scanner) {
        return make(dirDeserializer.deserialize(scanner));
      }
    };
  }
}
