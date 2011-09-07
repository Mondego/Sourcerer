//package edu.uci.ics.sourcerer.repo.general;
//
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//
//import java.io.File;
//import java.util.Map;
//import java.util.Scanner;
//import java.util.logging.Level;
//
//import edu.uci.ics.sourcerer.util.Helper;
//import edu.uci.ics.sourcerer.util.io.internal.LWRec;
//
//public class RepoFile implements LWRec {
//  private final RepoFile root;
//  private final String relativePath;
//  private final File file;
//
//  protected RepoFile(File file) {
//    root = this;
//    relativePath = "";
//    this.file = file;
//  }
//  
//  protected RepoFile(RepoFile root, String relativePath) {
//    this.root = root;
//    if (relativePath.charAt(0) == '/') {
//      this.relativePath = relativePath.substring(1);
//    } else {
//      this.relativePath = relativePath;
//    }
//    this.file = new File(root.file, relativePath.replace('*', ' '));
//  }
//  
//  private static Map<File, RepoFile> rootMap = Helper.newHashMap();
//  public static RepoFile make(File root) {
//    RepoFile file = rootMap.get(root);
//    if (file == null) {
//      file = new RepoFile(root);
//      rootMap.put(root, file);
//    }
//    return file;
//  }
//
//  public RepoFile makeRoot() {
//    return make(file);
//  }
//  
//  public boolean isDirectory() {
//    return file.isDirectory();
//  }
//  
//  public boolean exists() {
//    if (file.exists()) {
//      if (file.isDirectory()) {
//        // make sure it's not empty
//        if (file.list().length == 0) {
//          file.delete();
//          return false;
//        } else {
//          return true;
//        }
//      } else {
//        return true;
//      }
//    } else {
//      return false;
//    }
//  }
//  
//  /**
//   * Creates the parent directories, if needed.
//   */
//  public File toFile() {
//    if (!file.exists()) {
//      File parent = file.getParentFile();
//      if (!parent.exists()) {
//        parent.mkdirs();
//      }
//    }
//  
//    return file;
//  }
//  
//  /**
//   * Creates the directory, if needed.
//   */
//  public File toDir() {
//    if (!file.exists()) {
//      file.mkdirs();
//    }
//    
//    return file;
//  }
//    
//  public File getChildFile(String child) {
//    if (file.isFile()) {
//      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + child);
//    } else {
//      return new File(toDir(), child);
//    }
//  }
//  
//  public RepoFile getChild(String child) {
//    if (file.isFile()) {
//      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + child);
//    } else {
//      if (relativePath.equals("")) {
//        if (child.charAt(0) == '/') {
//          return new RepoFile(root, child.substring(1));
//        } else {
//          return new RepoFile(root, child);
//        }
//      } else {
//        if (child.charAt(0) == '/') {
//          return new RepoFile(root, relativePath + child);
//        } else {
//          return new RepoFile(root, relativePath + "/" + child);
//        }
//      }
//    }
//  }
//  
//  public RepoFile rebaseFile(RepoFile toRebase) {
//    if (root.file.equals(toRebase.root.file)) {
//      return toRebase;
//    } else {
//      return new RepoFile(root, toRebase.relativePath);
//    }
//  }
//  
//  public String getRelativePath() {
//    return relativePath;
//  }
//  
//  public String getName() {
//    return file.getName();
//  }
//  
//  @Override
//  public String toString() {
//    return relativePath;
//  }
//  
//  public static RepoFile makeFromScanner(Scanner scanner) {
//    String value = scanner.next();
//    int colon = value.indexOf(';');
//    if (colon == -1) {
//      logger.log(Level.SEVERE, "Invalid value: " + value);
//      return null;
//    } else {
//      File rootFile = new File(value.substring(0, colon).replace('*', ' '));
//      RepoFile root = make(rootFile);
//      String relativePath = value.substring(colon + 1);
//      return new RepoFile(root, relativePath);
//    }
//  }
//  
//  @Override
//  public String writeToString() {
//    return root.toFile().getPath().replace(' ', '*') + ";" + relativePath;
//  }
//}
