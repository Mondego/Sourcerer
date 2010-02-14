package edu.uci.ics.sourcerer.repo.general;

import java.io.File;

public class RepoPath {
  private File content;
  private String relativePath;
  
  private RepoPath(File content, String relativePath) {
    this.content = content;
    this.relativePath = relativePath;
  }
  
  public static RepoPath getNewPath(File content, String relativePath) {
    return new RepoPath(content, relativePath);
  }
  
  public static RepoPath getNewPath(String basePath, String relativePath) {
    return new RepoPath(new File(basePath, relativePath), relativePath);
  }
  
  public RepoPath getNewPath(String newBase) {
    return getNewPath(newBase, relativePath);
  }

  public File toFile() {
    return content;
  }
  
  public File getChildFile(String child) {
    return new File(content, child);
  }
  
  public RepoPath getChild(String child) {
    return new RepoPath(new File(content, child), relativePath + "/" + child);
  }
  
  public String getRelativePath() {
    return relativePath;
  }
}
