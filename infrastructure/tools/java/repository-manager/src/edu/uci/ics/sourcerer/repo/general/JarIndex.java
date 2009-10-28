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

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarIndex {
  public static final Property<String> JAR_INDEX_FILE = new StringProperty("jar-index", "index.txt", "Repository Manager", "The filename of the jar index.");
  
  private Map<String, IndexedJar> index;
  
  private JarIndex() {
    index = Helper.newHashMap();
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
  
  protected static JarIndex getJarIndex(File indexFile) {
    JarIndex index = new JarIndex();
    if (indexFile.exists()) {
      String basePath = indexFile.getParentFile().getPath() + File.separatorChar;
      BufferedReader br = null;
      try {
        br = new BufferedReader(new FileReader(indexFile));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          String[] parts = line.split(" ");
          IndexedJar jar = null;
          // It has two parts if it's a project jar
          if (parts.length == 2) {
            jar = new IndexedJar(basePath, parts[1]);
          } else if (parts.length == 3) {
            jar = new IndexedJar(basePath, parts[1], parts[2]);
          } else if (parts.length == 4) {
            jar = new IndexedJar(basePath, parts[1], parts[2], parts[3]);
          }
          if (index.index.containsKey(parts[0])) {
            IndexedJar oldJar = index.index.get(parts[0]);
//            logger.log(Level.WARNING, oldJar.toString() + " duplicates " + jar.toString());
            if (!oldJar.isMavenJar() && jar.isMavenJar()) {
              index.index.put(parts[0], jar);
            }
          } else {
            index.index.put(parts[0], jar);
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
  
  public static void printJarStats(File dir) {
    int projectJarCount = 0;
    int mavenJarCount = 0;
    int mavenVersionCount = 0;
    int mavenProjectsWithSource = 0;
    int mavenProjectsWithJar = 0;
    
    logger.info("Beginning jar stats calculation...");
    Collection<File> projects = Helper.newHashSet();
    for (File file : dir.listFiles()) {
      // A file is a project jar
      if (file.isFile()) {
        if (file.getName().endsWith(".jar")) {
          if (++projectJarCount % 1000 == 0) {
            logger.info(projectJarCount + " project jars counted.");
          }
        }
      } else if (file.isDirectory()) {
        Deque<File> stack = Helper.newStack();
        stack.push(file);
        while (!stack.isEmpty()) {
          File top = stack.pop();
          for (File next : top.listFiles()) {
            if (next.isDirectory()) {
              stack.push(next);
            } else {
              if (next.getName().endsWith(".jar")) {
                projects.add(top.getParentFile());
                if (++mavenJarCount % 1000 == 0) {
                  logger.info(mavenJarCount + " maven jars counted.");
                }
              }
            }
          }
        }
      }
    }
    
    logger.info("Beginning individual project stats calculation...");
    for (File project : projects) {
      boolean foundJar = false;
      boolean foundSource = false;
      for (File version : project.listFiles()) {
        if (version.isDirectory()) {
          mavenVersionCount++;
          for (File jar : version.listFiles()) {
            if (jar.getName().endsWith(version.getName() + ".jar")) {
              foundJar = true;
            } else if (jar.getName().endsWith("sources.jar") || jar.getName().endsWith("source.jar")) {
              foundSource = true;
            }
          }
        }
      }
      if (foundJar) {
        if (++mavenProjectsWithJar % 1000 == 0) {
          logger.info(mavenProjectsWithJar + " projects counted.");
        }
      }
      if (foundSource) {
        mavenProjectsWithSource++;
      }
    }
    
    TablePrettyPrinter printer = TablePrettyPrinter.getCommandLinePrettyPrinter();
    printer.beginTable(2);
    printer.addDividerRow();
    printer.addRow("Total jar count", "" + (projectJarCount + mavenJarCount));
    printer.addRow("Project jar count", "" + projectJarCount);
    printer.addRow("Maven jar count", "" + mavenJarCount);
    printer.addDividerRow();
    printer.addRow("Maven project count", "" + projects.size());
    printer.addRow("Maven version count", "" + mavenVersionCount);
    printer.addRow("Maven projects with jars", "" + mavenProjectsWithJar);
    printer.addRow("Maven projects with source", "" + mavenProjectsWithSource);
    printer.addDividerRow();
    printer.endTable();
  }
  
  public static void createJarIndexFile(File dir) {
    Set<String> completed = Logging.initializeResumeLogger();
    
    File indexFile = new File(dir, JAR_INDEX_FILE.getValue());
    
    String baseDir = dir.getPath().replace('\\', '/');
    if (!baseDir.endsWith("/")) {
      baseDir += "/";
    }
    
    if (completed.isEmpty() && indexFile.exists()) {
      indexFile.delete();
    }
    
    int projectJarCount = 0;
    int mavenJarCount = 0;
    int mavenSourceCount = 0;
    FileWriter writer = null; 
    try {
      writer = new FileWriter(indexFile, true);
      for (File file : dir.listFiles()) {
        // A directory indicates a maven jar
        if (file.isDirectory()) {
          Deque<File> stack = Helper.newStack();
          stack.push(file);
          while (!stack.isEmpty()) {
            File top = stack.pop();
            String version = top.getName();
            File jar = null;
            File source = null;
            for (File next : top.listFiles()) {
              if (next.isDirectory()) {
                stack.push(next);
              } else {
                if (next.getName().endsWith(version + ".jar")) {
                  if (jar != null) {
                    logger.log(Level.SEVERE, "Found two jars! " + top.getPath());
                  }
                  jar = next;
                } else if (next.getName().endsWith("source.jar") || next.getName().endsWith("sources.jar")) {
                  if (source != null) {
                    logger.log(Level.SEVERE, "Found two source jars! " + top.getPath());
                  }
                  source = next;
                }
              }
            }
            
            if (jar != null) {
              String id = getRelativePath(baseDir, jar.getPath());
              if (!completed.contains(id)) {
                // Find out the hash
                String hash = RepoJar.getHash(jar);
                      
                // Write out the entry
                if (source == null) {
                  writer.write(hash + " " + getRelativePath(baseDir, top.getPath()) + " " + jar.getName() + "\n");
                } else {
                  writer.write(hash + " " + getRelativePath(baseDir, top.getPath()) + " " + jar.getName() + " " + source.getName() + "\n");
                }
                writer.flush();
                      
                // Write out the properties file
                String name = jar.getName();
                name = name.substring(0, name.lastIndexOf('.'));
                      
                String groupPath = top.getParentFile().getParentFile().getPath().replace('\\', '/');
                groupPath = getRelativePath(baseDir, groupPath);
                String groupName = groupPath.replace('/', '.');
                      
                String artifactName = top.getParentFile().getName();
                      
                File propsFile = new File(top, jar.getName() + ".properties");
                JarProperties.create(propsFile, artifactName, groupName, version, hash);
                
                logger.log(RESUME, id);
              }
              if (++mavenJarCount % 1000 == 0) {
                logger.info(mavenJarCount + " maven jars indexed");
              }
              if (source != null && ++mavenSourceCount % 1000 == 0) {
                logger.info(mavenSourceCount + " maven source jars indexed");
              }
            }
          }
        } else if (file.isFile() && file.getName().endsWith(".jar")) {
          if (!completed.contains(file.getName())) {
            // Find out the hash
            String hash = RepoJar.getHash(file);
          
            // Write out the entry
            writer.write(hash + " " + file.getName() + "\n");
            writer.flush();
          
            // Write out the properties file
            String name = file.getName();
            name = name.substring(0, name.lastIndexOf('.'));
          
            File propsFile = new File(dir, file.getName() + ".properties");
            JarProperties.create(propsFile, name, hash);
            
            logger.log(RESUME, file.getName());
          }
          if (++projectJarCount % 1000 == 0) {
            logger.info(projectJarCount + " project jars indexed");
          }
        }
      }
      writer.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write out jar index", e);
    } finally {
      FileUtils.close(writer);
    }
    
    logger.info(mavenJarCount + " maven jars indexed");
    logger.info(mavenJarCount + " maven source jars indexed");
    logger.info(projectJarCount + " project jars indexed");
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
  
  public Iterable<IndexedJar> getIndexedJars() {
    return index.values();
  }
  
  public IndexedJar getIndexedJar(String hash) {
    return index.get(hash);
  }
}
