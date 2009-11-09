package edu.uci.ics.sourcerer.db.util;

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
  
  public void addWrites(String ... tables) {
    for (String table : tables) {
      addWrite(table);
    }
  }
  
  public void addWrite(String table) {
    if (locked) {
      throw new IllegalStateException("Cannot add when already locked");
    } else {
      tables.append(table).append(" WRITE,");
    }
  }
  
  public void addReads(String ... tables) {
    for (String table : tables) {
      addWrite(table);
    }
  }
  
  public void addRead(String table) {
    if (locked) {
      throw new IllegalStateException("Cannot add when already locked");
    } else {
      tables.append(table).append(" READ,");
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
