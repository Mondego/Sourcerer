package edu.uci.ics.sourcerer.utils.db.internal;

import edu.uci.ics.sourcerer.utils.db.ITableLocker;
import edu.uci.ics.sourcerer.utils.db.sql.ITable;

class TableLocker implements ITableLocker {
  private boolean locked;
  private QueryExecutor executor;
  private StringBuilder tables;
  
  TableLocker(QueryExecutor executor) {
    this.executor = executor;
    reset();
  }
  
  private void reset() {
    locked = false;
    tables = new StringBuilder("LOCK TABLES ");
  }
  
  public void addWrite(ITable table) {
    if (locked) {
      throw new IllegalStateException("Cannot add when already locked");
    } else {
      tables.append(table.getName()).append(" WRITE,");
    }
  }
  
  public void addWrites(ITable... tables) {
    for (ITable table : tables) {
      addWrite(table);
    }
  }
  
  public void addRead(ITable table) {
    if (locked) {
      throw new IllegalStateException("Cannot add when already locked");
    } else {
      tables.append(table.getName()).append(" READ,");
    }
  }
  
  public void addReads(ITable... tables) {
    for (ITable table : tables) {
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
