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
package edu.uci.ics.sourcerer.repo.extracted;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.general.AbstractExtractedProperties;
import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
import edu.uci.ics.sourcerer.repo.general.IndexedJar;
import edu.uci.ics.sourcerer.repo.general.JarIndex;
import edu.uci.ics.sourcerer.repo.general.RepoPath;
import edu.uci.ics.sourcerer.repo.general.JarIndex.MavenFilter;
import edu.uci.ics.sourcerer.repo.general.JarIndex.ProjectFilter;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedRepository extends AbstractRepository {
  public static final Property<String> EXTRACTION_STATS_FILE = new StringProperty("extraction-stats-file", "extraction-stats.txt", "Output file for the extraction stats.");
  
  private boolean includeNotExtracted = false;
  private Collection<ExtractedLibrary> libraries;
  private Collection<ExtractedJar> jars;
  private Collection<ExtractedProject> projects;
  
  private ExtractedRepository(File repoRoot) {
    super(repoRoot);
  }
  
  @Override
  protected void addProject(RepoPath path) {
    ExtractedProject extracted = new ExtractedProject(path);
    if (includeNotExtracted || extracted.extracted()) {
      projects.add(extracted);
    } else {
      extracted = new ExtractedProject(path.getChild("content"));
      if (includeNotExtracted || extracted.extracted()) {
        projects.add(extracted);
      }
    }
  }
  
  private void populateLibraries() {
    libraries = Helper.newLinkedList();
    if (libsRoot.exists()) {
      for (File lib : libsRoot.toFile().listFiles()) {
        if (lib.isDirectory()) {
          ExtractedLibrary extracted = new ExtractedLibrary(libsRoot.getChild(lib.getName()));
          if (includeNotExtracted || extracted.extracted()) {
            libraries.add(extracted);
          }
        }
      }
    }
  }

  private void populateJars(final Set<String> filter) {
    final JarIndex index = getJarIndex();
    if (index == null) {
      jars = Collections.emptyList();
    } else {
      jars = new AbstractCollection<ExtractedJar>() {
        @Override
        public int size() {
          return index.getIndexSize();
        }
        
        @Override
        public Iterator<ExtractedJar> iterator() {
          return new Iterator<ExtractedJar>() {
            private Iterator<IndexedJar> iter = (filter == null ? index.getJars() : index.getJars(MavenFilter.ALL, ProjectFilter.ALL, filter)).iterator();
            
            @Override
            public void remove() {
              throw new UnsupportedOperationException();          
            }
            
            @Override
            public ExtractedJar next() {
              return iter.next().getExtractedJar();
            }
            
            @Override
            public boolean hasNext() {
              return iter.hasNext();
            }
          };
        }
      };
    }    
    // This is too slow, use the jar index
//    File jarsDir = getJarsDir();
//    if (jarsDir.exists()) {
//      Deque<File> stack = Helper.newStack();
//      stack.push(jarsDir);
//      while (!stack.isEmpty()) {
//        File top = stack.pop();
//        for (File dir : top.listFiles()) {
//          if (dir.isDirectory()) {
//            stack.push(dir);
//          } else if (dir.isFile() && dir.getName().endsWith(".properties")) {
//            ExtractedJar extracted = new ExtractedJar(top);
//            if (extracted.getPropertiesFile().exists()) {
//              jars.add(extracted);
//            }
//          }
//        }
//      }
//    }
  }
 
  public static ExtractedRepository getRepository() {
    return getRepository(INPUT_REPO.getValue());
  }
  
  public static ExtractedRepository getRepository(File repoRoot) {
    return new ExtractedRepository(repoRoot);
  }
  
  public Collection<ExtractedLibrary> getLibraries() {
    if (libraries == null) {
      populateLibraries();
    }
    return libraries;
  }
  
  public Collection<ExtractedJar> getJars() {
    populateJars(null);
    return jars;
  }
  
  public Collection<ExtractedJar> getJars(Set<String> filter) {
    populateJars(filter);
    return jars;
  }
  
  public Collection<ExtractedProject> getProjects() {
    if (projects == null) {
      projects = Helper.newLinkedList();
      populateRepository();
    }
    return projects;
  }
  
  public Collection<ExtractedProject> getProjects(Set<String> filter) {
    if (projects == null) {
      projects = Helper.newLinkedList();
      populateRepository();
    }
    if (filter == null) {
      return projects;
    } else {
      Collection<ExtractedProject> result = Helper.newArrayList();
      for (ExtractedProject project : projects) {
        if (filter.contains(project.getRelativePath())) {
          result.add(project);
        }
      }
      return result;
    }
  }
  
  public File getJavaLibrary(String path) {
    return new File(repoRoot, path + "/lib.jar");
  }
    
  public File getJavaLibrarySource(String path) {
    return new File(repoRoot, path + "/source.jar"); 
  }
  
  public void cloneProperties(ExtractedRepository target) {
    logger.info("Cloning extracted library properties...");
    {
      int count = 0;
      for (ExtractedLibrary lib : getLibraries()) {
        lib.clonePropertiesFile(target);
        count++;
      }
      logger.info("  " + count + " libraries cloned.");
    }
    
    logger.info("Cloning extracted jar properties...");
    {
      int count = 0;
      for (ExtractedJar jar : getJars()) {
        jar.clonePropertiesFile(target);
        count++;
      }
      logger.info("  " + count + " jars cloned.");
    }
    
    logger.info("Cloning extracted project properties...");
    {
      int count = 0;
      for (ExtractedProject project : getProjects()) {
        project.clonePropertiesFile(target);
        count++;
      }
      logger.info("  " + count + " projects cloned.");
    }
  }
  
  public void printProjectNames() {
    logger.info("Loading projects...");
    
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(PROJECT_NAMES_FILE);
    printer.beginTable(3);
    printer.addDividerRow();
    printer.addRow("host", "project", "crawled date");
    printer.addDividerRow();
    for (ExtractedProject project : getProjects()) {
      AbstractExtractedProperties props = project.getProperties();
      printer.beginRow();
      printer.addCell(props.getOriginRepo());
      printer.addCell(props.getName());
      printer.addCell(props.getCrawledDate());
    }
    printer.endTable();
    printer.close();
    logger.info("Done!");
  }
  
  public void computeExtractionStats() {
    includeNotExtracted = true;
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(EXTRACTION_STATS_FILE);
    if (libraries == null) {
      logger.info("Loading libraries...");
      populateLibraries();
    }
    logger.info("Computing stats for " + libraries.size() + " libraries.");
    {
      int libsExtracted = 0;
      int libsNonEmpty = 0;
      int libsWithMissingTypes = 0;
      int extractedLibsWithMissingTypes = 0;
      int totalBinaryExtracted = 0;
      int libsWithBinaryExceptions = 0;
      int totalBinaryExceptions = 0;
      int libsWithSource = 0;
      int totalSourceExtracted = 0;
      int libsWithSourceExceptions = 0;
      int totalSourceExceptions = 0;
      
      for (ExtractedLibrary lib : libraries) {
        if (lib.extracted()) {
          libsExtracted++;
          if (!lib.empty()) {
            libsNonEmpty++;
          
            totalBinaryExtracted += lib.getExtractedFromBinary();
            if (lib.hasBinaryExceptions()) {
              libsWithBinaryExceptions++;
              totalBinaryExceptions += lib.getBinaryExceptions();
            }
            
            if (lib.hasSource()) {
              libsWithSource++;
              totalSourceExtracted += lib.getExtractedFromSource();
              if (lib.hasSourceExceptions()) {
                libsWithSourceExceptions++;
                totalSourceExceptions += lib.getSourceExceptions();
              }
            }
          }
          if (lib.hasMissingTypes()) {
            extractedLibsWithMissingTypes++;
          }
        } else if (lib.hasMissingTypes()) {
          libsWithMissingTypes++;
        }
      }
      
      printer.addHeader("Extracted Library Statistics");
      printer.beginTable(2);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Extracted libraries");
      printer.addCell(libsExtracted);
      printer.beginRow();
      printer.addCell("Non-empty libraries");
      printer.addCell(libsNonEmpty);
      printer.beginRow();
      printer.addCell("Extracted libraries with missing types");
      printer.addCell(libsExtracted);
      printer.beginRow();
      printer.addCell("Non-extracted libraries with missing types");
      printer.addCell(libsExtracted);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Binary files extracted");
      printer.addCell(totalBinaryExtracted);
      printer.beginRow();
      printer.addCell("Libs with binary file exceptions");
      printer.addCell(libsWithBinaryExceptions);
      printer.beginRow();
      printer.addCell("Binary files with exceptions");
      printer.addCell(totalBinaryExceptions);
      printer.beginRow();
      printer.addCell("Binary files extracted");
      printer.addCell(totalBinaryExtracted);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Libs with source files");
      printer.addCell(libsWithSource);
      printer.beginRow();
      printer.addCell("Source files extracted");
      printer.addCell(totalSourceExtracted);
      printer.beginRow();
      printer.addCell("Libs with source file exceptions");
      printer.addCell(libsWithSourceExceptions);
      printer.beginRow();
      printer.addCell("Source files with exceptions");
      printer.addCell(totalSourceExceptions);
      printer.addDividerRow();
      printer.endTable();
    }
    
    if (jars == null) {
      logger.info("Loading jars...");
      populateJars(null);
    }
    
    logger.info("Computing stats for " + jars.size() + " jars.");
    {
      int extracted = 0;
      int nonEmpty = 0;
      int withMissingTypes = 0;
      int jarsWithMissingTypes = 0;
      int binaryExtracted = 0;
      int jarsWithBinaryExceptions = 0;
      int binaryExceptions = 0;
      int sourceSkipped = 0;
      int withSource = 0;
      int sourceExtracted = 0;
      int jarsWithSourceExceptions = 0;
      int sourceExceptions = 0;
      int usingJars = 0;
      int usedJars = 0;
      int firstOrderJars = 0;
      
      for (ExtractedJar jar : jars) {
        if (jar.extracted()) {
          extracted++;
          if (!jar.empty()) {
            nonEmpty++;
          
            binaryExtracted += jar.getExtractedFromBinary();
            if (jar.hasBinaryExceptions()) {
              jarsWithBinaryExceptions++;
              binaryExceptions += jar.getBinaryExceptions();
            }
            
            if (jar.sourceSkipped()) {
              sourceSkipped++;
            }
            
            if (jar.hasSource()) {
              withSource++;
              sourceExtracted += jar.getExtractedFromSource();
              if (jar.hasSourceExceptions()) {
                jarsWithSourceExceptions++;
                sourceExceptions += jar.getSourceExceptions();
              }
            }
            
//            if (jar.getFirstOrderJars() == 0 && jar.getJars() > 0) {
//              logger.info(jar.getName() + " " + jar.getFirstOrderJars() + " " + jar.getJars());
//            } else if (jar.getFirstOrderJars() < 0 || jar.getJars() < 0) {
//              logger.info(jar.getName() + " " + jar.getFirstOrderJars() + " " + jar.getJars());
//            }
            if (jar.getFirstOrderJars() > 0) {
              firstOrderJars += jar.getFirstOrderJars();
              int usedCount = jar.getJars();
              if (usedCount > 0) {
                usingJars++;
                usedJars += usedCount;
              }
            }
          }
          if (jar.hasMissingTypes()) {
            jarsWithMissingTypes++;
          }
        } else if (jar.hasMissingTypes()) {
          withMissingTypes++;
        }
      }
      
      printer.addHeader("Extracted Jar Statistics");
      printer.beginTable(2);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Extracted jars");
      printer.addCell(extracted);
      printer.beginRow();
      printer.addCell("Non-empty jars");
      printer.addCell(nonEmpty);
      printer.beginRow();
      printer.addCell("Extracted jars with missing types");
      printer.addCell(jarsWithMissingTypes);
      printer.beginRow();
      printer.addCell("Non-extracted jars with missing types");
      printer.addCell(withMissingTypes);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Binary files extracted");
      printer.addCell(binaryExtracted);
      printer.beginRow();
      printer.addCell("Jars with binary file exceptions");
      printer.addCell(jarsWithBinaryExceptions);
      printer.beginRow();
      printer.addCell("Binary files with exceptions");
      printer.addCell(binaryExceptions);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Jars with source skipped");
      printer.addCell(sourceSkipped);
      printer.beginRow();
      printer.addCell("Jars with source files");
      printer.addCell(withSource);
      printer.beginRow();
      printer.addCell("Source files extracted");
      printer.addCell(sourceExtracted);
      printer.beginRow();
      printer.addCell("Jars with source file exceptions");
      printer.addCell(jarsWithSourceExceptions);
      printer.beginRow();
      printer.addCell("Source files with exceptions");
      printer.addCell(sourceExceptions);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Jars using other jars");
      printer.addCell(usingJars);
      printer.beginRow();
      printer.addCell("Jars used by other jars");
      printer.addCell(usedJars);
      printer.beginRow();
      printer.addCell("First order jars uses");
      printer.addCell(firstOrderJars);
      printer.addDividerRow();
      printer.endTable();
    }
    
    getProjects();
    
    logger.info("Computing stats for " + projects.size() + " projects.");
    {
      int extracted = 0;
      int nonEmpty = 0;
      int withMissingTypes = 0;
      int projectsWithMissingTypes = 0;
      int projectsWithSourceExceptions = 0;
      int sourceExceptions = 0;
      int correctUsingJars = 0;
      int missingUsingJars = 0;
      Averager<Integer> correctSource = new Averager<Integer>();
      Averager<Integer> missingSource = new Averager<Integer>();
      Averager<Integer> correctJars = new Averager<Integer>();
      Averager<Integer> missingJars = new Averager<Integer>();
      
      for (ExtractedProject project : projects) {
        if (project.extracted()) {
          extracted++;
          if (!project.empty()) {
            nonEmpty++;

            if (project.hasSourceExceptions()) {
              projectsWithSourceExceptions++;
              sourceExceptions += project.getSourceExceptions();
            }
            
            if (project.hasMissingTypes()) {
              projectsWithMissingTypes++;
              missingSource.addValue(project.getExtractedFromSource());
              int usedCount = project.getJars();
              if (usedCount > 0) {
                missingUsingJars++;
                missingJars.addValue(usedCount);
              }
              logger.log(Level.SEVERE, "MISSING - " + project);
            } else {
              correctSource.addValue(project.getExtractedFromSource());
            
              int usedCount = project.getJars();
              if (usedCount > 0) {
                correctUsingJars++;
                correctJars.addValue(usedCount);
              }
            }
          } else {
            if (project.hasMissingTypes()) {
              logger.info("MISSING + EMPTY: " + project);
            }
          }
        } else if (project.hasMissingTypes()) {
          withMissingTypes++;
        } else {
          logger.info(project.toString());
        }
      }
      
      printer.addHeader("Extracted Project Statistics");
      printer.beginTable(6);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Extracted projects");
      printer.addCell(extracted);
      printer.beginRow();
      printer.addCell("Non-empty projects");
      printer.addCell(nonEmpty);
      printer.beginRow();
      printer.addCell("Extracted projects with missing types");
      printer.addCell(projectsWithMissingTypes);
      printer.beginRow();
      printer.addCell("Non-extracted projects with missing types");
      printer.addCell(withMissingTypes);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Correct Projects", 6, TablePrettyPrinter.Alignment.CENTER);
      printer.addRow("", "Sum", "Mean", "Dev", "Min", "Max");
      printer.addRow("Source files extracted", correctSource.getSum(), correctSource.getMean(), correctSource.getStandardDeviation(), correctSource.getMin(), correctSource.getMax());
      printer.addRow("Using jars", "" + correctUsingJars);
      printer.addRow("Used jars", correctJars.getSum(), correctJars.getMean(), correctJars.getStandardDeviation(), correctJars.getMin(), correctJars.getMax());
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Missing Type Projects", 6, TablePrettyPrinter.Alignment.CENTER);
      printer.addRow("", "Sum", "Mean", "Dev", "Min", "Max");
      printer.addRow("Source files extracted", missingSource.getSum(), missingSource.getMean(), missingSource.getStandardDeviation(), missingSource.getMin(), missingSource.getMax());
      printer.addRow("Using jars", "" + missingUsingJars);
      printer.addRow("Used jars", missingJars.getSum(), missingJars.getMean(), missingJars.getStandardDeviation(), missingJars.getMin(), missingJars.getMax());
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Projects with source file exceptions");
      printer.addCell(projectsWithSourceExceptions);
      printer.beginRow();
      printer.addCell("Source files with exceptions");
      printer.addCell(sourceExceptions);
      printer.addDividerRow();
      printer.endTable();
    }
    printer.close();
  }
}
