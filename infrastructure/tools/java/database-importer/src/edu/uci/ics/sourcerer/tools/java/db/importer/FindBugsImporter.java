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
package edu.uci.ics.sourcerer.tools.java.db.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FileMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.FindBugsRunner;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.Nullerator;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FindBugsImporter extends DatabaseImporter {
  private Nullerator<ExtractedJarFile> jars;
  
  protected FindBugsImporter(Nullerator<ExtractedJarFile> jars) {
    super("Adding findbugs metrics");
    this.jars = jars;
  }
  
  private class Handler extends DefaultHandler {
    private final Integer projectID;
    private final Map<String, Integer> packageMap;
    private final Map<String, Integer> fileMap;

    private final Averager<Double> totalSize = Averager.create();
    private final Averager<Double> totalBugs = Averager.create();
    private final Averager<Double> totalPriority1 = Averager.create();
    private final Averager<Double> totalPriority2 = Averager.create();
    private final Averager<Double> totalPriority3 = Averager.create();
    
    private boolean inFindBugsSummary;
    
    private BatchInserter entityInserter;
    private BatchInserter fileInserter;
    
    private Handler(Integer projectID, Map<String, Integer> packageMap, Map<String, Integer> fileMap) {
      this.projectID = projectID;
      this.packageMap = packageMap;
      this.fileMap = fileMap;
      entityInserter = exec.makeInFileInserter(FileUtils.getTempDir(), EntityMetricsTable.TABLE);
      fileInserter = exec.makeInFileInserter(FileUtils.getTempDir(), FileMetricsTable.TABLE);
    }
    
    private Double readAtt(Attributes attributes, String name) {
      String att = attributes.getValue(name);
      if (att == null) {
//        task.report(Level.SEVERE, "Missing " + name);
        return null;
      } else {
        return Integer.valueOf(att).doubleValue();
      }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      if ("FindBugsSummary".equals(qName)) {
        inFindBugsSummary = true;
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.FB_TOTAL_CLASSES, readAtt(attributes, "total_classes"), null, null, null, null));
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.FB_REFERENCED_CLASSES, readAtt(attributes, "referenced_classes"), null, null, null, null));
      } else if (inFindBugsSummary) {
        if ("PackageStats".equals(qName)) {
          String pkg = attributes.getValue("package");
          if (pkg == null) {
            task.report(Level.SEVERE, "Missing package");
          } else {
            Integer entityID = packageMap.get(pkg);
            if (entityID == null) {
              task.report(Level.SEVERE, "Missing entity for package: " + pkg);
            } else {
              entityInserter.addInsert(EntityMetricsTable.createInsert(projectID, null, entityID, Metric.FB_TOTAL_CLASSES, readAtt(attributes, "total_types")));
              exec.insert(EntityMetricsTable.createInsert(projectID, null, entityID, Metric.FB_SIZE, readAtt(attributes, "total_size")));
              
              Double bugs = readAtt(attributes, "total_bugs");
              Double priority1 = readAtt(attributes, "priority_1");
              if (priority1 == null) {
                priority1 = 0.0d;
              }
              Double priority2 = readAtt(attributes, "priority_2");
              if (priority2 == null) {
                priority2 = 0.0d;
              }
              Double priority3 = bugs - priority1 - priority2;
              entityInserter.addInsert(EntityMetricsTable.createInsert(projectID, null, entityID, Metric.FB_BUGS, bugs));
              entityInserter.addInsert(EntityMetricsTable.createInsert(projectID, null, entityID, Metric.FB_PRIORITY_1, priority1));
              entityInserter.addInsert(EntityMetricsTable.createInsert(projectID, null, entityID, Metric.FB_PRIORITY_2, priority2));
              entityInserter.addInsert(EntityMetricsTable.createInsert(projectID, null, entityID, Metric.FB_PRIORITY_3, priority3));
            }
          }
        } else if ("ClassStats".equals(qName)) {
          String className = attributes.getValue("class");
          if (className == null) {
            task.report(Level.SEVERE, "Missing class name");
          } else {
            Integer fileID = fileMap.get(className + ".class");
            if (fileID == null) {
              task.report(Level.SEVERE, "Missing file: " + className);
            } else {
              Double size = readAtt(attributes, "size");
              fileInserter.addInsert(FileMetricsTable.createInsert(projectID, fileID, Metric.FB_SIZE, size));
              totalSize.addValue(size);
              Double bugs = readAtt(attributes, "bugs");
              Double priority1 = readAtt(attributes, "priority_1");
              if (priority1 == null) {
                priority1 = 0.0d;
              }
              Double priority2 = readAtt(attributes, "priority_2");
              if (priority2 == null) {
                priority2 = 0.0d;
              }
              Double priority3 = bugs - priority1 - priority2;
              fileInserter.addInsert(FileMetricsTable.createInsert(projectID, fileID, Metric.FB_BUGS, bugs));
              totalBugs.addValue(bugs);
              fileInserter.addInsert(FileMetricsTable.createInsert(projectID, fileID, Metric.FB_PRIORITY_1, priority1));
              totalPriority1.addValue(priority1);
              fileInserter.addInsert(FileMetricsTable.createInsert(projectID, fileID, Metric.FB_PRIORITY_2, priority2));
              totalPriority2.addValue(priority2);
              fileInserter.addInsert(FileMetricsTable.createInsert(projectID, fileID, Metric.FB_PRIORITY_3, priority3));
              totalPriority3.addValue(priority3);
            }
          }
        }
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if ("FindBugsSummary".equals(qName)) {
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.FB_SIZE, totalSize));
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.FB_BUGS, totalBugs));
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.FB_PRIORITY_1, totalPriority1));
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.FB_PRIORITY_2, totalPriority2));
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.FB_PRIORITY_3, totalPriority3));
        entityInserter.insert();
        fileInserter.insert();
        inFindBugsSummary = false;
      }
    }
  };
  
  @Override
  protected void doImport() {
    try (SelectQuery projectQuery = exec.createSelectQuery(ProjectsTable.TABLE);
         SelectQuery filesQuery = exec.createSelectQuery(FilesTable.TABLE);
         SelectQuery pkgQuery = exec.createSelectQuery(EntitiesTable.TABLE);) {
      projectQuery.addSelect(ProjectsTable.PROJECT_ID);
      ConstantCondition<String> equalsHash = ProjectsTable.HASH.compareEquals();
      projectQuery.andWhere(equalsHash);
      
      filesQuery.addSelect(FilesTable.FILE_ID, FilesTable.PATH);
      ConstantCondition<Integer> equalsProjectID = FilesTable.PROJECT_ID.compareEquals();
      filesQuery.andWhere(equalsProjectID);
      
      pkgQuery.addSelect(EntitiesTable.ENTITY_ID, EntitiesTable.FQN);
      ConstantCondition<Integer> equalsProjectID2 = EntitiesTable.PROJECT_ID.compareEquals();
      pkgQuery.andWhere(equalsProjectID2, EntitiesTable.ENTITY_TYPE.compareEquals(Entity.PACKAGE));
      
      task.start("Adding FindBugs results");
      SAXParser parser = null;
      try {
        parser = SAXParserFactory.newInstance().newSAXParser();
      } catch (ParserConfigurationException | SAXException e) {
       task.exception(e);
       return;
      }
      
      ExtractedJarFile jar = null;
      while ((jar = jars.next()) != null) {
        String name = jar.getProperties().NAME.getValue();
        task.start("Adding " + name + "'s FindBugs results");
        
        Integer projectID = null;
        
        task.start("Loading project");
        equalsHash.setValue(jar.getProperties().HASH.getValue());
        projectID = projectQuery.select().toSingleton(ProjectsTable.PROJECT_ID, true);
        if (projectID == null) {
          task.report("Unable to locate project for: " + jar.getProperties().HASH.getValue());
          task.finish();
          task.finish();
          continue;
        }
        task.finish();
        
        Map<String, Integer> packageMap = new HashMap<>();
        {
          task.start("Loading packages", "packages loaded");
          equalsProjectID2.setValue(projectID);
          TypedQueryResult result = pkgQuery.select();
          while (result.next()) {
            Integer entityID = result.getResult(EntitiesTable.ENTITY_ID);
            String fqn = result.getResult(EntitiesTable.FQN);
            packageMap.put(fqn, entityID);
            task.progress();
          }
          task.finish();
        }
        
        Map<String, Integer> fileMap = new HashMap<>();
        {
          task.start("Loading files", "filed loaded");
          equalsProjectID.setValue(projectID);
          TypedQueryResult result = filesQuery.select();
          while (result.next()) {
            Integer fileID = result.getResult(FilesTable.FILE_ID);
            String path = result.getResult(FilesTable.PATH);
            fileMap.put(path, fileID);
            task.progress();
          }
          task.finish();
        }

        task.start("Parsing results file");
        InputStream is = null;
        Handler handler = new Handler(projectID, packageMap, fileMap);
        try {
          // Check for the uncompressed file
          File findbugsFile = new File(jar.getExtractionDir().toFile(), FindBugsRunner.FINDBUGS_FILE_NAME.getValue());
          if (findbugsFile.exists()) {
            is = new FileInputStream(findbugsFile);
          } else {
            // Check for the compressed file
            if (jar.getCompressedFile().exists()) {
              String entryName = FindBugsRunner.FINDBUGS_FILE_NAME.getValue();
              ZipInputStream zis = new ZipInputStream(new FileInputStream(jar.getCompressedFile().toFile()));
              ZipEntry entry = null;
              while ((entry = zis.getNextEntry()) != null) {
                if (entryName.equals(entry.getName())) {
                  is = zis;
                  break;
                }
              }
            }
          }
          
          if (is == null) {
            task.report("Unable to find FindBugs file for: " + jar);
          } else {
            parser.parse(is, handler);
          }
          task.finish();
        } catch (SAXException | IOException e) {
          task.exception(e);
        } finally {
          IOUtils.close(is);
        }
        
        task.finish();
      }
      task.finish();
    }
  }
}
