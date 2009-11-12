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
import java.util.Collection;

import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ExtractedRepository extends AbstractRepository {
  public static final Property<String> EXTRACTION_STATS_FILE = new StringProperty("extraction-stats-file", "extraction-stats.txt", "Repository Manager", "Output file for the extraction stats.");
  
  private Collection<ExtractedLibrary> libraries;
  private Collection<ExtractedJar> jars;
  private Collection<ExtractedProject> projects;
  
  private ExtractedRepository(File repoRoot) {
    super(repoRoot);
  }
  
  @Override
  protected void addFile(File checkout) {
    ExtractedProject extracted = new ExtractedProject(checkout, checkout.getParentFile().getName() + "/" + checkout.getName());
    if (extracted.extracted()) {
      projects.add(extracted);
    }
  }
  
  private void populateLibraries() {
    libraries = Helper.newLinkedList();
    File libsDir = getLibsDir();
    if (libsDir.exists()) {
      for (File lib : libsDir.listFiles()) {
        if (lib.isDirectory()) {
          ExtractedLibrary extracted = new ExtractedLibrary(lib);
          if (extracted.extracted()) {
            libraries.add(extracted);
          }
        }
      }
    }
  }

  private void populateJars() {
    jars = Helper.newLinkedList();
    File jarsDir = getJarsDir();
    if (jarsDir.exists()) {
      for (File jar : jarsDir.listFiles()) {
        if (jar.isDirectory()) {
          ExtractedJar extracted = new ExtractedJar(jar);
          if (extracted.extracted()) {
            jars.add(extracted);
          }
        }
      }
    }
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
    if (jars == null) {
      populateJars();
    }
    return jars;
  }
  
  public Collection<ExtractedProject> getProjects() {
    if (projects == null) {
      projects = Helper.newLinkedList();
      populateRepository();
    }
    return projects;
  }
  
  public void computeExtractionStats() {
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
      populateJars();
    }
    
    logger.info("Computing stats for " + jars.size() + " jars.");
    {
      int jarsExtracted = 0;
      int jarsNonEmpty = 0;
      int jarsWithMissingTypes = 0;
      int extractedJarsWithMissingTypes = 0;
      int totalBinaryExtracted = 0;
      int jarsWithBinaryExceptions = 0;
      int totalBinaryExceptions = 0;
      int jarsWithSource = 0;
      int totalSourceExtracted = 0;
      int jarsWithSourceExceptions = 0;
      int totalSourceExceptions = 0;
      
      for (ExtractedJar jar : jars) {
        if (jar.extracted()) {
          jarsExtracted++;
          if (!jar.empty()) {
            jarsNonEmpty++;
          
            totalBinaryExtracted += jar.getExtractedFromBinary();
            if (jar.hasBinaryExceptions()) {
              jarsWithBinaryExceptions++;
              totalBinaryExceptions += jar.getBinaryExceptions();
            }
            
            if (jar.hasSource()) {
              jarsWithSource++;
              totalSourceExtracted += jar.getExtractedFromSource();
              if (jar.hasSourceExceptions()) {
                jarsWithSourceExceptions++;
                totalSourceExceptions += jar.getSourceExceptions();
              }
            }
          }
          if (jar.hasMissingTypes()) {
            extractedJarsWithMissingTypes++;
          }
        } else if (jar.hasMissingTypes()) {
          jarsWithMissingTypes++;
        }
      }
      
      printer.addHeader("Extracted Jar Statistics");
      printer.beginTable(2);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Extracted jars");
      printer.addCell(jarsExtracted);
      printer.beginRow();
      printer.addCell("Non-empty jars");
      printer.addCell(jarsNonEmpty);
      printer.beginRow();
      printer.addCell("Extracted jars with missing types");
      printer.addCell(jarsExtracted);
      printer.beginRow();
      printer.addCell("Non-extracted jars with missing types");
      printer.addCell(jarsExtracted);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Binary files extracted");
      printer.addCell(totalBinaryExtracted);
      printer.beginRow();
      printer.addCell("Jars with binary file exceptions");
      printer.addCell(jarsWithBinaryExceptions);
      printer.beginRow();
      printer.addCell("Binary files with exceptions");
      printer.addCell(totalBinaryExceptions);
      printer.beginRow();
      printer.addCell("Binary files extracted");
      printer.addCell(totalBinaryExtracted);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Jars with source files");
      printer.addCell(jarsWithSource);
      printer.beginRow();
      printer.addCell("Jars with source file exceptions");
      printer.addCell(jarsWithSourceExceptions);
      printer.beginRow();
      printer.addCell("Source files with exceptions");
      printer.addCell(totalSourceExceptions);
      printer.addDividerRow();
      printer.endTable();
    }
    
    printer.close();
  }
}
