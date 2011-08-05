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
package edu.uci.ics.sourcerer.tools.core.repo;

import java.io.File;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.uci.ics.sourcerer.tools.core.repo.model.IBatch;
import edu.uci.ics.sourcerer.tools.core.repo.model.IBatchM;
import edu.uci.ics.sourcerer.tools.core.repo.model.IContentFile;
import edu.uci.ics.sourcerer.tools.core.repo.model.IFileSet;
import edu.uci.ics.sourcerer.tools.core.repo.model.IRepo;
import edu.uci.ics.sourcerer.tools.core.repo.model.IRepoM;
import edu.uci.ics.sourcerer.tools.core.repo.model.ISourceProject;
import edu.uci.ics.sourcerer.tools.core.repo.model.ISourceProjectM;
import edu.uci.ics.sourcerer.tools.core.repo.model.RepositoryFactory;
import edu.uci.ics.sourcerer.util.io.arguments.Command;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CoreRepositoryTest {
  public static final Command COMMAND = new Command("test", "Run a junit test.") {
    @Override
    protected void action() {
    }
  };
  
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  
  @BeforeClass
  public static void initialize() {
    // Initializes the logging
    Command.execute(new String[] { "--test" }, CoreRepositoryTest.class);
    
  }
  
  @Test
  public void testProjectCreation() {
    RepositoryFactory.INPUT_REPO.setValue(folder.newFolder("repo"));
    
    {
      IRepoM<? extends ISourceProjectM, ? extends IBatchM<? extends ISourceProjectM>> repo = RepositoryFactory.INSTANCE.loadModifiableSourceRepository(RepositoryFactory.INPUT_REPO);
      Assert.assertEquals(0, repo.getProjectCount());
      
      {
        // Create a new batch
        IBatchM<? extends ISourceProjectM> batch = repo.createBatch();
        batch.getProperties().DESCRIPTION.setValue("A test batch");
        batch.getProperties().save();
        
        Assert.assertEquals(0, batch.getBatchNumber().intValue());
        Assert.assertEquals(0, batch.getProjectCount());
  
        {
          // Create a new project
          ISourceProjectM project = batch.createProject();
          
          Assert.assertEquals("0/0", project.getLocation().getProjectRoot().getRelativePath().toString());
          project.getProperties().NAME.setValue("Test project A");
          project.getProperties().save();
        }
        
        {
          // Create a new project
          ISourceProjectM project = batch.createProject();
          
          Assert.assertEquals("0/1", project.getLocation().getProjectRoot().getRelativePath().toString());
          project.getProperties().NAME.setValue("Test project B");
          project.getProperties().save();
        }
        
        Assert.assertEquals(2, batch.getProjectCount());
      }
      {
        // Create a new batch
        IBatchM<? extends ISourceProjectM> batch = repo.createBatch();
        batch.getProperties().DESCRIPTION.setValue("A second test batch");
        batch.getProperties().save();
        
        Assert.assertEquals(1, batch.getBatchNumber().intValue());
        Assert.assertEquals(0, batch.getProjectCount());
  
        {
          // Create a new project
          ISourceProjectM project = batch.createProject();
          
          Assert.assertEquals("1/0", project.getLocation().getProjectRoot().getRelativePath().toString());
          project.getProperties().NAME.setValue("Test project C");
          project.getProperties().save();
          
          
        }
        
        {
          // Create a new project
          ISourceProjectM project = batch.createProject();
          
          Assert.assertEquals("1/1", project.getLocation().getProjectRoot().getRelativePath().toString());
          project.getProperties().NAME.setValue("Test project D");
          project.getProperties().save();
        }
        Assert.assertEquals(2, batch.getProjectCount());
      }
      Assert.assertEquals(4, repo.getProjectCount());
      Assert.assertEquals(2, repo.getProjectCount());
    }
    
    {
      // Reload the repo and make sure everything works
      IRepo<? extends ISourceProject, ? extends IBatch<? extends ISourceProject>> repo = RepositoryFactory.INSTANCE.loadSourceRepository(RepositoryFactory.INPUT_REPO);
      
      Assert.assertEquals(4, repo.getProjectCount());
      Assert.assertEquals(2, repo.getProjectCount());

      {
        Iterator<? extends IBatch<? extends ISourceProject>> iter = repo.getBatches().iterator();
        Assert.assertEquals("A test batch", iter.next().getProperties().DESCRIPTION.getValue());
        Assert.assertEquals("A second test batch", iter.next().getProperties().DESCRIPTION.getValue());
        Assert.assertFalse(iter.hasNext());
      }
      
      {
        Iterator<? extends ISourceProject> iter = repo.getProjects().iterator();
        Assert.assertEquals("Test project A", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project B", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project C", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project D", iter.next().getProperties().NAME.getValue());
        Assert.assertFalse(iter.hasNext());
      }
    }
    
    {
      // Reload one last time to check for the cache
      IRepo<? extends ISourceProject, ? extends IBatch<? extends ISourceProject>> repo = RepositoryFactory.INSTANCE.loadSourceRepository(RepositoryFactory.INPUT_REPO);
      
      Assert.assertEquals(4, repo.getProjectCount());
      Assert.assertEquals(2, repo.getProjectCount());

      {
        Iterator<? extends IBatch<? extends ISourceProject>> iter = repo.getBatches().iterator();
        Assert.assertEquals("A test batch", iter.next().getProperties().DESCRIPTION.getValue());
        Assert.assertEquals("A second test batch", iter.next().getProperties().DESCRIPTION.getValue());
        Assert.assertFalse(iter.hasNext());
      }
      
      {
        Iterator<? extends ISourceProject> iter = repo.getProjects().iterator();
        Assert.assertEquals("Test project A", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project B", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project C", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project D", iter.next().getProperties().NAME.getValue());
        Assert.assertFalse(iter.hasNext());
      }
    }
  }
  
  @Test
  public void testAddingProjectContent() {
    RepositoryFactory.INPUT_REPO.setValue(folder.newFolder("add-content"));
    
    // Create the repository
    IRepoM<? extends ISourceProjectM, ? extends IBatchM<? extends ISourceProjectM>> repo = RepositoryFactory.INSTANCE.loadModifiableSourceRepository(RepositoryFactory.INPUT_REPO);
    
    IBatchM<? extends ISourceProjectM> batch = repo.createBatch();
    
    ISourceProjectM project = batch.createProject();
    
    IFileSet files = project.getContent();
    
    Assert.assertEquals(0, files.getFiles().size());
    Assert.assertFalse(files.getRoot().getAllFiles().iterator().hasNext());
    
    project.addContent(new File("./test/resources/project-content"));
    
    Assert.assertEquals(3, files.getFiles().size());
    Iterator<? extends IContentFile> iter = files.getFiles().iterator();
    Assert.assertEquals("A.txt", iter.next().getFile().getName());
    Assert.assertEquals("subdir/B.txt", iter.next().getFile().getRelativePath().toString());
    Assert.assertEquals("subdir/subsubdir/B.txt", iter.next().getFile().getRelativePath().toString());
    Assert.assertFalse(iter.hasNext());
  }
}
