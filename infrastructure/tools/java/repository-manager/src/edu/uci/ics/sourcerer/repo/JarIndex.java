// Sourcerer: an infrastructure for large-scale source code analysis.
// Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
package edu.uci.ics.sourcerer.repo;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Deque;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarIndex {
  private Map<String, IndexedJar> index;
  
  private JarIndex() {
    index = Helper.newHashMap();
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
          index.index.put(parts[0], new IndexedJar(basePath, parts[1]));
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error in reading jar md5 index");
        index.index.clear();
      } finally {
        try {
          if (br != null) {
            br.close();
          }
        } catch(IOException e) {}
      }
    } else {
      logger.severe("No jar index file");
    }
    return index;
  }
  
  public static void createJarIndexFile(File dir) {
    PropertyManager properties = PropertyManager.getProperties();
    File indexFile = new File(dir, properties.getValue(Property.JAR_INDEX_FILE));
    
    String baseDir = dir.getPath().replace('\\', '/');
    
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile));
      for (File file : dir.listFiles()) {
        // A directory indicates a maven jar
        if (file.isDirectory()) {
          Deque<File> stack = Helper.newStack();
          stack.push(file);
          while (!stack.isEmpty()) {
            File top = stack.pop();
            for (File next : top.listFiles()) {
              if (next.isDirectory()) {
                stack.push(next);
              } else {
                if (next.getName().endsWith(".jar")) {
                  String version = top.getName();
                  if (next.getName().endsWith(version + ".jar")) {
                    // Find out the hash
                    String hash = RepoJar.getHash(next);
                    
                    // Write out the entry
                    bw.write(hash + " " + getRelativePath(baseDir, next.getPath()));
                    
                    // Write out the properties file
                    String name = next.getName();
                    name = name.substring(0, name.lastIndexOf('.'));
                    
                    String groupPath = top.getParentFile().getParentFile().getPath().replace('\\', '/');
                    groupPath = getRelativePath(baseDir, groupPath);
                    String groupName = groupPath.replace('/', '.');
                    
                    String artifactName = top.getParentFile().getName();
                    
                    Properties props = new Properties();
                    props.setProperty("name", artifactName);
                    props.setProperty("group", groupName);
                    props.setProperty("version", version);
                    props.setProperty("hash", hash);
                    
                    File propsFile = new File(dir, name +".properties");
                    OutputStream os = new FileOutputStream(propsFile);
                    props.store(os, null);
                    os.close();
                  }
                }
              }
            }
          }
        } else if (file.isFile() && file.getName().endsWith(".jar")) {
          // Find out the hash
          String hash = RepoJar.getHash(file);
          
          // Write out the entry
          bw.write(hash + " " + file.getName());
          
          // Write out the properties file
          String name = file.getName();
          name = name.substring(0, name.lastIndexOf('.'));
          
          Properties props = new Properties();
          props.setProperty("name", name);
          props.setProperty("hash", hash);
          
          File propsFile = new File(dir, name + ".properties");
          OutputStream os = new FileOutputStream(propsFile);
          props.store(os, null);
          os.close();
        }
      }
      bw.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write out jar index", e);
    }
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
