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
package edu.uci.ics.sourcerer.repo.maven;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;
import static edu.uci.ics.sourcerer.util.io.Logging.RESUME;

import java.io.File;
import java.util.Set;

import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MavenToRepositoryAdder {
  public static void addToRepository() {
    Set<String> resume = Logging.initializeResumeLogger();
    PropertyManager properties = PropertyManager.getProperties();
    
    Repository repo = Repository.getUninitializedRepository();
    File jarsDir = repo.getJarsDir();
    
    if (!jarsDir.exists()) {
      jarsDir.mkdir();
    }
    
    File mavenDownloadDir = properties.getValueAsFile(Property.INPUT);
    for (File jar : mavenDownloadDir.listFiles()) {
      if (jar.isFile() && jar.getName().endsWith(".jar")) {
        if (!resume.contains(jar.getName())) {
          String project = MavenJarNameUtils.getProjectName(jar.getName());
          File projectDir = new File(jarsDir, project);
          if (!projectDir.exists()) {
            projectDir.mkdir();
          }
          File newJar = new File(projectDir, jar.getName());
          if (FileUtils.copyFile(jar, newJar)) {
            logger.log(RESUME, jar.getName());
          }
        }
      }
    }
  }
}
