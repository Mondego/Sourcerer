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
package edu.uci.ics.sourcerer.tools.java.model.types;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public enum ComponentRelation {
  CLUSTER_CONTAINS_CLUSTER_VERSION,
  CLUSTER_CONTAINS_CORE_TYPE,
  CLUSTER_CONTAINS_VERSION_TYPE,
  CLUSTER_VERSION_CONTAINS_TYPE_VERSION,
  JAR_CONTAINS_CLUSTER,
  JAR_CONTAINS_CLUSTER_VERSION,
  JAR_CONTAINS_TYPE_VERSION,
  JAR_MATCHES_LIBRARY_VERSION,
  LIBRARY_CONTAINS_JAR,
  LIBRARY_MATCHES_CLUSTER,
  LIBRARY_CONTAINS_CLUSTER,
  LIBRARY_CONTAINS_LIBRARY_VERSION,
  LIBRARY_VERSION_CONTAINS_CLUSTER,
  LIBRARY_VERSION_CONTAINS_CLUSTER_VERSION,
  LIBRARY_VERSION_CONTAINS_TYPE_VERSION,
  LIBRARY_VERSION_CONTAINS_LIBRARY,
  LIBRARY_VERSION_CONTAINS_LIBRARY_VERSION,
  ;
}
