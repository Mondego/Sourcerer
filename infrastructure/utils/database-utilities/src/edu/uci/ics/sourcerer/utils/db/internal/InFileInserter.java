package edu.uci.ics.sourcerer.utils.db.internal;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.utils.db.IRowInsert;
import edu.uci.ics.sourcerer.utils.db.IRowInserter;
import edu.uci.ics.sourcerer.utils.db.sql.ITable;

class InFileInserter implements IRowInserter {
  private File tempFile;
  private BufferedWriter writer;
  private QueryExecutor executor;
  private ITable table;
  
  private InFileInserter() {}
  
  static InFileInserter getInFileInserter(File tempDir, QueryExecutor executor, ITable table) {
    File tempFile = new File(tempDir, table.toString());
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
  public void addRow(IRowInsert insert) {
    try {
      writer.write(insert.toString());
      writer.newLine();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing to file: " + tempFile.getPath(), e);
    }
  }
  
  @Override
  public void insert() {
    FileUtils.close(writer);
    executor.execute("LOAD DATA LOCAL INFILE '" + tempFile.getPath().replace('\\', '/') + "' " +
    		"INTO TABLE " + table + " " +
				"FIELDS TERMINATED BY ',' " +
				"OPTIONALLY ENCLOSED BY '\\\'' " + 
				"LINES STARTING BY '(' " +
				"TERMINATED BY ')\n'");
    tempFile.delete();
  }
}
