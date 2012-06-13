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
package edu.uci.ics.sourcerer.tools.link;

import edu.uci.ics.sourcerer.tools.core.repo.model.RepositoryFactory;
import edu.uci.ics.sourcerer.tools.link.crawler.flossmole.FlossmoleCrawler;
import edu.uci.ics.sourcerer.tools.link.crawler.sourceforge.SourceForgeCrawler;
import edu.uci.ics.sourcerer.tools.link.downloader.RepoBuilder;
import edu.uci.ics.sourcerer.tools.link.downloader.Subversion;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.Command;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  public static final DualFileArgument GOOGLE_CODE_JAVA_LIST = new DualFileArgument("google-code-java-list", "google-code-java-list.txt", "File containing list of crawled Google Code projects containing Java in their svn repositories.");
  
  public static final Command CRAWL_GOOGLE_CODE = new Command("crawl-google-code", "Gets the listing of google code projects from Flossmole.") {
    @Override
    protected void action() {
      FlossmoleCrawler.crawlGoogleCode();
    }
  }.setProperties(FlossmoleCrawler.GOOGLE_CODE_LIST.asOutput(), DatabaseConnectionFactory.DATABASE_URL, DatabaseConnectionFactory.DATABASE_USER, DatabaseConnectionFactory.DATABASE_PASSWORD);
  
  public static final Command FILTER_GC_PROJECTS_FOR_JAVA = new Command("filter-gc-projects-for-java", "Filters the Google Code projects for the projects that contain Java in their SVN.") {
    @Override
    protected void action() {
      Subversion.filterSubversionLinksForJava(FlossmoleCrawler.GOOGLE_CODE_LIST, GOOGLE_CODE_JAVA_LIST);
    }
  }.setProperties(Arguments.INPUT, FlossmoleCrawler.GOOGLE_CODE_LIST.asInput(), GOOGLE_CODE_JAVA_LIST.asOutput(), Subversion.FAST_CONTAINS_JAVA);
  
  public static final Command ADD_JAVA_GC_PROJECTS_TO_REPO = new Command("add-java-gc-projects-to-repo", "Adds the Java Google Code projects to the repository.") {
    @Override
    protected void action() {
      FlossmoleCrawler.addProjectsToRepository(GOOGLE_CODE_JAVA_LIST);
    }
  }.setProperties(Arguments.INPUT, GOOGLE_CODE_JAVA_LIST.asInput(), RepositoryFactory.OUTPUT_REPO);
  
  public static final Command ADD_SOURCEFORGE_PROJECTS_TO_REPO = new Command("add-sourceforge-projects-to-repo", "Adds the SourceForge projecst to the repository.") {
    @Override
    protected void action() {
      SourceForgeCrawler.addProjectsToRepository();
    }
  }.setProperties(SourceForgeCrawler.SOURCEFORGE_LIST, RepositoryFactory.OUTPUT_REPO);
  
  public static final Command DOWNLOAD_REPO_CONTENT = new Command("download-repo-content", "Downloads the repository content.") {
    @Override
    protected void action() {
      RepoBuilder.downloadProjectContent();
    }
  }.setProperties(RepositoryFactory.INPUT_REPO);
  
  public static final Command INTERACTIVE_REPO_ADDER = new Command("interactive-repo-adder", "Tool for manually adding projects to the repository.") {
    @Override
    protected void action() {
      RepoBuilder.interactiveRepositoryAdder();
    }
  }.setProperties(RepositoryFactory.OUTPUT_REPO);
  
  public static final Command CLEAN_VERSIONING_CONTENT = new Command("clean-versioning-content", "Removes all the .svn and CVS directories.") {
    @Override
    protected void action() {
      RepoBuilder.cleanVersioningContent();
    }
  }.setProperties(RepositoryFactory.INPUT_REPO);

  public static void main(String[] args) {
    Command.execute(args, Main.class);
  }
}
