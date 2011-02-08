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
package edu.uci.ics.sourcerer.repo.general;

import static edu.uci.ics.sourcerer.util.io.Logging.RESUME;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import edu.uci.ics.sourcerer.repo.base.IFileSet;
import edu.uci.ics.sourcerer.repo.base.IJarFile;
import edu.uci.ics.sourcerer.repo.base.JarNamer;
import edu.uci.ics.sourcerer.repo.base.RepoProject;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarIndex {
  public enum MavenFilter {
    NONE,
    LATEST,
    MANUAL,
    ALL;
  }
  
  public enum ProjectFilter {
    NONE,
    MANUAL,
    ALL;
  }
  
  private Map<String, IndexedJar> index;
  private Map<String, IndexedJar> nameIndex;
  
  private JarIndex() {
    index = Helper.newHashMap();
    nameIndex = Helper.newHashMap();
  }
  
  public void cleanManifestFiles() {
    logger.info("--- Cleaning manifest files for " + index.size() + " jars ---");
    int count = 0;
    int rewriteCount = 0;
    for (IndexedJar jar : index.values()) {
      logger.info("Checking " + jar + " (" + ++count + " of " + index.size() + ")");
      File file = jar.getJarFile();
      
      // Find if the jar contains a problematic manifest
      byte[] newManifest = null;
      ZipFile zip = null;
      BufferedReader br = null;
      try {
        boolean foundClassPath = false;
        ByteArrayOutputStream out = new ByteArrayOutputStream(); 
        zip = new ZipFile(file);
        ZipEntry entry = zip.getEntry("META-INF/MANIFEST.MF");
        if (entry != null) {
          br = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
          for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (line.startsWith("Class-Path")) {
              foundClassPath = true;
            } else {
              out.write(line.getBytes());
            }
          }
          
          // We need to rewrite the jar
          if (foundClassPath) {
            newManifest = out.toByteArray();
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error reading jar: " + jar, e);
      } finally {
        FileUtils.close(zip);
        FileUtils.close(br);
      }
      
      // We need to rewrite the jar
      if (newManifest != null) {
        logger.info("  Found problematic manifest... rewriting...");
        rewriteCount++;
        // rename the old jar
        File backup = new File(file.getParentFile(), file.getName() + ".bak");
        file.renameTo(backup);
        
        // Read through the jar, and keep everything except the manifest
        ZipInputStream zis = null;
        ZipOutputStream zos = null;
        try {
          zis = new ZipInputStream(new FileInputStream(backup));
          zos = new ZipOutputStream(new FileOutputStream(file));
          
          byte[] buff = new byte[1024];
          for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
            ZipEntry newEntry = new ZipEntry(entry.getName());
            zos.putNextEntry(newEntry);
            if (entry.getName().equals("META-INF/MANIFEST.MF")) {
              zos.write(newManifest);
             } else {
              int read = 0;
              while ((read = zis.read(buff)) > 0) {
                zos.write(buff, 0, read);
              }
            }
            zos.closeEntry();
          }
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Error in rewriting: " + jar, e);
        } finally {
          FileUtils.close(zis);
          FileUtils.close(zos);
        }
      }
    }
    logger.info("Done!");
    logger.info(count + " jars checked.");
    logger.info(rewriteCount + " jars rewritten.");
  }
  
  protected static JarIndex getJarIndex(AbstractRepository repo) {
    RepoFile indexFile = repo.getJarIndexFile();
    JarIndex index = new JarIndex();
    if (indexFile.exists()) {
      RepoFile projectPath = repo.getProjectJarsPath();
      RepoFile mavenPath = repo.getMavenJarsPath();
      BufferedReader br = null;
      try {
        br = new BufferedReader(new FileReader(indexFile.toFile()));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          String[] parts = line.split(" ");
          IndexedJar jar = null;
          // It has two parts if it's a project jar
          if (parts.length == 4) {
            if ("PROJECT".equals(parts[1])) {
              jar = new IndexedJar(parts[0], projectPath.getChild(parts[2]), parts[3]);
            } else {
              logger.log(Level.SEVERE, "Invalid index line: " + line);
            }
          } else if (parts.length == 7) {
            if ("MAVEN".equals(parts[1])) {
              jar = new IndexedJar(parts[0], parts[2], parts[3], parts[4], mavenPath.getChild(parts[5]), parts[6]);
            } else {
              logger.log(Level.SEVERE, "Invalid index line: " + line);
            }
          } else if (parts.length == 8) {
            if ("MAVEN".equals(parts[1])) {
              jar = new IndexedJar(parts[0], parts[2], parts[3], parts[4], mavenPath.getChild(parts[5]), parts[6], parts[7]);
            } else {
              logger.log(Level.SEVERE, "Invalid index line: " + line);
            }
          } else {
            logger.log(Level.SEVERE, "Invalid index line: " + line);
          }
          if (index.index.containsKey(parts[0])) {
            IndexedJar oldJar = index.index.get(parts[0]);
//            logger.log(Level.WARNING, oldJar.toString() + " duplicates " + jar.toString());
            if (!oldJar.isMavenJar() && jar.isMavenJar()) {
              index.index.put(parts[0], jar);
            }
          } else {
            index.index.put(parts[0], jar);
            if (!jar.isMavenJar()) {
              index.nameIndex.put(jar.getName(), jar);
            }
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error in reading jar md5 index");
        index.index.clear();
      } finally {
        FileUtils.close(br);
      }
    } else {
      logger.severe("No jar index file");
    }
    return index;
  }
  
//  public static void printJarStats(File dir) {
//    int projectJarCount = 0;
//    int mavenJarCount = 0;
//    int mavenVersionCount = 0;
//    int mavenProjectsWithSource = 0;
//    int mavenProjectsWithJar = 0;
//    
//    logger.info("Beginning jar stats calculation...");
//    Collection<File> projects = Helper.newHashSet();
//    for (File file : dir.listFiles()) {
//      // A file is a project jar
//      if (file.isFile()) {
//        if (file.getName().endsWith(".jar")) {
//          if (++projectJarCount % 1000 == 0) {
//            logger.info(projectJarCount + " project jars counted.");
//          }
//        }
//      } else if (file.isDirectory()) {
//        Deque<File> stack = Helper.newStack();
//        stack.push(file);
//        while (!stack.isEmpty()) {
//          File top = stack.pop();
//          for (File next : top.listFiles()) {
//            if (next.isDirectory()) {
//              stack.push(next);
//            } else {
//              if (next.getName().endsWith(".jar")) {
//                projects.add(top.getParentFile());
//                if (++mavenJarCount % 1000 == 0) {
//                  logger.info(mavenJarCount + " maven jars counted.");
//                }
//              }
//            }
//          }
//        }
//      }
//    }
//    
//    logger.info("Beginning individual project stats calculation...");
//    for (File project : projects) {
//      boolean foundJar = false;
//      boolean foundSource = false;
//      for (File version : project.listFiles()) {
//        if (version.isDirectory()) {
//          mavenVersionCount++;
//          for (File jar : version.listFiles()) {
//            if (jar.getName().endsWith(version.getName() + ".jar")) {
//              foundJar = true;
//            } else if (jar.getName().endsWith("sources.jar") || jar.getName().endsWith("source.jar")) {
//              foundSource = true;
//            }
//          }
//        }
//      }
//      if (foundJar) {
//        if (++mavenProjectsWithJar % 1000 == 0) {
//          logger.info(mavenProjectsWithJar + " projects counted.");
//        }
//      }
//      if (foundSource) {
//        mavenProjectsWithSource++;
//      }
//    }
//    
//    TablePrettyPrinter printer = TablePrettyPrinter.getCommandLinePrettyPrinter();
//    printer.beginTable(2);
//    printer.addDividerRow();
//    printer.addRow("Total jar count", "" + (projectJarCount + mavenJarCount));
//    printer.addRow("Project jar count", "" + projectJarCount);
//    printer.addRow("Maven jar count", "" + mavenJarCount);
//    printer.addDividerRow();
//    printer.addRow("Maven project count", "" + projects.size());
//    printer.addRow("Maven version count", "" + mavenVersionCount);
//    printer.addRow("Maven projects with jars", "" + mavenProjectsWithJar);
//    printer.addRow("Maven projects with source", "" + mavenProjectsWithSource);
//    printer.addDividerRow();
//    printer.endTable();
//  }
  
  public static void aggregateJars(Repository repo) {
    Set<String> completed = Logging.initializeResumeLogger();
    
    logger.info("--- Aggregating jar files for: " + repo.toString() + " ---");
    
    // Create the jar folder
    File repoJarFolder = repo.getProjectJarsPath().toDir();
    
    int currentOne = 0;
    int currentTwo = 0;
    
    // Create the name table
    Map<RepoJar, JarNamer> nameIndex = Helper.newHashMap();
    
    logger.info("Checking for partially completed aggregates...");
    // Check for any partially completed transfers
    for (File one : repoJarFolder.listFiles()) {
      if (one.isDirectory()) {
        try {
          currentOne = Math.max(currentOne, 1 + Integer.parseInt(one.getName()));
        } catch (NumberFormatException e) {}
        for (File two : one.listFiles()) {
          if (two.isDirectory()) {
            for (File file : two.listFiles()) {
              // A tmp file is a partially completed jar
              // An info file contains the info on a partially completed jar
              if (file.isFile() && file.getName().endsWith(".info")) {
                BufferedReader br = null;
                try {
                  br = new BufferedReader(new FileReader(file));
                  // The first line should contain the length
                  String line = br.readLine();
                  if (line == null) {
                    logger.log(Level.SEVERE, "Incorrect info file: " + file.getPath());
                    continue;
                  }
                  long length = Long.parseLong(line);
                  // The second line should contain the hash
                  String hash = br.readLine();
                  if (hash == null) {
                    logger.log(Level.SEVERE, "Incorrect info file: " + file.getPath());
                    continue;
                  }
                  RepoJar jar = new RepoJar(length, hash);
                  // The name of the jar is the name of the info file minus .info
                  String name = file.getName();
                  name = name.substring(0, name.lastIndexOf('.'));
                  JarNamer namer = new JarNamer(new File(two, name)); 
                  // The rest of the lines contain the potential names
                  for (line = br.readLine(); line != null; line = br.readLine()) {
                    namer.addName(line);
                  }
                  namer.setInfoFile(file);
                  nameIndex.put(jar, namer);
                } catch (IOException e) {
                  logger.log(Level.SEVERE, "Error reading info file", e);
                } finally {
                  FileUtils.close(br);
                }
              }
            }
          }
        }
      }
    }
    logger.info("Found " + nameIndex.size() + " partially completed aggregates.");
    logger.info("Found " + completed.size() + " completed projects.");
    
    JarIndex index = repo.getJarIndex();
    
    Collection<RepoProject> projects = repo.getProjects();
    
    logger.info("Extracting jars from " + projects.size() + " projects...");
    int projectCount = 0;
    int totalFiles = nameIndex.size();
    int uniqueFiles = nameIndex.size();
    int currentThree = 0;
    for (RepoProject project : projects) {
      projectCount++;
      if (completed.contains(project.getProjectRoot().getRelativePath())) {
        logger.info("Already completed: " + project);
      } else {
        logger.info("Getting file set for: " + project);
        try {
          IFileSet fileSet = project.getFileSet();
          if (fileSet == null) {
            continue;
          }
          logger.info("Extracting " + fileSet.getJarFileCount() + " jar files from project " + projectCount + " of " + repo.getProjects().size());
          
          for (IJarFile jar : fileSet.getJarFiles()) {
            RepoJar newJar = new RepoJar(jar.getFile());
            // If the index already contains the jar
            IndexedJar indexedJar = null;
            if (index != null ) {
              indexedJar = index.getIndexedJar(newJar.getHash());
            }
            if (indexedJar != null && !indexedJar.isMavenJar()) {
              File info = indexedJar.getInfoFile();
              FileWriter infoWriter = null;
              try {
                infoWriter = new FileWriter(info, true);
                infoWriter.write(jar + "\n");
              } catch (IOException e) {
                logger.log(Level.SEVERE, "Unable to write info file.", e);
              } finally {
                FileUtils.close(infoWriter);
              }
            } else {
              JarNamer namer = nameIndex.get(newJar);
              if (namer == null) {
                File one = new File(repoJarFolder, "" + currentOne);
                File two = new File(one, "" + currentTwo);
                File tmpFile = new File(two, currentThree + ".tmp");
                File infoFile = new File(two, currentThree + ".tmp.info");
                FileUtils.copyFile(jar.getFile().toFile(), tmpFile);
                namer = new JarNamer(tmpFile);
                nameIndex.put(newJar, namer);
                FileWriter writer = null;
                try {
                  writer = new FileWriter(infoFile);
                  writer.write(newJar.getLength() + "\n");
                  writer.write(newJar.getHash() + "\n");
                  namer.setInfoFile(infoFile);
                } finally {
                  FileUtils.close(writer);
                }
                uniqueFiles++;
                if (++currentThree == 100) {
                  currentThree = 0;
                  if (++currentTwo == 100) {
                    currentOne++;
                    currentTwo = 0;
                  }
                }
              }
              namer.addName(jar.getFile().getName());
            }
            totalFiles++;
          }
          logger.log(RESUME, project.getProjectRoot().getRelativePath());
          FileUtils.resetTempDir();
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Unable to extract project: " + project, e);
        }
      }
    }
      
    logger.info(totalFiles + " jars found");
    
    // Rename the jars
    logger.info("Renaming the " + uniqueFiles + " unique jar files");
    for (JarNamer namer : nameIndex.values()) {
      namer.rename();
    }
    
    FileUtils.cleanTempDir();
    
    logger.info("--- Done! ---");
  }
  
  public static void createJarIndexFile(Repository repo) {
    Set<String> completed = Logging.initializeResumeLogger();
    
    File indexFile = repo.getJarIndexFile().toFile();
    
    if (completed.isEmpty() && indexFile.exists()) {
      indexFile.delete();
    }
        
    FileWriter writer = null; 
    try {
      writer = new FileWriter(indexFile, true);
      
      if (repo.getMavenJarsPath().toFile().exists()) {
        logger.info("Indexing maven jars...");
        // Start by indexing the maven jars
        String mavenBaseDir = repo.getMavenJarsPath().toFile().getPath().replace('\\', '/');
        if (!mavenBaseDir.endsWith("/")) {
          mavenBaseDir += "/";
        }
        
        for (File dir : repo.getMavenJarsPath().toFile().listFiles()) {
          if (dir.isDirectory()) {
            Deque<File> stack = Helper.newStack();
            stack.push(dir);
            while (!stack.isEmpty()) {
              File top = stack.pop();
              String version = top.getName();
              File jar = null;
              File source = null;
              for (File next : top.listFiles()) {
                if (next.isDirectory()) {
                  stack.push(next);
                } else {
                  String name = next.getName();
                  if (name.endsWith("source.jar") || name.endsWith("sources.jar") || name.endsWith("sources-" + version + ".jar")) {
                    if (source != null) {
                      logger.log(Level.SEVERE, "Found two source jars! " + top.getPath());
                      if (name.length() < source.getName().length()) {
                        source = next;
                      }  
                    } else {
                      source = next;
                    }
                  } else if (name.endsWith(version + ".jar")) {
                    if (jar != null) {
                      logger.log(Level.SEVERE, "Found two jars! " + top.getPath());
                      if (name.length() < jar.getName().length()) {
                        jar = next;
                      }
                    } else {
                      jar = next;
                    }
                  }
                }
              }
              
              if (jar != null) {
                String id = getRelativePath(mavenBaseDir, jar.getPath());
                if (!completed.contains(id)) {
                  // Find out the hash
                  String hash = FileUtils.computeHash(jar);
                  
                  String groupPath = top.getParentFile().getParentFile().getPath().replace('\\', '/');
                  groupPath = getRelativePath(mavenBaseDir, groupPath);
                  String groupName = groupPath.replace('/', '.');
                        
                  String artifactName = top.getParentFile().getName();
                  
                  // Write out the entry
                  if (source == null) {
                    writer.write(hash + " MAVEN " + groupName + " " + version + " " + artifactName + " " + getRelativePath(mavenBaseDir, top.getPath()) + " " + jar.getName() + "\n");
                  } else {
                    writer.write(hash + " MAVEN " + groupName + " " + version + " " + artifactName + " " + getRelativePath(mavenBaseDir, top.getPath()) + " " + jar.getName() + " " + source.getName() + "\n");
                  }
                  writer.flush();
                        
                  // Write out the properties file
                  File propsFile = new File(top, jar.getName() + ".properties");
                  ExtractedJarProperties.create(propsFile, artifactName, groupName, version, hash);
                  
                  logger.log(RESUME, id);
                }
              }
            }
          }
        }
      }
        
      logger.info("Indexing project jars...");
      // Now index the project jars
      String projectBaseDir = repo.getProjectJarsPath().toFile().getPath().replace('\\', '/');
      if (!projectBaseDir.endsWith("/")) {
        projectBaseDir += "/";
      }
      
      for (File one : repo.getProjectJarsPath().toFile().listFiles()) {
        if (one.isDirectory()) {
          for (File two : one.listFiles()) {
            if (two.isDirectory()) {
              for (File file : two.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                  if (!completed.contains(file.getName())) {
                    // Find the hash
                    String hash = FileUtils.computeHash(file);
                    
                    // Write out the entry
                    writer.write(hash + " PROJECT " + one.getName() + "/" + two.getName() + " " + file.getName() + "\n");
                    writer.flush();
                    
                    // Write out the properties file
                    String name = file.getName();
                    name = name.substring(0, name.lastIndexOf('.'));
                    
                    File propsFile = new File(two, file.getName() + ".properties");
                    ExtractedJarProperties.create(propsFile, name, hash);
                    
                    logger.log(RESUME, file.getName());
                  }
                }
              }
            }
          }
        }
      }
      writer.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write out jar index", e);
    } finally {
      FileUtils.close(writer);
    }
    
    logger.info("Done!");
  }
  
  private static String getRelativePath(String basePath, String path) {
    path = path.replace('\\', '/');
    if (path.startsWith(basePath)) {
      return path.substring(basePath.length());
    } else {
      logger.log(Level.SEVERE, "Unable to convert to relative path: " + path);
      return path;
    }
  }
  
  public int getIndexSize() {
    return index.size();
  }
  
  public IndexedJar getPossibleSourceMatch(IndexedJar jar) {
    String name = jar.getName();
    name = name.replaceFirst("_", ".source_");
    if (name.equals(jar.getName())) {
      return null;
    } else {
      return nameIndex.get(name);
    }
  }
  
  public Collection<IndexedJar> getJars() {
    return getJars(MavenFilter.ALL, ProjectFilter.ALL, null);
  }
  
  public Collection<IndexedJar> getJars(MavenFilter maven, ProjectFilter project, Set<String> filter) {
    if (maven == MavenFilter.ALL && project == ProjectFilter.ALL) {
      return index.values();
    } else {
      Collection<IndexedJar> jars = Helper.newArrayList();
      Map<String, IndexedJar> byProject = null;
      if (maven == MavenFilter.LATEST) {
        byProject = Helper.newHashMap();
      }
      for (IndexedJar jar : index.values()) {
        if (jar.isMavenJar()) {
          if (maven == MavenFilter.ALL) {
            jars.add(jar);
          } else if (maven == MavenFilter.LATEST) {
            String key = jar.getGroupName() + jar.getArtifactName();
            IndexedJar other = byProject.get(key);
            if (other == null) {
              byProject.put(key, jar);
            } else {
              if (newestVersion(jar.getVersion(), other.getVersion())) {
                byProject.put(key, jar);
              }
            }
          } else if (maven == MavenFilter.MANUAL) {
            if (filter.contains(jar.getHash())) {
              jars.add(jar);
            }
          }
        } else {
          if (project == ProjectFilter.ALL) {
            jars.add(jar);
          } else if (project == ProjectFilter.MANUAL) {
            if (filter.contains(jar.getHash())) {
              jars.add(jar);
            }
          }
        }
      }
      if (maven == MavenFilter.LATEST) {
        for (IndexedJar jar : byProject.values()) {
          jars.add(jar);
        }
      }
      return jars;
    }
  }
  
//  private static Pattern leadingDigits = Pattern.compile("(\\d+)(.*)");
  private static boolean newestVersion(String first, String second) {
    String[] firstParts = first.split("\\.");
    String[] secondParts = second.split("\\.");
    for (int i = 0; i < firstParts.length; i++) {
      if (i < secondParts.length) {
        try {
          int firstNum = Integer.parseInt(firstParts[i]);
          int secondNum = Integer.parseInt(secondParts[i]);
          if (firstNum > secondNum) {
            return true;
          } else if (firstNum < secondNum) {
            return false;
          }
        } catch (NumberFormatException e) {
          int comp = firstParts[i].compareTo(secondParts[i]);
          if (comp > 0) {
            return true;
          } else if (comp < 0) {
            return false;
          }
        }
      }
    }
    return false;
  }
  
  public IndexedJar getIndexedJar(String hash) {
    return index.get(hash);
  }
}
