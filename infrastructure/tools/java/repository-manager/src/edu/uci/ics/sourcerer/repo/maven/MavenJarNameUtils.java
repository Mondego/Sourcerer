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
package edu.uci.ics.sourcerer.repo.maven;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class MavenJarNameUtils {
  private static final Pattern pattern = Pattern.compile("([a-zA-Z-]*)-((\\w+\\.)*\\w+)(-(.*?))?\\.jar");
  
  public static String getProjectName(String jar) {
    Matcher matcher = pattern.matcher(jar);
    if (matcher.matches()) {
      return matcher.group(1);
    } else {
      throw new IllegalArgumentException("The input does not match the expected format: " + jar);
    }
  }
  
  public static String getProjectVersion(String jar) {
    Matcher matcher = pattern.matcher(jar);
    if (matcher.matches()) {
      return matcher.group(2);
    } else {
      throw new IllegalArgumentException("The input does not match the expected format: " + jar);
    }
  }
  
  public static String getExtra(String jar) {
    Matcher matcher = pattern.matcher(jar);
    if (matcher.matches()) {
      return matcher.group(5);
    } else {
      throw new IllegalArgumentException("The input does not match the expected format: " + jar);
    }
  }
}
