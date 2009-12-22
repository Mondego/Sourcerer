package edu.uci.ics.sourcerer.model.db;


public class JarDB {
  private String jarID;
  private String hash;
  private String name;
  private String group;
  private String version;
  private boolean hasSource;
  
  public JarDB(String jarID, String hash, String name, String group, String version, boolean hasSource) {
    this.jarID = jarID;
    this.hash = hash;
    this.name = name;
    this.group = group;
    this.version = version;
    this.hasSource = hasSource;
  }

  public String getJarID() {
    return jarID;
  }

  public String getHash() {
    return hash;
  }

  public String getName() {
    return name;
  }

  public String getGroup() {
    return group;
  }

  public String getVersion() {
    return version;
  }

  public boolean hasSource() {
    return hasSource;
  }
}
