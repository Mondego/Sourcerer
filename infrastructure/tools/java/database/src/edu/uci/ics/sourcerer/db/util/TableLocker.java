package edu.uci.ics.sourcerer.db.util;

import edu.uci.ics.sourcerer.db.schema.DatabaseTable;

public class TableLocker {
  private boolean locked;
  private QueryExecutor executor;
  private StringBuffer tables;
  
  protected TableLocker(QueryExecutor executor) {
    this.executor = executor;
    reset();
  }
  
  private void reset() {
    locked = false;
    tables = new StringBuffer("LOCK TABLES ");
  }
  
  public void addWrite(DatabaseTable table) {
    if (locked) {
      throw new IllegalStateException("Cannot add when already locked");
    } else {
      tables.append(table.getName()).append(" WRITE,");
    }
  }
  
  public void addWrites(DatabaseTable... tables) {
    for (DatabaseTable table : tables) {
      addWrite(table);
    }
  }
  
  public void addRead(DatabaseTable table) {
    if (locked) {
      throw new IllegalStateException("Cannot add when already locked");
    } else {
      tables.append(table.getName()).append(" READ,");
    }
  }
  
  public void addReads(DatabaseTable... tables) {
    for (DatabaseTable table : tables) {
      addRead(table);
    }
  }
  
  public void lock() {
    if (locked) {
      throw new IllegalStateException("Cannot lock when already locked");
    } else {
      tables.setCharAt(tables.length() - 1, ';');
      executor.execute(tables.toString());
      locked = true;
    }
  }
  
  public void unlock() {
    if (locked) {
      executor.execute("UNLOCK TABLES;");
      reset();
    } else {
      throw new IllegalStateException("Cannot unlock when not locked");
    }
  }
}
