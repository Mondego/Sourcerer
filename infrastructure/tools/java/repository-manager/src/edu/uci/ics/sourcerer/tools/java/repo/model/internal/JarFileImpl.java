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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Scanner;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarProperties;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class JarFileImpl implements JarFile, IJar {
  public static final Argument<String> JAR_NAME = new StringArgument("jar-name", "jar.jar", "Name of indexed jar files.").permit();
  public static final Argument<String> SOURCE_JAR_NAME = new StringArgument("source-jar-name", "source.jar", "Name of indexed source jar files.").permit();

  private JarProperties properties;
  
  private RepoFileImpl file;
  private RepoFileImpl sourceFile;
  
  private JarFileImpl(JarProperties properties, RepoFileImpl file, RepoFileImpl sourceFile) {
    this.properties = properties;
    this.file = file;
    this.sourceFile = sourceFile;
  }
  
  static JarFileImpl load(RepoFileImpl dir) {
    RepoFileImpl root = dir.asRoot();
    RepoFileImpl propFile = root.getChild(JAR_PROPERTIES.getValue());
    if (propFile.exists()) {
      JarProperties properties = new JarProperties(propFile);
      RepoFileImpl file = root.getChild(JAR_NAME.getValue());
      RepoFileImpl sourceFile = root.getChild(SOURCE_JAR_NAME.getValue());
      if (file.exists()) {
        // Verify the hash exists
        String hash = properties.HASH.getValue();
        if (hash == null) {
          hash = FileUtils.computeHash(file.toFile());
          properties.HASH.setValue(hash);
          properties.save();
        }
        if (sourceFile.exists()) {
          return new JarFileImpl(properties, file, sourceFile);
        } else {
          return new JarFileImpl(properties, file, null);
        }
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
  
  static JarFileImpl create(RepoFileImpl dir) {
    RepoFileImpl root = dir.asRoot();
    RepoFileImpl propFile = root.getChild(JAR_PROPERTIES.getValue());
    if (propFile.exists()) {
      logger.log(Level.SEVERE, "Cannot create a JarFileImpl where one already exists: " + dir);
      return null;
    } else {
      JarProperties properties = new JarProperties(propFile);
      RepoFileImpl file = root.getChild(JAR_NAME.getValue());
      RepoFileImpl sourceFile = root.getChild(SOURCE_JAR_NAME.getValue());
      if (file.exists()) {
        if (sourceFile.exists()) {
          return new JarFileImpl(properties, file, sourceFile);
        } else {
          return new JarFileImpl(properties, file, null);
        }
      } else {
        return null;
      }
    }
  }

  @Override
  public JarProperties getProperties() {
    return properties;
  }

  @Override
  public RepoFileImpl getFile() {
    return file;
  }

  @Override
  public RepoFileImpl getSourceFile() {
    return sourceFile;
  }
  
  @Override
  public String toString() {
    String group = properties.GROUP.getValue();
    String name = properties.NAME.getValue();
    String version = properties.VERSION.getValue();
    if (version == null) {
      return properties.NAME.getValue();
    } else {
      StringBuilder builder = new StringBuilder();
      if (group != null) {
        builder.append(group).append('.');
      }
      builder.append(name);
      if (version != null) {
        builder.append(" (").append(version).append(")");
      }
      return builder.toString();
    }
  }

  /* Serialization Related Methods */
  
  @Override
  public String serialize() {
    return file.getRoot().serialize();
  }
  
  public static ObjectDeserializer<JarFileImpl> makeDeserializer(RepoFileImpl dir) {
    final ObjectDeserializer<RepoFileImpl> dirDeserializer = dir.makeDeserializer();
    return new ObjectDeserializer<JarFileImpl>() {
      @Override
      public JarFileImpl deserialize(Scanner scanner) {
        return load(dirDeserializer.deserialize(scanner));
      }
    };
  }
}