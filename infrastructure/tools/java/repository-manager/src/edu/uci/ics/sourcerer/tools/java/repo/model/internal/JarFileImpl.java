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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Scanner;

import edu.uci.ics.sourcerer.tools.core.repo.model.RepoFile;
import edu.uci.ics.sourcerer.tools.core.repo.model.internal.RepoFileImpl;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarProperties;
import edu.uci.ics.sourcerer.util.io.CustomSimpleSerializable;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class JarFileImpl implements JarFile, CustomSimpleSerializable {
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
  
  static JarFileImpl make(RepoFileImpl dir) {
    JarProperties properties = new JarProperties(dir.getChild(JAR_PROPERTIES.getValue()));
    RepoFileImpl file = dir.getChild(JAR_NAME.getValue());
    RepoFileImpl sourceFile = dir.getChild(SOURCE_JAR_NAME.getValue());
    if (file.exists()) {
      if (sourceFile.exists()) {
        return new JarFileImpl(properties, file, sourceFile);
      } else {
        return new JarFileImpl(properties, file, null);
      }
    } else {
      logger.severe("Unable to find jar in " + dir);
      return null;
    }
  }

  @Override
  public JarProperties getProperties() {
    return properties;
  }

  @Override
  public RepoFile getFile() {
    return file;
  }

  @Override
  public RepoFile getSourceFile() {
    return sourceFile;
  }

  /* Serialization Related Methods */
  
  @Override
  public String serialize() {
    return file.getRoot().serialize();
  }
  
  public static ObjectDeserializer<JarFileImpl> makeDeserializer(RepoFileImpl repoRoot) {
    final ObjectDeserializer<RepoFileImpl> dirDeserializer = repoRoot.makeDeserializer();
    return new ObjectDeserializer<JarFileImpl>() {
      @Override
      public JarFileImpl deserialize(Scanner scanner) {
        return make(dirDeserializer.deserialize(scanner));
      }
    };
  }
}