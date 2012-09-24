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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarProperties;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.internal.IJar;
import edu.uci.ics.sourcerer.tools.java.repo.model.internal.JarFileImpl;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedJarFileImpl implements ModifiableExtractedJarFile, IJar {
  private final RepoFileImpl dir;
  private final RepoFileImpl zip;
  
  private ExtractedJarProperties properties;
  
  private ExtractedJarFileImpl(RepoFileImpl dir) {
    this.dir = dir;
    this.zip = dir.getChild(COMPRESSED_OUTPUT.getValue());
    this.properties = new ExtractedJarProperties(dir.getChild(JarFileImpl.JAR_PROPERTIES));
  }
  
  static ExtractedJarFileImpl create(RepoFileImpl dir, JarProperties properties) {
    ExtractedJarFileImpl jar = new ExtractedJarFileImpl(dir);
    jar.properties.copy(properties);
    jar.properties.save();
    return jar;
  }
  
  static ExtractedJarFileImpl create(RepoFileImpl dir) {
    ExtractedJarFileImpl jar = new ExtractedJarFileImpl(dir);
    if (Boolean.TRUE.equals(jar.properties.EXTRACTED.getValue())) {
      return jar;
    } else {
//      dir.delete(); this was messing up maven!
      return null;
    }
  }
  
  @Override
  public void compress() {
    Collection<File> compressed = new LinkedList<>();
    
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip.toFile()))) {
      zos.setMethod(ZipOutputStream.DEFLATED);
      zos.setLevel(9);
      
      // Look at the files in the directory, and only compress those that end in .txt
      for (File file : dir.toFile().listFiles()) {
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
  public void reset(JarFile jar) {
    // TODO this is dangerous for nested maven jars
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
        return create(dirDeserializer.deserialize(scanner));
      }
    };
  }
}
