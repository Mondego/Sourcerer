package edu.uci.ics.sourcerer.repo;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.logging.Level;

public class RepoJar {
  private long length;
  private String md5;
  
  public RepoJar(File path) {
    length = path.length();
    md5 = getHash(path);
  }
  
  public RepoJar(long length, String hash) {
    this.length = length;
    this.md5 = hash;
  }
  
  public static String getHash(File path) {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      
      byte[] buff = new byte[1024];
      InputStream is = new FileInputStream(path);
      int size;
      while ((size = is.read(buff)) != -1) {
        digest.update(buff, 0, size);
      }
      return new BigInteger(1, digest.digest()).toString(16);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error getting md5 for " + path.getPath(), e);
      return null;
    }
  }
  
  public String getHash() {
    return md5;
  }
  
  public long getLength() {
    return length;
  }
  
  public boolean equals(Object o) {
    if (o instanceof RepoJar) {
      RepoJar other = (RepoJar) o;
      return length == other.length && md5.equals(other.md5);
    } else {
      return false;
    }
  }
  
  public int hashCode() {
    return md5.hashCode();
  }
}
