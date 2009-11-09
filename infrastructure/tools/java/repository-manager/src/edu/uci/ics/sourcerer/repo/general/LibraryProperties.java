package edu.uci.ics.sourcerer.repo.general;

import java.io.File;
import java.util.Properties;

public class LibraryProperties extends AbstractProperties {
  private static final String NAME = "name";
  private static final String EXTRACTED = "extracted";
  private static final String HAS_SOURCE = "hasSource";
  private static final String SOURCE_ERROR = "sourceError";
  
  // Base properties
  private String name;
  
  // Extraction properties
  private boolean extracted;
  private boolean hasSource;
  private boolean sourceError;
  
  private LibraryProperties() {}
  
  public static LibraryProperties load(File file) {
    LibraryProperties props = new LibraryProperties();
    props.loadProperties(file);
    
    props.name = props.properties.getProperty(NAME);
    
    props.extracted = "true".equals(props.properties.getProperty(EXTRACTED));
    props.hasSource = "true".equals(props.properties.getProperty(HAS_SOURCE));
    props.sourceError = "true".equals(props.properties.getProperty(SOURCE_ERROR));
    
    return props;
  }
  
  public static void create(File file, String name, boolean hasSource, boolean sourceError) {
    Properties properties = new Properties();
    
    properties.setProperty(NAME, name);
    properties.setProperty(EXTRACTED, "true");
    properties.setProperty(HAS_SOURCE, Boolean.toString(hasSource));
    properties.setProperty(SOURCE_ERROR, Boolean.toString(sourceError));
    
    write(file, properties);
  }

  public String getName() {
    return name;
  }

  public boolean extracted() {
    return extracted;
  }

  public boolean hasSource() {
    return hasSource;
  }

  public boolean sourceError() {
    return sourceError;
  }
  
  
}
