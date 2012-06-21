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
package edu.uci.ics.sourcerer.tools.java.index;

import edu.uci.ics.sourcerer.tools.java.index.internal.AbstractIndexBuilder;
import edu.uci.ics.sourcerer.tools.java.index.internal.ClearIndex;
import edu.uci.ics.sourcerer.tools.java.index.internal.CodeGenieIndexBuilder;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Command;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  public static final Argument<String> SOLR_URL = new StringArgument("solr-url", "URL of the solr server");
  
  public static final Command CLEAR_INDEX = new Command("clear-index", "Clears the index") {
    @Override
    protected void action() {
      ClearIndex.clearIndex();
    }
  }.setProperties(SOLR_URL);
  
  public static final Command BUILD_CODEGENIE_INDEX = new Command("build-codegenie-index", "Builds the CodeGenie index") {
    @Override
    protected void action() {
      CodeGenieIndexBuilder.buildIndex();
    }
  }.setProperties(SOLR_URL, AbstractIndexBuilder.INDEX_IMPORT_BATCH_SIZE, DatabaseConnectionFactory.DATABASE_URL, DatabaseConnectionFactory.DATABASE_USER, DatabaseConnectionFactory.DATABASE_PASSWORD);
  
  public static void main(String[] args) {
    Command.execute(args, Main.class);
  }
}
