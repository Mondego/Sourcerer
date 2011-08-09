package edu.uci.ics.sourcerer.tools.core.repo.model;
///* 
// * Sourcerer: an infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
//
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.uci.ics.sourcerer.repo.core;
//
//import static edu.uci.ics.sourcerer.util.io.Logging.RESUME;
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.Collection;
//import java.util.Deque;
//import java.util.Map;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipFile;
//import java.util.zip.ZipOutputStream;
//
//import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
//import edu.uci.ics.sourcerer.repo.general.IndexedJar;
//import edu.uci.ics.sourcerer.repo.general.JarIndex;
//import edu.uci.ics.sourcerer.repo.general.RepoFile;
//import edu.uci.ics.sourcerer.repo.internal.core.RepoProject;
//import edu.uci.ics.sourcerer.util.Helper;
//import edu.uci.ics.sourcerer.util.io.FileUtils;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class FRepository extends AbstractRepository {
//  private File tempDir;
//  private Map<String, RepoProject> projects;
//  
//  private FRepository(File repoRoot, File tempDir) {
//    super(repoRoot);
//    this.tempDir = tempDir;
//  }
//  
//  public static FRepository getRepository(File repoRoot) {
//    return new FRepository(repoRoot, null);
//  }
//  
//  public static FRepository getRepository(File repoRoot, File tempDir) {
//    return new FRepository(repoRoot, tempDir);
//  }
//  
//  @Override
//  protected void addProject(RepoFile path) {
//    RepoFile content = path.getChild("content");
//    if (!content.exists()) {
//      content = path.getChild("content.zip");
//    }
//    if (content.exists()) {
//      RepoProject project = new RepoProject(this, path, content);
//      projects.put(project.getProjectRoot().getRelativePath(), project);
//    }
//  }
//  
//  @Override
//  protected void addLibrary(RepoFile path) {}
// 
//  public void createJarIndex() {
//    JarIndex.createJarIndexFile(this);
//  }
//   
//  public void aggregateJarFiles() {
//    JarIndex.aggregateJars(this);
//  }
//
//  public static void migrateRepository(File source, File target, Set<String> completed) {
//    logger.info("--- Migrating and compressing repository from " + source.getPath() + " to " + target.getPath() + " ---");
//    
//    logger.info("Loading source repository...");
//    FRepository repo = getRepository(source, null);
//    logger.info(repo.projects.size() + " projects found");
//    
//    logger.info("Initializing target repository...");
//    if (!target.exists()) {
//      target.mkdirs();
//    }
//    FRepository targetRepo = new FRepository(target, null);
//    
//    logger.info("Migrating " + repo.jarIndex.getIndexSize() + " jar files...");
//    for (IndexedJar jar : repo.getJarIndex().getJars()) {
//      if (!completed.contains(jar.toString())) {
//        jar.migrateIndexedJar(targetRepo.getRoot());
//        logger.log(RESUME, jar.toString());
//      }
//    }
//    if (repo.jarIndexFile.exists()) {
//      if (!(targetRepo.jarIndexFile.exists() && completed.contains(targetRepo.jarIndexFile.toFile().getName()))) {
//        FileUtils.copyFile(repo.jarIndexFile.toFile(), targetRepo.jarIndexFile.toFile());
//        logger.log(RESUME, targetRepo.jarIndexFile.getName());
//      }
//    }
//    
//    logger.info("Migrating " + repo.projects.size() + " projects.");
//    for (RepoProject project : repo.getProjects()) {
//      RepoFile targetProject = targetRepo.repoRoot.getChild(project.getProjectRoot().getRelativePath());
//
//      File content = project.getContent().toFile();
//      File contentTarget = targetProject.getChildFile("content.zip");
//      if (contentTarget.exists() && completed.contains(project.getProjectRoot().getRelativePath())) {
//        logger.info(project + " already migrated.");
//      } else {
//        logger.info("Migrating " + project);
//        
//        boolean success = false;
//        if (content.isFile()) {
//          success = FileUtils.copyFile(content, contentTarget);
//        } else {
//          success = zipContent(content, contentTarget);
//        }
//        if (success) {
//          RepoFile properties = project.getProperties();
//          if (properties.exists()) {
//            RepoFile targetProperties = targetProject.rebaseFile(properties);
//            success = FileUtils.copyFile(properties.toFile(), targetProperties.toFile());
//          } else {
//            logger.warning("No properties file for: " + project);
//          }
//          if (success) {
//            logger.log(RESUME, project.getProjectRoot().getRelativePath());
//          }
//        }
//      }
//    }
//  }
//
//  private static boolean zipContent(File input, File output) {
//    ZipOutputStream zos = null;
//    boolean delete = false;
//    try {
//      int offset = input.getPath().length() + 1;
//      byte[] buff = new byte[2048];
//      zos = new ZipOutputStream(new FileOutputStream(output));
//      
//      Deque<File> stack = Helper.newStack();
//      stack.push(input);
//      while (!stack.isEmpty()) {
//        for (File file : stack.pop().listFiles()) {
//          if (file.isDirectory()) {
//            stack.add(file);
//          } else {
//            if (file.exists()) {
//              try {
//                String hash = null;
//                if (file.getName().endsWith(".jar")) {
//                  hash = FileUtils.computeHash(file);
//                }
//                FileInputStream fis = new FileInputStream(file);
//                try {
//                  ZipEntry entry = new ZipEntry(file.getPath().substring(offset).replace('\\', '/'));
//                  if (hash != null) {
//                    entry.setComment(hash);
//                  }
//                  zos.putNextEntry(entry);
//                  int read = 0;
//                  while ((read = fis.read(buff)) != -1) {
//                    zos.write(buff, 0, read);
//                  }
//                } finally {
//                  FileUtils.close(fis);
//                }
//              } catch (IOException e) {
//                logger.log(Level.SEVERE, "Unable to write zip entry for file: " + file.getName(), e);
//              }
//            }
//          }
//        }
//      }
//      return true;
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Unable to create content file.", e);
//      delete = true;
//      return false;
//    } finally {
//      FileUtils.close(zos);
//      if (delete) {
//        output.delete();
//      }
//    }
//  }
//  
//  private void loadProjects() {
//    if (projects == null) {
//      projects = Helper.newHashMap();
//      if (PROJECT_FILTER.hasValue()) {
//        Set<String> filter = FileUtils.getFileAsSet(PROJECT_FILTER.getValue());
//        if (filter.isEmpty()) {
//          logger.log(Level.SEVERE, "Empty project filter!");
//        }
//        populateFilteredRepository(filter);
//      } else {
//        populateProjects();
//      }
//    }
//  }
//  
//  /**
//   * This method loads the project listing, if necessary. The project
//   * listing is loaded only once. The PROJECT_FILTER property 
//   * dictates which projects are loaded.
//   * 
//   * @see AbstractRepository#PROJECT_FILTER
//   */
//  public Collection<RepoProject> getProjects() {
//    if (projects == null) {
//      loadProjects();
//    }
//    return projects.values();
//  }
//  
//  public int getProjectCount() {
//    return getProjects().size();
//  }
//  
//  /**
//   * This method loads the project listing, if necessary. The project
//   * listing is loaded only once. The PROJECT_FILTER property 
//   * dictates which projects are loaded.
//   * 
//   * @see AbstractRepository#PROJECT_FILTER
//   */
//  public RepoProject getProject(String projectPath) {
//    if (projects == null) {
//      loadProjects();
//    }
//    return projects.get(projectPath);
//  }
//  
//  public JarIndex getJarIndex() {
//    return super.getJarIndex();
//  }
//  
//  /**
//   * This method loads the project listing, if necessary. The project
//   * listing is loaded only once. The PROJECT_FILTER property 
//   * dictates which projects are loaded.
//   * 
//   * @see AbstractRepository#PROJECT_FILTER
//   */
//  public byte[] getFile(String projectPath, String path) {
//    path = path.replace('*', ' ');
//    
//    RepoProject project = getProject(projectPath);
//    RepoFile content = project.getContent();
//    if (content.isDirectory()) {
//      File file = content.getChildFile(path);
//      if (file.exists()) {
//        return FileUtils.getFileAsByteArray(file);
//      } else {
//        return null;
//      }
//    } else {
//      ZipFile zip = null;
//      try {
//        zip = new ZipFile(content.toFile());
//        path = path.substring(1);
//        ZipEntry entry = zip.getEntry(path);
//        if (entry != null) {
//          return FileUtils.getInputStreamAsByteArray(zip.getInputStream(entry), (int)entry.getSize());
//        } else {
//          return null;
//        }
//      } catch (IOException e) {
//        logger.log(Level.SEVERE, "Unable to read zip file", e);
//        return null;
//      } finally {
//        FileUtils.close(zip);
//      }
//    }
//  }
//  
//  public String getFilePath(String projectPath, String path) {
//    path = path.replace('*', ' ');
//    
//    RepoProject project = getProject(projectPath);
//    RepoFile content = project.getContent();
//    if (content.isDirectory()) {
//      File file = content.getChildFile(path);
//      if (file.exists()) {
//        return file.getAbsolutePath();
//      } else {
//        return null;
//      }
//    } else {
//      return "C:" + project.getProjectRoot().getChild("content").getChildFile(path).getAbsolutePath();
//    }
//  }
//  
//  protected RepoFile getRoot() {
//    return repoRoot;
//  }
//  
//  public File getTempDir() {
//    if (tempDir == null) {
//      IllegalStateException e = new IllegalStateException("May not use temp dir without initializing it.");
//      logger.log(Level.SEVERE, "Temp dir is null!", e);
//      throw e;
//    } else {
//      return tempDir;
//    }
//  }
//}
