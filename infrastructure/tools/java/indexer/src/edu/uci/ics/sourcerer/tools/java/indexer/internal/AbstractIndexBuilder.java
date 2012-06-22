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
package edu.uci.ics.sourcerer.tools.java.indexer.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import edu.uci.ics.sourcerer.tools.java.indexer.Main;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.IntegerArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class AbstractIndexBuilder extends DatabaseRunnable {
  public static final Argument<Integer> INDEX_IMPORT_BATCH_SIZE = new IntegerArgument("index-import-batch-size", 100_000, "Number of imports to batch");

  protected final TaskProgressLogger task;
  private final int batchSize;
  private final SolrServer server;
  private final Collection<SolrInputDocument> docs;
  
  protected AbstractIndexBuilder() {
    task = TaskProgressLogger.get();
    batchSize = INDEX_IMPORT_BATCH_SIZE.getValue();
    docs = new ArrayList<>(batchSize);
    server = new HttpSolrServer(Main.SOLR_URL.getValue());
  }
  
  @Override
  public final void action() {
    task.start("Indexing");
    try {
      index();
      if (!docs.isEmpty()) {
        task.start("Adding " + docs.size() + " docs to the index");
        server.add(docs);
        task.finish();
      }
      task.start("Committing index");
      server.commit();
      task.finish();
      
      task.finish();
    } catch (SolrServerException | IOException e) {
      task.exception(e);
    }
  }
  
  protected abstract void index() throws SolrServerException, IOException;
  
  protected final void add(SolrInputDocument doc) throws SolrServerException, IOException {
    docs.add(doc);
    if (docs.size() >= batchSize) {
      task.start("Adding " + docs.size() + " docs to the index");
      server.add(docs);
      task.finish();
      docs.clear();
    }
  }
}
