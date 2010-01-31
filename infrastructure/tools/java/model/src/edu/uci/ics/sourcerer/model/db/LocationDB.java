package edu.uci.ics.sourcerer.model.db;

public class LocationDB {
  private String path;
  private int offset;
  private int length;
  
  public LocationDB(String path, int offset, int length) {
    this.path = path;
    this.offset = offset;
    this.length = length;
  }

  public String getPath() {
    return path;
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }
}
