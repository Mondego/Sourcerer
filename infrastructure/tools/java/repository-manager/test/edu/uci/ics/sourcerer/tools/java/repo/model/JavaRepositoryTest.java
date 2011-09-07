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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.uci.ics.sourcerer.util.io.arguments.Command;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaRepositoryTest {
  public static final Command COMMAND = new Command("test", "Run a junit test.") {
    @Override
    protected void action() {
    }
  }.setProperties(JavaRepositoryFactory.INPUT_REPO);
  
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  
  @Before
  public void initialize() {
    // Initializes the logging
    Command.execute(new String[] { "--test" }, JavaRepositoryTest.class);
  }
  
  @Test
  public void testJavaRepository() {
    JavaRepositoryFactory.INPUT_REPO.setValue(folder.newFolder("repo"));
    
    {
      // Create the repository
      ModifiableJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadModifiableJavaRepository(JavaRepositoryFactory.INPUT_REPO);
      
      ModifiableJavaBatch batch = repo.createBatch();
      {
        ModifiableJavaProject project = batch.createProject();
        project.getProperties().NAME.setValue("Repository Manager");
        project.getProperties().save();
        
        JavaFileSet files = project.getContent();
        
        Assert.assertEquals(0, files.getFiles().size());
        Assert.assertFalse(files.getRoot().getAllFiles().iterator().hasNext());
        
        project.addContent(new File("./src"));
        
        Assert.assertTrue(files.getFiles().size() > 0);
        Assert.assertTrue(files.getJavaFiles().size() > 0);
        Assert.assertTrue(files.getJavaFiles().size() > files.getFilteredJavaFiles().size());
        Assert.assertTrue(files.getFilteredJavaFiles().size() > 0);
        Assert.assertEquals(0, files.getJarFiles().size());
      }
      {
        ModifiableJavaProject project = batch.createProject();
        
        JavaFileSet files = project.getContent();
        
        Assert.assertEquals(0, files.getFiles().size());
        Assert.assertFalse(files.getRoot().getAllFiles().iterator().hasNext());
        
        project.addContent(new File("../../../../lib"));
        
        Assert.assertTrue(files.getFiles().size() > 0);
        Assert.assertEquals(0, files.getJavaFiles().size());
        Assert.assertEquals(0, files.getJarFiles().size());
        
        repo.aggregateJarFiles();
        
        Assert.assertTrue(files.getFiles().size() > 0);
        Assert.assertEquals(0, files.getJavaFiles().size());
        Assert.assertTrue(files.getJarFiles().size() > 0);
      }
    }
    {
      // Load the repository
      JavaRepository repo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JavaRepositoryFactory.INPUT_REPO);
      
      {
        JavaProject project = repo.getProject(0, 0);
        JavaFileSet files = project.getContent();
  
        Assert.assertTrue(files.getFiles().size() > 0);
        Assert.assertTrue(files.getJavaFiles().size() > 0);
        Assert.assertTrue(files.getJavaFiles().size() > files.getFilteredJavaFiles().size());
        Assert.assertTrue(files.getFilteredJavaFiles().size() > 0);
        Assert.assertEquals(0, files.getJarFiles().size());
      }
      
      {
        JavaProject project = repo.getProject(0, 1);
        JavaFileSet files = project.getContent();
        
        Assert.assertTrue(files.getFiles().size() > 0);
        Assert.assertEquals(0, files.getJavaFiles().size());
        Assert.assertTrue(files.getJarFiles().size() > 0);
      }
    }
  }
}
