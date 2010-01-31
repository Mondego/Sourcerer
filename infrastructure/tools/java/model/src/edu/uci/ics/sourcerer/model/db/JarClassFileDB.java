package edu.uci.ics.sourcerer.model.db;

public class JarClassFileDB {
  private String jarID;
  private String hash;
  private String path;
  
  public JarClassFileDB(String jarID, String hash, String path) {
    this.jarID = jarID;
    this.hash = hash;
    this.path = path;
  }

  public String getJarID() {
    return jarID;
  }

  public String getHash() {
    return hash;
  }

  public String getPath() {
    return path;
  }}
