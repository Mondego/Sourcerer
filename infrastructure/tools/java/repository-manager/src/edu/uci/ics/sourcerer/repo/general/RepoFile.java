package edu.uci.ics.sourcerer.repo.general;

import java.io.File;

public class RepoFile {
  private final RepoFile root;
  private final String relativePath;
  private final File file;
  
  protected RepoFile(File file) {
    root = this;
    relativePath = "";
    this.file = file;
  }
  
  protected RepoFile(RepoFile root, String relativePath) {
    this.root = root;
    this.relativePath = relativePath;
    this.file = new File(root.file, relativePath);
  }
  
  public static RepoFile make(File root) {
    return new RepoFile(root);
  }
//
//  public RepoFile makeRoot() {
//    return make(file);
//  }
  
  public boolean isDirectory() {
    return file.isDirectory();
  }
  
  public boolean exists() {
    if (file.exists()) {
      if (file.isDirectory()) {
        // make sure it's not empty
        if (file.list().length == 0) {
          file.delete();
          return false;
        } else {
          return true;
        }
      } else {
        return true;
      }
    } else {
      return false;
    }
  }
  
  public File toFile() {
    if (!file.exists()) {
      if (file.isFile()) {
        File parent = file.getParentFile();
        if (!parent.exists()) {
          parent.mkdirs();
        }
      } else {
        file.mkdirs();
      }
    }
  
    return file;
  }
  
  public File getChildFile(String child) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + child);
    } else {
      return new File(toFile(), child);
    }
  }
  
  public RepoFile getChild(String child) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + child);
    } else {
      if (child.charAt(0) == '/') {
        return new RepoFile(root, relativePath + child);
      } else {
        return new RepoFile(root, relativePath + "/" + child);
      }
    }
  }
  
  public RepoFile rebaseFile(RepoFile toRebase) {
    if (root.file.equals(toRebase.root.file)) {
      return toRebase;
    } else {
      return new RepoFile(root, toRebase.relativePath);
    }
  }
  
  public String getRelativePath() {
    return relativePath;
  }
  
  public String getName() {
    return file.getName();
  }
  
  @Override
  public String toString() {
    return relativePath;
  }
}
