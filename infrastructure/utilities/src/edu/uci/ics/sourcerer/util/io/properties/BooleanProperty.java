package edu.uci.ics.sourcerer.util.io.properties;

import edu.uci.ics.sourcerer.util.io.Property;

public class BooleanProperty extends Property<Boolean> {
  public BooleanProperty(String name, Boolean defaultValue, String category, String description) {
    super(name, defaultValue, category, description);
  }

  @Override
  public String getType() {
    return "bool";
  }
  
  @Override
  protected Boolean parseString(String value) {
    if ("true".equals(value)) {
      return true;
    } else if ("false".equals(value)) {
      return false;
    } else {
      return null;
    }
  }
}
