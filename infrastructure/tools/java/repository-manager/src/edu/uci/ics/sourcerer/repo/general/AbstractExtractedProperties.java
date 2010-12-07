package edu.uci.ics.sourcerer.repo.general;

import java.io.File;

public abstract class AbstractExtractedProperties extends AbstractProperties {
  protected static final String EXTRACTED = "extracted";
  protected static final String MISSING_TYPES = "missingTypes";
  protected static final String FROM_SOURCE = "fromSource";
  protected static final String SOURCE_EXCEPTIONS = "sourceExceptions";
  protected static final String FIRST_ORDER_JARS = "firstOrderJars";
  protected static final String JARS = "jars";
  
  //Extraction properties
  protected boolean extracted;

  protected boolean missingTypes;
  protected int fromSource;
  protected int sourceExceptions;
  protected int firstOrderJars;
  protected int jars;
  
  protected void load(File file) {
    super.load(file);
      
    extracted = readBooleanProperty(EXTRACTED);
    missingTypes = readBooleanProperty(MISSING_TYPES);
    fromSource = readIntProperty(FROM_SOURCE);
    sourceExceptions = readIntProperty(SOURCE_EXCEPTIONS);
    firstOrderJars = readIntProperty(FIRST_ORDER_JARS);
    jars = readIntProperty(JARS);
  }
  
  public void save(File file) {
    set(EXTRACTED, extracted);
    set(MISSING_TYPES, missingTypes);
    set(FROM_SOURCE, fromSource);
    set(SOURCE_EXCEPTIONS, sourceExceptions);
    set(FIRST_ORDER_JARS, firstOrderJars);
    set(JARS, jars);
    
    super.save(file);
  }
  
  public void setExtracted(boolean extracted) {
    this.extracted = extracted;
  }

  public void setMissingTypes(boolean missingTypes) {
    this.missingTypes = missingTypes;
  }

  public void setFromSource(int fromSource) {
    this.fromSource = fromSource;
  }

  public void setSourceExceptions(int sourceExceptions) {
    this.sourceExceptions = sourceExceptions;
  }
  
  public void setFirstOrderJars(int firstOrderJars) {
    this.firstOrderJars = firstOrderJars;
  }
  
  public void setJars(int jars) {
    this.jars = jars;
  }
  
  public boolean extracted() {
    return extracted;
  }
  
  public boolean missingTypes() {
    return missingTypes;
  }
  
  public int getFirstOrderJars() {
    return firstOrderJars;
  }
  
  public int getJars() {
    return jars;
  }
  
  public void verifyExtracted() {
    if (!extracted) {
      throw new IllegalStateException("This item has not been extracted yet.");
    }
  }
  
  public int getExtractedFromSource() {
    verifyExtracted();
    return fromSource;
  }
  
  public int getSourceExceptions() {
    verifyExtracted();
    return sourceExceptions;
  }
}
