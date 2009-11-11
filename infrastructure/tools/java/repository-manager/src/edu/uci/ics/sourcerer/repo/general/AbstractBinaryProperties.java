package edu.uci.ics.sourcerer.repo.general;

import java.io.File;

public abstract class AbstractBinaryProperties extends AbstractProperties {
  protected static final String FROM_BINARY = "fromBinary";
  protected static final String BINARY_EXCEPTIONS = "binaryExceptions";
  
  // Extraction properties
  protected int fromBinary;
  protected int binaryExceptions;
  
  protected void loadProperties(File file) {
    super.loadProperties(file);
    
    fromBinary = readIntProperty(FROM_BINARY);
    binaryExceptions = readIntProperty(BINARY_EXCEPTIONS);
  }
  
  public int getExtractedFromBinary() {
    verifyExtracted();
    return fromBinary;
  }
  
  public int getBinaryExceptions() {
    verifyExtracted();
    return binaryExceptions;
  }
}
