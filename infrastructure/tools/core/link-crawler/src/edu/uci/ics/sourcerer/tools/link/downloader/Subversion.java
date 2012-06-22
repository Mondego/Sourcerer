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
package edu.uci.ics.sourcerer.tools.link.downloader;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import edu.uci.ics.sourcerer.tools.link.model.Project;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.TimeCounter;
import edu.uci.ics.sourcerer.util.io.EntryWriter;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.Logging;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class Subversion {
  public static final Argument<Boolean> FAST_CONTAINS_JAVA = new BooleanArgument("fast-contains-java", false, "Immediately discount project if it contains a c, c++ or python file.");
  
  private Subversion() {}
  
  public static void filterSubversionLinksForJava(DualFileArgument in, DualFileArgument out) {
    logger.info("Filtering " + in + " for Java projects...");
    Set<String> resume = Logging.initializeResumeLogger();
    
    DAVRepositoryFactory.setup();
      
    SimpleSerializer writer = null;
    EntryWriter<Project> ew = null;
    try {
      writer = IOUtils.resumeSimpleSerializer(out);
      ew = writer.getEntryWriter(Project.class);
      
      int noSVN = 0;
      int withoutJava = 0;
      int withJava = 0;
      
      TimeCounter timer = new TimeCounter(25, 2, "projects processed");
      if (!resume.isEmpty()) {
        timer.setCount(resume.size());
        timer.logTimeAndCount(0, "projects already processed");
        timer.lap();
      }
      
      SVNRepository repo = null;
      boolean fast = FAST_CONTAINS_JAVA.getValue();
      
      for (Project project : IOUtils.deserialize(Project.class, in, true)) {
        if (!resume.contains(project.getName())) {
          try {
            logger.info("Examining " + project.getName());
            repo = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(project.getUrl()));
            SVNNodeKind nodeKind = repo.checkPath("", -1);
            if (nodeKind == SVNNodeKind.DIR) {
              Collection<String> trunks = findTrunks(repo);
              if (trunks == null) {
                logger.log(Level.WARNING, "Unable to find trunk for " + project.getName());
                trunks = Collections.singleton("");
              } else {
                logger.info("  Trunk(s) located");
              }
              boolean containsJava = false;
              if (fast) {
                containsJava = fastContainsJava(repo, trunks);
              } else {
                containsJava = containsJava(repo, trunks);
              }
              if (containsJava) {
                ew.write(project);
                ew.flush();
                withJava++;
              } else {
                withoutJava++;
              }
            } else if (nodeKind == SVNNodeKind.NONE) {
              logger.info("  No SVN");
              noSVN++;
            } else {
              logger.log(Level.SEVERE, "Unexpected node kind: " + nodeKind + " for " + project.getName());
            }
            logger.log(Logging.RESUME, project.getName());
          } catch (SVNException e) {
            logger.log(Level.SEVERE, "SVN Exception", e);
            noSVN++;
          } finally {
            if (repo != null) {
              repo.closeSession();
            }
          }
          timer.increment();
        }
      }
      
      timer.logTotalTimeAndCount(0, "projects processed");
      logger.info(withJava + " Java projects.");
      logger.info(withoutJava + " non-Java projects.");
      logger.info(noSVN + " projects with no svn.");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading from file system.", e);
    } finally {
      IOUtils.close(ew, writer);
    }
  }
  
  @SuppressWarnings("unchecked")
  private static Collection<String> findTrunks(SVNRepository repo) throws SVNException {
    Collection<SVNDirEntry> entries = repo.getDir("", -1, null, (Collection<?>)null);
    
    // Look for a top-level trunk 
    for (SVNDirEntry entry : entries) {
      if (entry.getKind() == SVNNodeKind.DIR && "trunk".equals(entry.getName())) {
        return Collections.singleton(entry.getName());
      }
    }
    
    // Now try second-level trunks
    Collection<String> trunks = new LinkedList<>();
    for (SVNDirEntry entry : entries) {
      if (entry.getKind() == SVNNodeKind.DIR) {
        for (SVNDirEntry subEntry : (Collection<SVNDirEntry>)repo.getDir(entry.getName(), -1, null, (Collection<?>)null)) {
          if (subEntry.getKind() == SVNNodeKind.DIR && "trunk".equals(subEntry.getName())) {
            trunks.add(entry.getName() + "/" + subEntry.getName());
          }
        }
      }
    }
    
    if (trunks.isEmpty()) {
      // Give up
      return null;
    } else {
      return trunks;
    }
  }
  
  @SuppressWarnings("unchecked")
  private static boolean fastContainsJava(SVNRepository repo, Collection<String> trunks) throws SVNException {
    logger.info("  Looking for Java...");
    Deque<String> stack = Helper.newStack();
    stack.addAll(trunks);
    TimeCounter timer = new TimeCounter(10, 4, "directories examined");
    while (!stack.isEmpty()) {
      String path = stack.pop();
      timer.increment();
      Collection<SVNDirEntry> entries = repo.getDir(path, -1, null, (Collection<?>) null);
      for (SVNDirEntry entry : entries) {
        if (entry.getKind() == SVNNodeKind.FILE) {
          if (entry.getName().endsWith(".java") || entry.getName().endsWith(".jar")) {
            timer.logTimeAndCount(4, "directories examined before Java found");
            return true;
          } else if (entry.getName().endsWith(".c") || entry.getName().endsWith(".cpp") || entry.getName().endsWith(".h")) {
            timer.logTimeAndCount(4, "directories examined before c found");
            return false;
          } else if (entry.getName().endsWith(".py")) {
            timer.logTimeAndCount(4, "directories examined before python found");
            return false;
          }
        } else if (entry.getKind() == SVNNodeKind.DIR) {
          stack.push(path.equals("") ? entry.getName() : path + "/" + entry.getName());
        }
      }
    }
    timer.logTimeAndCount(4, "directories examined and no Java found");
    return false;
  }
  
  @SuppressWarnings("unchecked")
  private static boolean containsJava(SVNRepository repo, Collection<String> trunks) throws SVNException {
    logger.info("  Looking for Java...");
    Deque<String> stack = Helper.newStack();
    stack.addAll(trunks);
    TimeCounter timer = new TimeCounter(10, 4, "directories examined");
    while (!stack.isEmpty()) {
      String path = stack.pop();
      timer.increment();
      Collection<SVNDirEntry> entries = repo.getDir(path, -1, null, (Collection<?>) null);
      for (SVNDirEntry entry : entries) {
        if (entry.getKind() == SVNNodeKind.FILE) {
          if (entry.getName().endsWith(".java") || entry.getName().endsWith(".jar")) {
            timer.logTimeAndCount(4, "directories examined before Java found");
            return true;
          }
        } else if (entry.getKind() == SVNNodeKind.DIR) {
          stack.push(path.equals("") ? entry.getName() : path + "/" + entry.getName());
        }
      }
    }
    timer.logTimeAndCount(4, "directories examined and no Java found");
    return false;
  }
  
  @SuppressWarnings("unchecked")
  public static boolean download(String url, File target) {
    DAVRepositoryFactory.setup();
    
    SVNRepository repo = null;
    try {
      repo = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(url));
      SVNNodeKind nodeKind = repo.checkPath("", -1);
      if (nodeKind == SVNNodeKind.DIR) {
        Collection<String> trunks = findTrunks(repo);
        if (trunks == null) {
          logger.log(Level.WARNING, "Unable to find trunk for " + url);
          trunks = Collections.singleton("");
        }
        
        Deque<String> stack = Helper.newStack();
        stack.addAll(trunks);
        
        TimeCounter timer = new TimeCounter(10, 2, "files downloaded");
        while (!stack.isEmpty()) {
          String path = stack.pop();
          File dir = new File(target, path);
          Collection<SVNDirEntry> entries = repo.getDir(path, -1, null, (Collection<?>) null);
          for (SVNDirEntry entry : entries) {
            String child = path.equals("") ? entry.getName() : path + "/" + entry.getName(); 
            if (entry.getKind() == SVNNodeKind.FILE) {
              dir.mkdirs();
              try (OutputStream os = IOUtils.makeOutputStream(new File(dir, entry.getName()))) {
                repo.getFile(child, -1, null, os);
              }
              timer.increment();
            } else if (entry.getKind() == SVNNodeKind.DIR) {
              stack.push(child);
            }
          }
        }
        timer.logTotalTimeAndCount(2, "files downloaded");
        return true;
      } else {
        logger.log(Level.SEVERE, "Unexpected node kind: " + nodeKind + " for " + url);
        FileUtils.delete(target);
        return false;
      }
    } catch (SVNException e) {
      logger.log(Level.SEVERE, "Error downloading " + url, e);
      FileUtils.delete(target);
      return false;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing download from " + url, e);
      FileUtils.delete(target);
      return false;
    } finally {
      if (repo != null) {
        repo.closeSession();
      }
    }
  }
  
  public static boolean checkout(String url, File target) {
    DAVRepositoryFactory.setup();
    
    SVNClientManager manager = SVNClientManager.newInstance();
    try {
      SVNUpdateClient client = manager.getUpdateClient();
      SVNURL svnURL = SVNURL.parseURIDecoded(url);
      client.doCheckout(svnURL, target, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
      return true;
    } catch (SVNException e) {
      logger.log(Level.SEVERE, "Error checking out project.", e);
    } finally {
      manager.dispose();
    }
    return false;
  }
}
