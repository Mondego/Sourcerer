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
    if (!content.exists()) {
      if (content.isFile()) {
        File parent = content.getParentFile();
        if (!parent.exists()) {
          parent.mkdirs();
        }
      } else {
        content.mkdirs();
      }
    }
  
    return content;
  }
  
  public File getChildFile(String child) {
    if (content.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + content.getPath() + " " + child);
    } else {
      return new File(toFile(), child);
    }
  }
  
  public RepoPath getChild(String child) {
    return new RepoPath(new File(content, child), relativePath + "/" + child);
  }
  
  public String getRelativePath() {
    return relativePath;
  }
  
  public String toString() {
    return relativePath;
  }
}
