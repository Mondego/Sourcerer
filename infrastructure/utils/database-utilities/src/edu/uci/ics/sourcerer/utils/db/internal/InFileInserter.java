package edu.uci.ics.sourcerer.utils.db.internal;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

class InFileInserter implements BatchInserter {
  private File tempFile;
  private BufferedWriter writer;
  private QueryExecutorImpl executor;
  private DatabaseTable table;
  
  private InFileInserter() {}
  
  static InFileInserter makeInFileInserter(File tempDir, QueryExecutorImpl executor, DatabaseTable table) {
    File tempFile = new File(tempDir, table.getName());
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
      InFileInserter retval = new InFileInserter();
      retval.tempFile = tempFile;
      retval.writer = writer;
      retval.executor = executor;
      retval.table = table;
      return retval;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write to temp file: " + tempFile.getPath(), e);
      return null;
    }
  }
  
  @Override
  public void addInsert(Insert insert) {
    if (writer == null) {
      throw new IllegalStateException("Inserter already used");
    }
    try {
      writer.write(insert.toString());
      writer.write('\n');
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing to file: " + tempFile.getPath(), e);
    }
  }
  
  @Override
  public void insert() {
    IOUtils.close(writer);
    writer = null;
    executor.execute("LOAD DATA CONCURRENT LOCAL INFILE '" + tempFile.getPath().replace('\\', '/') + "' " +
    		"INTO TABLE " + table.getName() + " " +
				"FIELDS TERMINATED BY ',' " +
				"OPTIONALLY ENCLOSED BY '\\\'' " + 
				"LINES STARTING BY '(' " +
				"TERMINATED BY ')\n'");
    tempFile.delete();
  }
}
