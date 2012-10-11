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
package edu.uci.ics.sourcerer.tools.java.db.exported;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentRelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.ComponentRelation;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.CollectionUtils;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedColumn;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedTable;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ComponentVerifier {
  public static Argument<File> JACCARD_TABLE = new RelativeFileArgument("jaccard-table", "jaccard-table.txt", Arguments.OUTPUT, "Table of jaccard values.");
  public static Argument<File> FRAGMENTED_TABLE = new RelativeFileArgument("fragmented-table", "fragmented-table.txt", Arguments.OUTPUT, "Table of fragmented jaccard values.");
  public static Argument<File> COMBINED_TABLE = new RelativeFileArgument("combined-table", "combined-table.txt", Arguments.OUTPUT, "Table of combined jaccard values.");
  public static Argument<File> FRAGMENTED_AND_COMBINED_TABLE = new RelativeFileArgument("fragmented-and-combined-table", "fragmented-and-combined-table.txt", Arguments.OUTPUT, "Table of fragmented and combined jaccard values.");
  public static Argument<File> JACCARD_LOG = new RelativeFileArgument("jaccard-log", "jaccard-log.txt", Arguments.OUTPUT, "Log of jaccard values.");
  public static Argument<File> IMPERFECT_JACCARD_LOG = new RelativeFileArgument("imperfect-jaccard-log", "imperfect-jaccard-log.txt", Arguments.OUTPUT, "Log of imperfect jaccard values.");
  
  public static void computeJaccard() {
    new DatabaseRunnable() {
      SelectQuery groupQuery = null;
      ConstantCondition<String> groupEquals = null;
      ConstantCondition<String> nameEquals = null;
      
      Averager<Double> jaccards = Averager.create();
      Averager<Double> fragmented = Averager.create();
      Averager<Double> combined = Averager.create();
      Averager<Double> fragmentedAndCombined = Averager.create();
      
      Averager<Integer> groupCount = Averager.create();
      Multiset<String> fragmentedLibs= HashMultiset.create();
      Set<String> combinedLibs = new HashSet<>();
      Set<String> perfectLibs = new HashSet<>();

      Set<Integer> ids = new HashSet<>();
      Set<Pair<String, String>> groups = new HashSet<>();
      

      private void compute(Integer libraryID, LogFileWriter writer, LogFileWriter imperfectWriter) {
        if (libraryID != null) {
          writer.writeAndIndent(Integer.toString(libraryID));
          Averager<Double> avg = Averager.create();
          Set<Integer> other = new HashSet<>();
          boolean superset = true;
          for (Pair<String, String> group : groups) {
            // Compute one jaccard for each
            groupEquals.setValue(group.getFirst());
            nameEquals.setValue(group.getSecond());
            String lib = group.getFirst() + "." + group.getSecond();
            fragmentedLibs.add(lib);
            if (lib.equals("patterntesting.patterntesting-aspectj")) {
              logger.info("wtf");
            }
            other.addAll(groupQuery.select().toCollection(ProjectsTable.PROJECT_ID));

            superset &= ids.containsAll(other);
            // Jaccard is size of intersection over size of union
            double jaccard = CollectionUtils.compuateJaccard(ids, other);
            writer.write(group.getFirst() + "." + group.getSecond() + " " + jaccard);
            avg.addValue(jaccard);
            other.clear();
          }
          writer.write("" + avg.getMean());
          writer.unindent();
          jaccards.addValue(avg.getMean());
          if (avg.getMean() < 1.0) {
            if (groups.size() > 1) {
              for (Pair<String, String> group : groups) {
                combinedLibs.add(group.getFirst() + "." + group.getSecond());
              }
              if (superset) {
                combined.addValue(avg.getMean());
              } else {
                fragmentedAndCombined.addValue(avg.getMean());
              }
              groupCount.addValue(groups.size());
            } else {
              fragmented.addValue(avg.getMean());
            }
            imperfectWriter.write(libraryID + " " + avg.getMean());
          } else {
            for (Pair<String, String> group : groups) {
              perfectLibs.add(group.getFirst() + "." + group.getSecond());
            }
          }
          ids.clear();
          groups.clear();
        }
      }
      
      @Override
      protected void action() {
        TaskProgressLogger task = TaskProgressLogger.get();
        
        // Set up the group query 
        groupQuery = exec.createSelectQuery(ProjectsTable.TABLE);
        groupQuery.addSelect(ProjectsTable.PROJECT_ID);
        groupEquals = ProjectsTable.GROUP.compareEquals();
        nameEquals = ProjectsTable.NAME.compareEquals();
        groupQuery.andWhere(groupEquals.and(nameEquals));
        
        
        // Get all the jars for each library
        QualifiedTable l2lv = ComponentRelationsTable.TABLE.qualify("a");
        QualifiedTable j2lv = ComponentRelationsTable.TABLE.qualify("b");
        try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.qualify(l2lv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(j2lv)), ComponentRelationsTable.SOURCE_ID.qualify(j2lv).compareEquals(ProjectsTable.PROJECT_ID));
             LogFileWriter writer = IOUtils.createLogFileWriter(JACCARD_LOG);
             LogFileWriter imperfectWriter = IOUtils.createLogFileWriter(IMPERFECT_JACCARD_LOG)) {
          QualifiedColumn<Integer> libraryIDcol = ComponentRelationsTable.SOURCE_ID.qualify(l2lv);
          query.addSelect(libraryIDcol, ProjectsTable.PROJECT_ID, ProjectsTable.GROUP, ProjectsTable.NAME);
          query.andWhere(ComponentRelationsTable.TYPE.qualify(l2lv).compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION), ComponentRelationsTable.TYPE.qualify(j2lv).compareEquals(ComponentRelation.JAR_MATCHES_LIBRARY_VERSION));
          query.orderBy(libraryIDcol, true);
          
          task.start("Querying project listing");
          TypedQueryResult result = query.select();
          task.finish();
          
          Integer lastLibraryID = null;
          
          task.start("Processing libraries", "libraries processed", 500);
          while (result.next()) {
            Integer libraryID = result.getResult(libraryIDcol);
            Integer id = result.getResult(ProjectsTable.PROJECT_ID);
            String group = result.getResult(ProjectsTable.GROUP);
            String name = result.getResult(ProjectsTable.NAME);
            if (!libraryID.equals(lastLibraryID)) {
              compute(lastLibraryID, writer, imperfectWriter);
              lastLibraryID = libraryID;
              task.progress();
            }
            ids.add(id);
            groups.add(new Pair<>(group, name));
          }
          compute(lastLibraryID, writer, imperfectWriter);
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Exception writing log file", e);
        }
        task.finish();
        
        task.start("Reporting general statistics");
        task.report("AVG: " + jaccards.getMean() + " +-" + jaccards.getStandardDeviation());
        task.report("Count: " + jaccards.getCount());
        task.report("MIN: " + jaccards.getMin());
        task.report("MAX: " + jaccards.getMax());
        jaccards.writeDoubleValueMap(JACCARD_TABLE.getValue(), 1);
        task.finish();
        
        task.start("Reporting fragmented statistics");
        task.report("AVG: " + fragmented.getMean() + " +-" + fragmented.getStandardDeviation());
        task.report("Count: " + fragmented.getCount());
        task.report("MIN: " + fragmented.getMin());
        task.report("MAX: " + fragmented.getMax());
        fragmented.writeDoubleValueMap(FRAGMENTED_TABLE.getValue(), 1);
        task.finish();
        
        task.start("Reporting combined statistics");
        task.report("AVG: " + combined.getMean() + " +-" + combined.getStandardDeviation());
        task.report("Count: " + combined.getCount());
        task.report("MIN: " + combined.getMin());
        task.report("MAX: " + combined.getMax());
        combined.writeDoubleValueMap(COMBINED_TABLE.getValue(), 1);
        task.finish();
        
        task.start("Reporting fragmented and combined statistics");
        task.report("AVG: " + fragmentedAndCombined.getMean() + " +-" + fragmentedAndCombined.getStandardDeviation());
        task.report("Count: " + fragmentedAndCombined.getCount());
        task.report("MIN: " + fragmentedAndCombined.getMin());
        task.report("MAX: " + fragmentedAndCombined.getMax());
        fragmentedAndCombined.writeDoubleValueMap(FRAGMENTED_AND_COMBINED_TABLE.getValue(), 1);
        task.finish();
        
        task.start("Reporting group size statistics");
        task.report("AVG: " + groupCount.getMean() + " +-" + groupCount.getStandardDeviation());
        task.report("Count: " + groupCount.getCount());
        task.report("MIN: " + groupCount.getMin());
        task.report("MAX: " + groupCount.getMax());
        task.finish();

        for (String lib : perfectLibs) {
          if (fragmentedLibs.count(lib) > 1) {
            task.report(lib + " should be perfect but is fragmented");
          }
          if (combinedLibs.contains(lib)) {
            task.report(lib + " should be perfect but is combined");
          }
        }
        int perfect = 0;
        int fragAndCombined = 0;
        Averager<Integer> frag = Averager.create();
        for (String lib : fragmentedLibs.elementSet()) {
          int count = fragmentedLibs.count(lib);
          if (count > 1) {
            frag.addValue(count);
            if (combinedLibs.contains(lib)) {
              fragAndCombined++;
            }
          } else if (!combinedLibs.contains(lib)) {
            perfect++;
          }
        }
        task.start("Reporting stats from maven pov");

        task.start("Reporting perfect match statistics");
        task.report("Count: " + perfect);
        task.finish();
        
        task.start("Reporting fragmentation statistics");
        task.report("AVG: " + frag.getMean() + " +-" + frag.getStandardDeviation());
        task.report("Count: " + frag.getCount());
        task.report("MIN: " + frag.getMin());
        task.report("MAX: " + frag.getMax());
        task.finish();
        
        task.start("Reporting combination statistics");
        task.report("Count: " + combinedLibs.size());
        task.finish();
        
        task.start("Reporting fragmented & combined statistics");
        task.report("Count: " + fragAndCombined);
        task.finish();
        
        task.finish();
      }
    }.run();
  }
}
