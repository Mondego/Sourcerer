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
package edu.uci.ics.sourcerer.util.io;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public enum Property {
  // -- Begin General Properties --
  PROPERTIES_FILE("properties"),
  INPUT("input"),
  OUTPUT("output"),
  // -- End General Properties --
  
  // -- Begin Logging Propeties --
  INFO_LOG("info-log", "info.log"),
  ERROR_LOG("error-log", "error.log"),
  RESUME_LOG("resume-log", "resume.log"),

  PROMPT_MISSING("prompt-missing", true),
  REPORT_TO_CONSOLE("report-to-console", true),
  SUPPRESS_FILE_LOGGING("suppress-file-logging", true),
  CLEAR_RESUME_LOG("clear-resume-log", true),
  // -- End Logging Propeties --

  // -- Begin Extractor Properties --
  REPO_MODE("repo-mode", true),
  JAR_ONLY("jar-only", true),
  
  FORCE_REDO("force-redo", true),
  
  PPA("ppa", true),

  IMPORT_WRITER("import-writer", null),
  PROBLEM_WRITER("problem-writer", null),
  ENTITY_WRITER("entity-writer", null),
  JAR_ENTITY_WRITER("jar-entity-writer", null),
  LOCAL_VARIABLE_WRITER("local-variable-writer", null),
  RELATION_WRITER("relation-writer", null),
  JAR_RELATION_WRITER("jar-relation-writer", null),
  COMMENT_WRITER("comment-writer", null),
  FILE_WRITER("file-writer", null),
  JAR_FILE_WRITER("jar-file-writer", null),
  
  IMPORT_FILE("import-file", "imports.txt"),
  PROBLEM_FILE("problem-file", "problems.txt"),
  ENTITY_FILE("entity-file", "entities.txt"),
  LOCAL_VARIABLE_FILE("local-variables-file", "local-variables.txt"),
  RELATION_FILE("relations-file", "relations.txt"),
  COMMENT_FILE("comment-file", "comments.txt"),
  FILE_FILE("file-file", "files.txt"),
  JAR_FILE_FILE("jar-file-file", "jar-files.txt"),
  // -- End Extractor Properties --
  
  // -- Begin Repository Manager Properties --
  REPO_ROOT("repo-root"),
  
  CREATE_JAR_INDEX("create-jar-index", true),
  JAR_INDEX_FILE("jar-index", "index.txt"),
  
  AGGREGATE_JAR_FILES("aggregate-jar-files", true),
  CALCULATE_JAR_NAME_SIMILARITY("calculate-jar-name-similarity", true),
  
  CRAWL_MAVEN("crawl-maven", true),
  DOWNLOAD_MAVEN("download-maven", true),
  MAVEN_STATS("fix-maven", true),
  LINKS_FILE("links-file", "links.txt"),
  MAVEN_URL("maven-url"),
  ADD_MAVEN_JARS("add-maven-jars", true),
  
  CLEAN_REPOSITORY("clean-repository", true),
  MIGRATE_REPOSITORY("migrate-repository", true),
  // -- End Repository Manager Properties --
  
  // -- Begin Repository File Server Properties --
  REPO_FILE_SERVER_URL("repo-file-server-url", "http://nile.ics.uci.edu:9180/repofileserver/"),
  // -- End Repository File Server Properties --
  
  // -- Begin Slicer File Server Properties --
  SLICING_FILE_SERVER_URL("slicing-file-server-url", "http://nile.ics.uci.edu:9180/slicingserver/"),
  // -- End Slicer File Server Properties --
  
  // -- Begin Database Properties --
  DATABASE_URL("db-url", "jdbc:mysql://tagus.ics.uci.edu:3306/sourcerer"),
  DATABASE_USER("ub-user", "sourcerer"),
  DATABASE_PASSWORD("db-passwd", "sourcerer4us"),
  // -- End Database Properties --

  // -- Begin Database Tools Properties --
  INITIALIZE_DATABASE("initialize-db", true),
  ADD_JARS("add-jars", true),
  ADD_PROJECTS("add-projects", true),
  // -- End Add Project Properties --
  
  // -- Begin Statistics Properties --
  REPO_STATS_FILE("repo-stats-file", "repo-stats.txt"),
  JDK_STATS_FILE("jdk-stats-file", "jdk-stats.txt"),
  POPULAR_JARS_FILE("popular-jars-file", "popular-jars.txt"),
  NAME_STATS_FILE("name-stats-file", "name-stats.txt"),
  // -- End Statistics Properties --
  
  // -- Begin Automated Slice Tester Properties --
  COUNT("count", "100"),
  RESULTS("results", "results.txt"),
  // -- End Automated Slice Tester Properties --
  
  // -- Begin Calculate Precision Recall Properties --
  STATS_FILE("top-stats-file", "top-stats.txt"),
  TOP_K("top-k", "10"),
  PR_FILE("precision-recall-file", "precision-recall.txt"),
  PR_THRESHOLD("pr-relevant-threshold", "0"),
  CSV_MODE("csv-mode", true),
  // -- End Calculate Precision Recall Properties --
  ;
    
  private String name;
  private String defaultValue;
  private boolean hasDefaultValue = false;
  private boolean isFlag = false;
  
  private Property(String name) {
    this.name = name;
  }
  
  private Property(String name, String defaultValue) {
    this.name = name;
    this.defaultValue = defaultValue;
    this.hasDefaultValue = true;
  }
  
  private Property(String name, boolean isFlag) {
    this.name = name;
    this.isFlag = isFlag;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean hasDefaultValue() {
    return hasDefaultValue;
  }
  
  public String getDefaultValue() {
    return defaultValue;
  }
  
  public boolean isFlag() {
    return isFlag;
  }
  
  public static Property parse(String s) {
    for (Property prop : values()) {
      if (prop.getName().equals(s)) {
        return prop;
      }
    }
    return null;
  }
}
