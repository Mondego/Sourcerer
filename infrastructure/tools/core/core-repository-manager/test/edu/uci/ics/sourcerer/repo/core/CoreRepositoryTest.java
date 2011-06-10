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
package edu.uci.ics.sourcerer.repo.core;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.uci.ics.sourcerer.util.io.arguments.ArgumentManager;
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
  
  @Before
  public void initialize() {
    // Initializes the logging
    ArgumentManager.executeCommand(new String[] { "--test" }, CoreRepositoryTest.class);
  }
  
  @Test
  public void testRepositoryCreation() {
    // Load up an empty repository
    AbstractRepository.INPUT_REPO.setValue(folder.newFolder("repo"));
    
    {
      SourceRepository repo = SourceRepository.make(AbstractRepository.INPUT_REPO);
      Assert.assertEquals(0, repo.getProjects().size());
      
      {
        // Create a new batch
        AbstractRepository<SourceProject>.Batch batch = repo.createBatch();
        batch.getProperties().DESCRIPTION.setValue("A test batch");
        batch.getProperties().save();
        
        Assert.assertEquals(0, batch.getBatchNumber().intValue());
        Assert.assertEquals(0, batch.getProjects().size());
  
        {
          // Create a new project
          SourceProject project = batch.createProject();
          
          Assert.assertEquals(0, project.getLocation().getBatchNumber().intValue());
          Assert.assertEquals(0, project.getLocation().getCheckoutNumber().intValue());
          Assert.assertEquals("0/0", project.getLocation().getProjectRoot().getRelativePath().toString());
          project.getProperties().NAME.setValue("Test project A");
          project.getProperties().save();
        }
        
        {
          // Create a new project
          SourceProject project = batch.createProject();
          
          Assert.assertEquals(0, project.getLocation().getBatchNumber().intValue());
          Assert.assertEquals(1, project.getLocation().getCheckoutNumber().intValue());
          Assert.assertEquals("0/1", project.getLocation().getProjectRoot().getRelativePath().toString());
          project.getProperties().NAME.setValue("Test project B");
          project.getProperties().save();
        }
        
        Assert.assertEquals(2, batch.getProjects().size());
      }
      {
        // Create a new batch
        AbstractRepository<SourceProject>.Batch batch = repo.createBatch();
        batch.getProperties().DESCRIPTION.setValue("A second test batch");
        batch.getProperties().save();
        
        Assert.assertEquals(1, batch.getBatchNumber().intValue());
        Assert.assertEquals(0, batch.getProjects().size());
  
        {
          // Create a new project
          SourceProject project = batch.createProject();
          
          Assert.assertEquals(1, project.getLocation().getBatchNumber().intValue());
          Assert.assertEquals(0, project.getLocation().getCheckoutNumber().intValue());
          Assert.assertEquals("1/0", project.getLocation().getProjectRoot().getRelativePath().toString());
          project.getProperties().NAME.setValue("Test project C");
          project.getProperties().save();
          
          
        }
        
        {
          // Create a new project
          SourceProject project = batch.createProject();
          
          Assert.assertEquals(1, project.getLocation().getBatchNumber().intValue());
          Assert.assertEquals(1, project.getLocation().getCheckoutNumber().intValue());
          Assert.assertEquals("1/1", project.getLocation().getProjectRoot().getRelativePath().toString());
          project.getProperties().NAME.setValue("Test project D");
          project.getProperties().save();
        }
        Assert.assertEquals(2, batch.getProjects().size());
      }
      Assert.assertEquals(4, repo.getProjects().size());
      Assert.assertEquals(2, repo.getBatches().size());
    }
    
    {
      // Reload the repo and make sure everything works
      SourceRepository repo = SourceRepository.make(AbstractRepository.INPUT_REPO);
      
      Assert.assertEquals(4, repo.getProjects().size());
      Assert.assertEquals(2, repo.getBatches().size());

      {
        Iterator<AbstractRepository<SourceProject>.Batch> iter = repo.getBatches().iterator();
        Assert.assertEquals("A test batch", iter.next().getProperties().DESCRIPTION.getValue());
        Assert.assertEquals("A second test batch", iter.next().getProperties().DESCRIPTION.getValue());
        Assert.assertFalse(iter.hasNext());
      }
      
      {
        Iterator<SourceProject> iter = repo.getProjects().iterator();
        Assert.assertEquals("Test project A", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project B", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project C", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project D", iter.next().getProperties().NAME.getValue());
        Assert.assertFalse(iter.hasNext());
      }
    }
    
    {
      // Reload one last time to check for the cache
      SourceRepository repo = SourceRepository.make(AbstractRepository.INPUT_REPO);
      
      Assert.assertEquals(4, repo.getProjects().size());
      Assert.assertEquals(2, repo.getBatches().size());

      {
        Iterator<AbstractRepository<SourceProject>.Batch> iter = repo.getBatches().iterator();
        Assert.assertEquals("A test batch", iter.next().getProperties().DESCRIPTION.getValue());
        Assert.assertEquals("A second test batch", iter.next().getProperties().DESCRIPTION.getValue());
        Assert.assertFalse(iter.hasNext());
      }
      
      {
        Iterator<SourceProject> iter = repo.getProjects().iterator();
        Assert.assertEquals("Test project A", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project B", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project C", iter.next().getProperties().NAME.getValue());
        Assert.assertEquals("Test project D", iter.next().getProperties().NAME.getValue());
        Assert.assertFalse(iter.hasNext());
      }
    }
  }
}
