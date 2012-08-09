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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
import edu.uci.ics.sourcerer.util.Nullerator;
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
    private final Map<String, Integer> fileMap;

    private boolean inFindBugsSummary;
    
    private Handler(Integer projectID, Map<String, Integer> fileMap) {
      this.projectID = projectID;
      this.fileMap = fileMap;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      switch (qName) {
        case "FindBugsSummary": inFindBugsSummary = true; break;
//        case "PackageStats": inPackageStats= true; break;
//        case "ClassStats": inClassStats = inClassStats; break;
      }
    }
  };
  
  @Override
  protected void doImport() {
    try (SelectQuery projectQuery = exec.createSelectQuery(ProjectsTable.TABLE);
         SelectQuery filesQuery = exec.createSelectQuery(FilesTable.TABLE);) {
      projectQuery.addSelect(ProjectsTable.PROJECT_ID);
      ConstantCondition<String> equalsHash = ProjectsTable.HASH.compareEquals();
      projectQuery.andWhere(equalsHash);
      
      filesQuery.addSelect(FilesTable.FILE_ID, FilesTable.PATH);
      ConstantCondition<Integer> equalsProjectID = FilesTable.PROJECT_ID.compareEquals();
      filesQuery.andWhere(equalsProjectID);
      
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
        task.start("Adding " + name + "'s FindBugs metrics");
        
        Integer projectID = null;
        
        task.start("Loading project");
        equalsHash.setValue(jar.getProperties().HASH.getValue());
        projectID = projectQuery.select().toSingleton(ProjectsTable.PROJECT_ID, true);
        task.finish();
        
        if (projectID == null) {
          task.report("Unable to locate project for: " + jar.getProperties().HASH.getValue());
        } else {
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
          
          File findbugsFile = new File(jar.getExtractionDir(), "findbugs.xml");
          Handler handler = new Handler(projectID, fileMap);
          parser.parse(jar.getExtractionDir(), dh)
        }
        task.finish();
      }
    }
  }
}
