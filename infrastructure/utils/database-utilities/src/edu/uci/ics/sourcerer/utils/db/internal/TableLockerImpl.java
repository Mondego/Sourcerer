package edu.uci.ics.sourcerer.utils.db.internal;

import edu.uci.ics.sourcerer.utils.db.TableLocker;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

class TableLockerImpl implements TableLocker {
  private boolean locked;
  private QueryExecutorImpl executor;
  private StringBuilder tables;
  
  TableLockerImpl(QueryExecutorImpl executor) {
    this.executor = executor;
    reset();
  }
  
  private void reset() {
    locked = false;
    tables = new StringBuilder("LOCK TABLES ");
  }
  
  public void addWrite(DatabaseTable table) {
    if (locked) {
      throw new IllegalStateException("Cannot add when already locked");
    } else {
      tables.append(table.getName()).append(" WRITE,");
    }
  }
  
  public void addWrites(DatabaseTable ... tables) {
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
  
  public void addReads(DatabaseTable ... tables) {
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
