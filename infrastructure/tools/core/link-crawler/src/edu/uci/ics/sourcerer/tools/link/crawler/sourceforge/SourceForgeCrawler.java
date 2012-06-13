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
package edu.uci.ics.sourcerer.tools.link.crawler.sourceforge;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceBatch;
import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceProject;
import edu.uci.ics.sourcerer.tools.core.repo.model.ModifiableSourceRepository;
import edu.uci.ics.sourcerer.tools.core.repo.model.RepositoryFactory;
import edu.uci.ics.sourcerer.tools.core.repo.model.SourceProjectProperties;
import edu.uci.ics.sourcerer.util.LetterCounter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SourceForgeCrawler {
  public static final Argument<File> SOURCEFORGE_LIST = new FileArgument("sourceforge-list", "File containing list of SourceForge proejcts.");
  
  private SourceForgeCrawler() {
  }
  
  public static void addProjectsToRepository() {
    final TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Adding SourceForge projects from " + SOURCEFORGE_LIST + " to repository");
    
    final ModifiableSourceRepository repo = RepositoryFactory.INSTANCE.loadModifiableSourceRepository(RepositoryFactory.OUTPUT_REPO);
    
    SAXParser parser = null;
    try {
      SAXParserFactory fact = SAXParserFactory.newInstance();
      parser = fact.newSAXParser();
    } catch (ParserConfigurationException | SAXException e) {
      task.exception(e);
      return;
    }
    
    class Handler extends DefaultHandler {
      public boolean inGroupName = false;
      public boolean inUnixGroupName = false;
      public boolean inCvs = false;
      public boolean inSvn =false;
      
      private String groupName;
      private String unixGroupName;
      private boolean CVS;
      private boolean SVN;
      
      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName) {
          case "group_name": inGroupName = true; break;
          case "unix_group_name": inUnixGroupName = true; break;
          case "use_cvs": inCvs = true; break;
          case "use_svn": inSvn = true; break;
        }
      }
      
      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
          case "row": addProject(); break;
          case "group_name": inGroupName = false; break;
          case "unix_group_name": inUnixGroupName = false; break;
          case "use_cvs": inCvs = false; break;
          case "use_svn": inSvn = false; break;
        }
      }
      
      @Override
      public void characters(char[] ch, int start, int length) {
        if (inGroupName) {
          if (ch[start] == '"' && length > 1) {
            groupName = new String(ch, start + 1, length - 2);
          } else {
            groupName = new String(ch, start, length);
          }
        } else if (inUnixGroupName) {
          if (ch[start] == '"' && length > 1) {
            unixGroupName = new String(ch, start + 1, length - 2);
          } else {
            unixGroupName = new String(ch, start, length);
          }
        } else if (inCvs) {
          switch (ch[start]) {
            case '0': CVS = false; break;
            case '1': CVS = true; break;
            default: task.report(Level.SEVERE, "Unknown CVS state of " + ch[start] + " for " + unixGroupName);
          }
        } else if (inSvn) {
          switch (ch[start]) {
            case '0': SVN = false; break;
            case '1': SVN = true; break;
            default: task.report(Level.SEVERE, "Unknown SVNstate of " + ch[start] + " for " + unixGroupName);
          }
        }
      }
      
      private ModifiableSourceBatch batch = null;
      private LetterCounter counter = new LetterCounter();
      
      private void addProject() {
        if (batch == null) {
          batch = repo.createBatch();
          batch.getProperties().DESCRIPTION.setValue("Projects from SourceForge");
          batch.getProperties().save();
        } else if (batch.getProjectCount() >= 1000) {
          if (counter.getCount() == 0) {
            batch.getProperties().DESCRIPTION.setValue("Projects from SourceForge, Part " + counter.getNext());
            batch.getProperties().save();
          }
          batch = repo.createBatch();
          batch.getProperties().DESCRIPTION.setValue("Projects from SourceForge, Part " + counter.getNext());
          batch.getProperties().save();
        }
        ModifiableSourceProject newProject= batch.createProject();
        SourceProjectProperties props = newProject.getProperties();
        props.NAME.setValue(groupName);
        props.PROJECT_URL.setValue("http://sourceforge.net/projects/" + unixGroupName + "/");
        if (SVN) {
          props.SVN_URL.setValue("https://" + unixGroupName + ".svn.sourceforge.net/svnroot/" + unixGroupName);
        }
        if (CVS) {
          props.CVS_URL.setValue(":pserver:anonymous@" + unixGroupName + ".cvs.sourceforge.net:/cvsroot/" + unixGroupName);
        }
        props.SOURCE.setValue("SourceForge");
        props.save();
        
        groupName = null;
        unixGroupName = null;
        SVN = false;
        CVS = false;
        task.progress();
      }
    };
    Handler handler = new Handler();
    
    task.start("Adding projects", "projects added", 500);
    try {
      parser.parse(SOURCEFORGE_LIST.getValue(), handler);
    } catch (SAXException | IOException e) {
      task.exception(e);
    }
    task.finish();
    
    task.finish();
  }
}
