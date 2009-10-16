package edu.uci.ics.sourcerer.util.io.properties;

import java.io.File;

import edu.uci.ics.sourcerer.util.io.Property;

public class FileProperty extends Property<File> {
  public FileProperty(String name, String category, String description) {
    super(name, category, description);
  }
  
  public FileProperty(String name, File defaultValue, String category, String description) {
    super(name, defaultValue, category, description);
  }
  
  @Override
  public String getType() {
    return "path";
  }
  
  @Override
  protected File parseString(String value) {
    return new File(value);
  }
}
