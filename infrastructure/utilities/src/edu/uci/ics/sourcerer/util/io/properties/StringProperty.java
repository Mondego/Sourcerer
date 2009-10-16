package edu.uci.ics.sourcerer.util.io.properties;

import edu.uci.ics.sourcerer.util.io.Property;

public class StringProperty extends Property<String> {
  public StringProperty(String name, String category, String description) {
    super(name, category, description);
  }
  
  public StringProperty(String name, String defaultValue, String category, String description) {
    super(name, defaultValue, category, description);
  }
  
  @Override
  public String getType() {
    return "string";
  }
  
  @Override
  protected String parseString(String value) {
    return value;
  }
}
