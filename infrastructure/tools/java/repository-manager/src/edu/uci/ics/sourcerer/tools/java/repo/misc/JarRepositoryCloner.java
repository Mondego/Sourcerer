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
package edu.uci.ics.sourcerer.tools.java.repo.misc;

import java.io.File;

import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.ModifiableJavaRepository;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarRepositoryCloner {
  public static final Argument<File> JAR_FILTER_FILE = new FileArgument("jar-filter-file", null, "Jar filter file");
  
  public static void cloneJarRepositoryFragment() {
    JavaRepository repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    ModifiableJavaRepository newRepo = JavaRepositoryFactory.INSTANCE.loadModifiableJavaRepository(JavaRepositoryFactory.OUTPUT_REPO);

    for (String hash : FileUtils.readFileToCollection(JAR_FILTER_FILE.getValue())) {
      JarFile jar = repo.getJarFile(hash);
      newRepo.addMavenJarFile(jar.getFile().toFile(), jar.getSourceFile() == null ? null : jar.getSourceFile().toFile(), jar.getProperties().GROUP.getValue(), jar.getProperties().NAME.getValue(), jar.getProperties().VERSION.getValue());
    }
  }
}
