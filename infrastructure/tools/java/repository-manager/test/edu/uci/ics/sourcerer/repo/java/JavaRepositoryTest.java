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
package edu.uci.ics.sourcerer.repo.java;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.uci.ics.sourcerer.repo.core.CoreRepositoryTest;
import edu.uci.ics.sourcerer.util.io.arguments.ArgumentManager;
import edu.uci.ics.sourcerer.util.io.arguments.Command;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaRepositoryTest {
  public static final Command COMMAND = new Command("test", "Run a junit test.") {
    @Override
    protected void action() {
    }
  };
  
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  
  @Before
  public void initialize() {
    // Initializes the logging
    ArgumentManager.executeCommand(new String[] { "--test" }, CoreRepositoryTest.class);
  }
  
  @Test
  public void test() {
    
  }
}
