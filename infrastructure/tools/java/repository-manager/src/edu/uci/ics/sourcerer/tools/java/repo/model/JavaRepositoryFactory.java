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
package edu.uci.ics.sourcerer.tools.java.repo.model;

import java.io.File;

import edu.uci.ics.sourcerer.tools.core.repo.model.RepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ModifiableExtractedJavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.internal.InternalJavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public interface JavaRepositoryFactory extends RepositoryFactory {
  public static final JavaRepositoryFactory INSTANCE = new InternalJavaRepositoryFactory();
  
  public JavaRepository loadJavaRepository(Argument<File> root);
  
  public ModifiableJavaRepository loadModifiableJavaRepository(Argument<File> root);
  
  public ExtractedJavaRepository loadExtractedJavaRepository(Argument<File> root);
  
  public ModifiableExtractedJavaRepository loadModifiableExtractedJavaRepository(Argument<File> root);
}
