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
package edu.uci.ics.sourcerer.repo.java.core;

import java.util.Scanner;

import edu.uci.ics.sourcerer.repo.core.RepoFile;
import edu.uci.ics.sourcerer.util.io.FieldConverter;
import edu.uci.ics.sourcerer.util.io.LWRec;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class IndexedJar implements LWRec {
  public enum Type {
    PROJECT,
    MAVEN;
  }
  private final Type type;
  private final String hash;
  private final String groupName;
  private final String version;
  private final String artifactName;
  private final RepoFile path;
  private final String jarName;
  private final String sourceName;
  
  /* LWRec Related Methods */
  public static void registerConveterHelper() {
    FieldConverter.registerConverterHelper(IndexedJar.class, new FieldConverter.FieldConverterHelper() {
      @Override
      protected Object makeFromScanner(Scanner scanner) throws IllegalAccessException {
        String value = scanner.next();
      }
    });
  }
  
  @Override
  public String writeToString() {
    if (type == Type.PROJECT) {
      return type.name() + " " +
    }
  }

}
