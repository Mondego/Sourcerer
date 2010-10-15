package edu.uci.ics.sourcerer.repo.properties;

import java.io.File;

public abstract class AbstractBinaryProperties extends AbstractExtractedProperties {
  protected static final String SOURCE_SKIPPED = "sourceSkipped";
  protected static final String FROM_BINARY = "fromBinary";
  protected static final String BINARY_EXCEPTIONS = "binaryExceptions";
  
  // Extraction properties
  protected boolean sourceSkipped;
  protected int fromBinary;
  protected int binaryExceptions;
  
  protected void loadProperties(File file) {
    super.loadProperties(file);
  
    sourceSkipped = readBooleanProperty(SOURCE_SKIPPED);
    fromBinary = readIntProperty(FROM_BINARY);
    binaryExceptions = readIntProperty(BINARY_EXCEPTIONS);
  }
  
  @Override
  public void save(File file) {
    set(SOURCE_SKIPPED, sourceSkipped);
    set(FROM_BINARY, fromBinary);
    set(BINARY_EXCEPTIONS, binaryExceptions);
    
    super.save(file);
  }
  
  public void setSourceSkipped(boolean sourceSkipped) {
    this.sourceSkipped = sourceSkipped;
  }
  
  public void setFromBinary(int fromBinary) {
    this.fromBinary = fromBinary;
  }

  public void setBinaryExceptions(int binaryExceptions) {
    this.binaryExceptions = binaryExceptions;
  }
  
  public boolean sourceSkipped() {
    return sourceSkipped;
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
