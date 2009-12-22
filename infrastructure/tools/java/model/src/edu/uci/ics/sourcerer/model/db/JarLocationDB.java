package edu.uci.ics.sourcerer.model.db;

public class JarLocationDB {
  private String jarID;
  private String hash;
  private String path;
  private int offset;
  private int length;
  
  public JarLocationDB(String jarID, String hash, String path, int offset, int length) {
    this.jarID = jarID;
    this.hash = hash;
    this.path = path;
    this.offset = offset;
    this.length = length;
  }

  public String getJarID() {
    return jarID;
  }

  public String getHash() {
    return hash;
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
