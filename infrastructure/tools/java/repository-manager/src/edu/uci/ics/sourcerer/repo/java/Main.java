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
package edu.uci.ics.sourcerer.repo.java;

import static edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository.EXTRACTION_STATS_FILE;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.JARS_DIR;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.OUTPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.JAR_INDEX_FILE;
import static edu.uci.ics.sourcerer.repo.stats.RepositoryStatistics.JAR_STATS_FILE;
import static edu.uci.ics.sourcerer.repo.stats.RepositoryStatistics.PROJECT_SIZES_FILE;
import static edu.uci.ics.sourcerer.repo.stats.DiskUsageCalculator.REPO_DISK_USAGE_FILE;
import static edu.uci.ics.sourcerer.repo.stats.RepositoryStatistics.PROJECT_NAMES_FILE;
import static edu.uci.ics.sourcerer.repo.tools.RepositoryFilterer.COMPRESSED_FILTERED_REPO_FILE;
import static edu.uci.ics.sourcerer.repo.tools.RepositoryFilterer.REPO_SUBSET_RATE;
import static edu.uci.ics.sourcerer.util.io.TablePrettyPrinter.CSV_MODE;

import java.util.Set;

import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.stats.DiskUsageCalculator;
import edu.uci.ics.sourcerer.repo.stats.RepositoryStatistics;
import edu.uci.ics.sourcerer.repo.tools.RepositoryFilterer;
import edu.uci.ics.sourcerer.tools.core.repo.base.Repository;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.arguments.ArgumentManager;
import edu.uci.ics.sourcerer.util.io.arguments.Command;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  public static final Command AGGREGATE_JAR_FILES =
    new Command("aggregate-jar-files", "Collects all the project jar files into the jars directory.") {
      protected void action() {
        Repository repo = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
        repo.aggregateJarFiles();
      }
    }.setProperties(INPUT_REPO, JARS_DIR);

  public static final Command CREATE_JAR_INDEX =
    new Command("create-jar-index", "Creates a jar index for all the jars in the jars directory.") {
      protected void action() {
        Repository repo = Repository.getRepository(INPUT_REPO.getValue());
        repo.createJarIndex();
      }
    }.setProperties(INPUT_REPO, JARS_DIR, JAR_INDEX_FILE);

  public static final Command PRINT_JAR_STATS =
    new Command("print-jar-stats", "Prints statistics regarding the jars in the repository.") {
      protected void action() {
        Repository repo = Repository.getRepository(INPUT_REPO.getValue());
        RepositoryStatistics.printJarStatistics(repo);
      }
    }.setProperties(INPUT_REPO, JARS_DIR, JAR_INDEX_FILE, JAR_STATS_FILE.asOutput());      

  public static final Command PRINT_PROJECT_SIZES =
    new Command("print-project-sizes", "Prints size statistics on the projects in the repository.") {
      protected void action() {
        Repository repo = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
        RepositoryStatistics.printProjectSizes(repo);
      }
    }.setProperties(INPUT_REPO, PROJECT_SIZES_FILE.asOutput());
    
    
  public static final Command PRINT_REPO_DISK_USAGE =
    new Command("print-repo-disk-usage", "Prints statistics on the repository's disk usage.") {
      protected void action() {
        Repository repo = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
        DiskUsageCalculator.printRepositoryDiskUsage(repo);
      }
    }.setProperties(INPUT_REPO, REPO_DISK_USAGE_FILE.asOutput(), CSV_MODE);
    
  public static final Command COMPRESS_FILTERED_REPOSITORY =
    new Command("compress-filtered-repo", "Creates a compressed repository containing only the filtered files.") {
      protected void action() {
        Repository repo = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
        RepositoryFilterer.compressFilteredRepository(repo, false);
      }
    }.setProperties(INPUT_REPO, COMPRESSED_FILTERED_REPO_FILE.asOutput());
    
  public static final Command COMPRESS_FILTERED_REPOSITORY_SUBSET =
    new Command("compress-filtered-repo-subset", "Creates a compressed repository containing only the filtered files.") {
      protected void action() {
        Repository repo = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
        RepositoryFilterer.compressFilteredRepository(repo, true);
      }
    }.setProperties(INPUT_REPO, COMPRESSED_FILTERED_REPO_FILE.asOutput(), REPO_SUBSET_RATE);
    
  public static final Command CREATE_REPOSITORY_SUBSET =
    new Command("create-repo-subset", "Creates a subset of the main repository.") {
      protected void action() {
        Repository repo = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
        RepositoryFilterer.createRepositorySubset(repo);
      }
    }.setProperties(INPUT_REPO, OUTPUT_REPO, REPO_SUBSET_RATE);
    
  public static final Command PRINT_PROJECT_NAMES =
    new Command("print-project-names", "Prints the names of all the projects in the repository.") {
      protected void action() {
        if (INPUT_REPO.hasValue()) {
          Repository repo = Repository.getRepository(INPUT_REPO.getValue());
          RepositoryStatistics.printProjectNames(repo);
        } else if (OUTPUT_REPO.hasValue()) {
          ExtractedRepository repo = ExtractedRepository.getRepository(OUTPUT_REPO.getValue());
          RepositoryStatistics.printProjectNames(repo);
        } else {
          throw new IllegalStateException("This should have been caught by the property manager");
        }
      }
    }.setProperties(PROJECT_NAMES_FILE.asOutput(), CSV_MODE, INPUT_REPO, OUTPUT_REPO);
  
  public static final Command MIGRATE_REPOSITORY =
    new Command("migrate-repository", "Migrates (while compressing) the input repository to the target repository.") {
      protected void action() {
        Set<String> completed = Logging.initializeResumeLogger();
        Repository.migrateRepository(INPUT_REPO.getValue(), OUTPUT_REPO.getValue(), completed);
      }
    }.setProperties(INPUT_REPO, OUTPUT_REPO, JARS_DIR, JAR_INDEX_FILE);

  public static final Command CLEAN_JAR_MANIFESTS = 
    new Command("clean-jar-manifests", "Removes Class-Path entries from manifest files.") {
      protected void action() {
        Repository.getRepository(INPUT_REPO.getValue()).getJarIndex().cleanManifestFiles();
      }
    }.setProperties(INPUT_REPO, JARS_DIR, JAR_INDEX_FILE);
  
  public static final Command EXTRACTION_STATS = 
    new Command("extraction-stats", "Get extraction stats.") {
      protected void action() {
        ExtractedRepository.getRepository(INPUT_REPO.getValue()).computeExtractionStats();
      }
    }.setProperties(INPUT_REPO, EXTRACTION_STATS_FILE.asOutput());


//  public static final Command SPLIT_PROJECTS = 
//      new Command("split-projects", "Split the projects into filter lists for easy parallelization.") {
//        protected void action() {
//          Repository.getRepository(INPUT_REPO.getValue()).splitProjectsForFilterList();
//        }
//      }.setProperties(INPUT_REPO, SPLIT_SIZE);

  public static void main(String[] args) {
    ArgumentManager.executeCommand(args, Main.class);
  }
}
