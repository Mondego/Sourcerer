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
package edu.uci.ics.sourcerer.repo.base;

import static edu.uci.ics.sourcerer.util.io.Logging.RESUME;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import edu.uci.ics.sourcerer.repo.AbstractRepository;
import edu.uci.ics.sourcerer.repo.IndexedJar;
import edu.uci.ics.sourcerer.repo.JarIndex;
import edu.uci.ics.sourcerer.repo.RepoJar;
import edu.uci.ics.sourcerer.repo.RepoPath;
import edu.uci.ics.sourcerer.repo.properties.ProjectProperties;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Properties;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.properties.IntegerProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Repository extends AbstractRepository {
  public static final Property<Integer> SPLIT_SIZE = new IntegerProperty("split-size", 1000, "Number of projects per split fragment.");
  
  private File tempDir;
  private Map<String, RepoProject> projects;
  
  private Repository(File repoRoot, File tempDir) {
    super(repoRoot);
    this.tempDir = tempDir;
  }
  
  @Override
  protected void addProject(RepoPath path) {
    RepoPath content = path.getChild("content");
    if (!content.exists()) {
      content = path.getChild("content.zip");
    }
    if (content.exists()) {
      File properties = path.getChildFile("project.properties");
      RepoProject project = new RepoProject(this, content, properties);
      projects.put(project.getProjectPath(), project);
    }
  }
 
  public static Repository getRepository(File repoRoot) {
    return new Repository(repoRoot, null);
  }
  
  public static Repository getRepository(File repoRoot, File tempDir) {
    return new Repository(repoRoot, tempDir);
  }
  
  public void createJarIndex() {
    JarIndex.createJarIndexFile(this);
  }
  
  public void printJarStats() {
    JarIndex.printJarStats(getJarsPath().toFile());
  }
  
  public void printProjectNames() {
    logger.info("Loading projects...");
    
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(PROJECT_NAMES_FILE);
    printer.beginTable(3);
    printer.addDividerRow();
    printer.addRow("host", "project", "crawled date");
    printer.addDividerRow();
    for (RepoProject project : getProjects()) {
      ProjectProperties props = project.getProperties();
      printer.beginRow();
      printer.addCell(props.getOriginRepo());
      printer.addCell(props.getName());
      printer.addCell(props.getCrawledDate());
    }
    printer.endTable();
    printer.close();
    logger.info("Done!");
  }
  
  public void aggregateJarFiles() {
    JarIndex.aggregateJars(this);
  }
  
  public void splitProjectsForFilterList() {
    logger.info("Loading projects...");
    File outputDir = Properties.OUTPUT.getValue();
    
    BufferedWriter bw = null;
    try {
      int filterNumber = 1;
      bw = new BufferedWriter(new FileWriter(new File(outputDir, "project-filter-" + filterNumber++ + ".txt")));
      int count = 0;
      for (RepoProject project : getProjects()) {
        bw.write(project.getProjectPath() + "\n");
        if (++count == SPLIT_SIZE.getValue()) {
          logger.info("  " + count + " projects written");
          bw.close();
          bw = new BufferedWriter(new FileWriter(new File(outputDir, "project-filter-" + filterNumber++ + ".txt")));
          count = 0;
        }
      }
      logger.info("Done!");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing filter list", e);
    } finally {
      FileUtils.close(bw);
    }
  }
  
  public void generateJarFilterList(Set<String> completed) {
    logger.info("Looking through project jars...");
    for (RepoProject project : getProjects(FileUtils.getFileAsSet(PROJECT_FILTER.getValue()))) {
      IFileSet fileSet = project.getFileSet();
      for (IJarFile jar : fileSet.getJarFiles()) {
        if (!completed.contains(jar.getHash())) {
          logger.log(Logging.RESUME, jar.getHash());
        }
      }
    }
    logger.info("Done!");
  }

  public static void migrateRepository(File source, File target, Set<String> completed) {
    logger.info("--- Migrating and compressing repository from " + source.getPath() + " to " + target.getPath() + " ---");
    
    logger.info("Loading source repository...");
    Repository repo = getRepository(source, null);
    logger.info(repo.projects.size() + " projects found");
    
    logger.info("Initializing target repository...");
    if (!target.exists()) {
      target.mkdirs();
    }
    Repository targetRepo = new Repository(target, null);
    
    logger.info("Migrating " + repo.jarIndex.getIndexSize() + " jar files...");
    File jars = targetRepo.getJarsPath().toFile();
    if (!jars.exists()) {
      jars.mkdir();
    }
    for (IndexedJar jar : repo.getJarIndex().getJars()) {
      if (!completed.contains(jar.toString())) {
        jar.migrateIndexedJar(targetRepo.getBaseDir().getPath());
        logger.log(RESUME, jar.toString());
      }
    }
    if (repo.jarIndexFile.exists()) {
      if (!(targetRepo.jarIndexFile.exists() && completed.contains(targetRepo.jarIndexFile.getName()))) {
        FileUtils.copyFile(repo.jarIndexFile, targetRepo.jarIndexFile);
        logger.log(RESUME, targetRepo.jarIndexFile.getName());
      }
    }
    
    logger.info("Migrating " + repo.projects.size() + " projects.");
    for (RepoProject project : repo.getProjects()) {
      File targetProject = new File(target, project.getProjectPath());
      File content = project.getContent().toFile();
      File contentTarget = new File(targetProject, "content.zip");
      if (contentTarget.exists() && completed.contains(project.getProjectPath())) {
        logger.info(project.getProjectPath() + " already migrated.");
      } else {
        logger.info("Migrating " + project.getProjectPath());
        
        if (!targetProject.exists()) {
          targetProject.mkdirs();
        }
        boolean success = false;
        if (content.isFile()) {
          success = FileUtils.copyFile(content, contentTarget);
        } else {
          success = zipContent(content, contentTarget);
        }
        if (success) {
          File properties = new File(content.getParentFile(), "project.properties");
          if (properties.exists()) {
            File targetProperties = new File(targetProject, "project.properties");
            success = FileUtils.copyFile(properties, targetProperties);
          } else {
            logger.warning("No properties file for: " + project.getProjectPath());
          }
          if (success) {
            logger.log(RESUME, project.getProjectPath());
          }
        }
      }
    }
  }

  private static boolean zipContent(File input, File output) {
    ZipOutputStream zos = null;
    boolean delete = false;
    try {
      int offset = input.getPath().length() + 1;
      byte[] buff = new byte[2048];
      zos = new ZipOutputStream(new FileOutputStream(output));
      
      Deque<File> stack = Helper.newStack();
      stack.push(input);
      while (!stack.isEmpty()) {
        for (File file : stack.pop().listFiles()) {
          if (file.isDirectory()) {
            stack.add(file);
          } else {
            if (file.exists()) {
              try {
                String hash = null;
                if (file.getName().endsWith(".jar")) {
                  hash = RepoJar.getHash(file);
                }
                FileInputStream fis = new FileInputStream(file);
                try {
                  ZipEntry entry = new ZipEntry(file.getPath().substring(offset).replace('\\', '/'));
                  if (hash != null) {
                    entry.setComment(hash);
                  }
                  zos.putNextEntry(entry);
                  int read = 0;
                  while ((read = fis.read(buff)) != -1) {
                    zos.write(buff, 0, read);
                  }
                } finally {
                  FileUtils.close(fis);
                }
              } catch (IOException e) {
                logger.log(Level.SEVERE, "Unable to write zip entry for file: " + file.getName(), e);
              }
            }
          }
        }
      }
      return true;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to create content file.", e);
      delete = true;
      return false;
    } finally {
      FileUtils.close(zos);
      if (delete) {
        output.delete();
      }
    }
  }
    
  public Collection<RepoProject> getProjects() {
    return getProjects(null);
  }
  
  public Collection<RepoProject> getFilteredProjects() {
    if (PROJECT_FILTER.hasValue()) {
      return getProjects(FileUtils.getFileAsSet(PROJECT_FILTER.getValue()));
    } else {
      return getProjects(null);
    }
  }
  
  public Collection<RepoProject> getProjects(Set<String> filter) {
    if (filter == null) {
      if (projects == null) {
        projects = Helper.newHashMap();
        populateRepository();
      }
      return projects.values();
    } else {
      if (filter.isEmpty()) {
        logger.log(Level.SEVERE, "Empty project filter!");
      }
      Collection<RepoProject> result = Helper.newArrayList();
      if (projects == null) {
        projects = Helper.newHashMap();
        populateFilteredRepository(filter);
        for (RepoProject project : projects.values()) {
          result.add(project);
        }
        projects = null;
      } else {
        for (RepoProject project : projects.values()) {
          if (filter.contains(project.getProjectPath())) {
            result.add(project);
          }
        }
      }
      return result;
    }
  }
  
  public RepoProject getProject(String projectPath) {
    if (projects == null) {
      projects = Helper.newHashMap();
      populateRepository();
    }
    return projects.get(projectPath);
  }
  
  public byte[] getFile(String projectPath, String path) {
    path = path.replace('*', ' ');
    
    RepoProject project = getProject(projectPath);
    File content = project.getContent().toFile();
    if (content.isDirectory()) {
      File file = new File(repoRoot, path);
      if (file.exists()) {
        return FileUtils.getFileAsByteArray(file);
      } else {
        return null;
      }
    } else {
      ZipFile zip = null;
      try {
        zip = new ZipFile(content);
        path = path.substring(1);
        ZipEntry entry = zip.getEntry(path);
        if (entry != null) {
          return FileUtils.getInputStreamAsByteArray(zip.getInputStream(entry), (int)entry.getSize());
        } else {
          return null;
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to read zip file", e);
        return null;
      } finally {
        FileUtils.close(zip);
      }
    }
  }
  
  public File getRoot() {
    return repoRoot;
  }
  
  public File getTempDir() {
    if (tempDir == null) {
      IllegalStateException e = new IllegalStateException("May not use temp dir without initializing it.");
      logger.log(Level.SEVERE, "Temp dir is null!", e);
      throw e;
    } else {
      return tempDir;
    }
  }
}
