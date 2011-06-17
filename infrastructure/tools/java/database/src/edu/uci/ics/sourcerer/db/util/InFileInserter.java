package edu.uci.ics.sourcerer.db.util;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.FileUtils;

public class InFileInserter {
  private File tempFile;
  private BufferedWriter writer;
  private QueryExecutor executor;
  private String table;
  
  private InFileInserter() {}
  
  protected static InFileInserter getInFileInserter(File tempDir, QueryExecutor executor, String table) {
    File tempFile = new File(tempDir, table);
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
  
  public void addValue(String line) {
    try {
      writer.write(line);
      writer.write('\n');
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing to file: " + tempFile.getPath(), e);
    }
  }
  
  public void insert() {
    FileUtils.close(writer);
    executor.execute("LOAD DATA CONCURRENT LOCAL INFILE '" + tempFile.getPath().replace('\\', '/') + "' " +
    		"INTO TABLE " + table + " " +
				"FIELDS TERMINATED BY ',' " +
				"OPTIONALLY ENCLOSED BY '\\\'' " + 
				"LINES STARTING BY '(' " +
				"TERMINATED BY ')\n'");
    tempFile.delete();
  }
}
