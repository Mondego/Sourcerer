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
package edu.uci.ics.sourcerer.tools.core.repo.model;

import java.io.File;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public interface ModifiableSourceProject extends ModifiableProject, SourceProject {
  @Override
  public ModifiableSourceRepository getRepository();
  
  /**
   * Deletes the project's contents.
   *  
   * @return <tt>true</tt> if successful
   */
  public boolean deleteContent();
  
  public void delete(DeletionFilter deleter);
  
  public interface DeletionFilter {
    public boolean shouldDelete(RepoFile file);
  }
  
  /**
   * Copies the contents of <tt>file</tt> into the
   * project's <tt>content</tt>> directory. Will
   * not work if the project's contents are compressed.
   * 
   * This will not overwrite anything.
   */
  public boolean addContent(File file);
  
  public boolean addContent(ContentAdder adder);
  
  public interface ContentAdder {
    public boolean addContent(File file);
  }
}
