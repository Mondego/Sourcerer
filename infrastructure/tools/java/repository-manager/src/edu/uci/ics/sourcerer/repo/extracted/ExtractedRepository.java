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
      int extracted = 0;
      int nonEmpty = 0;
      int source = 0;
      int sourceError = 0;
      int totalBinaryExtracted = 0;
      int totalSourceExtracted = 0;
      int totalSourceWithError = 0;
      for (ExtractedLibrary lib : libraries) {
        if (lib.extracted()) {
          extracted++;
          if (lib.getExtractedFromBinaryCount() > 0) {
            nonEmpty++;
          }
          totalBinaryExtracted += lib.getExtractedFromBinaryCount();
          if (lib.hasSource()) {
            source++;
            totalSourceExtracted += lib.getExtractedFromSource();
          }
          if (lib.sourceError()) {
            sourceError++;
            totalSourceWithError += lib.getSourceFilesWithErrors();
          }
        }
      }
      
      printer.addHeader("Extracted Library Statistics");
      printer.beginTable(2);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Extracted libraries");
      printer.addCell(extracted);
      printer.beginRow();
      printer.addCell("Non-empty libraries");
      printer.addCell(nonEmpty);
      printer.beginRow();
      printer.addCell("Binary files extracted");
      printer.addCell(totalBinaryExtracted);
      printer.beginRow();
      printer.addCell("Libraries with source");
      printer.addCell(source);
      printer.beginRow();
      printer.addCell("Source files extracted");
      printer.addCell(totalSourceExtracted);
      printer.beginRow();
      printer.addCell("Libraries with source errors");
      printer.addCell(sourceError);
      printer.beginRow();
      printer.addCell("Source files with errors");
      printer.addCell(totalSourceWithError);
      printer.addDividerRow();
      printer.endTable();
    }
    
    if (jars == null) {
      logger.info("Loading jars...");
      populateJars();
    }
    
    logger.info("Computing stats for " + jars.size() + " jars.");
    {
      int extracted = 0;
      int nonEmpty = 0;
      int source = 0;
      int sourceError = 0;
      int totalBinaryExtracted = 0;
      int totalSourceExtracted = 0;
      int totalSourceWithError = 0;
      for (ExtractedJar jar : jars) {
        if (jar.extracted()) {
          extracted++;
          if (jar.getExtractedFromBinaryCount() > 0) {
            nonEmpty++;
          }
          totalBinaryExtracted += jar.getExtractedFromBinaryCount();
          if (jar.hasSource()) {
            source++;
            totalSourceExtracted += jar.getExtractedFromSource();
          }
          if (jar.sourceError()) {
            sourceError++;
            totalSourceWithError += jar.getSourceFilesWithErrors();
          }
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
      printer.addCell("Binary files extracted");
      printer.addCell(totalBinaryExtracted);
      printer.beginRow();
      printer.addCell("Jars with source");
      printer.addCell(source);
      printer.beginRow();
      printer.addCell("Source files extracted");
      printer.addCell(totalSourceExtracted);
      printer.beginRow();
      printer.addCell("Jars with source errors");
      printer.addCell(sourceError);
      printer.beginRow();
      printer.addCell("Source files with errors");
      printer.addCell(totalSourceWithError);
      printer.addDividerRow();
      printer.endTable();
    }
    
    printer.close();
  }
}
