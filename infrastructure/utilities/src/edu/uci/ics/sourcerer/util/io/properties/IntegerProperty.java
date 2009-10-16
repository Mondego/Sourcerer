package edu.uci.ics.sourcerer.util.io.properties;

import edu.uci.ics.sourcerer.util.io.Property;

public class IntegerProperty extends Property<Integer> {
  public IntegerProperty(String name, Integer defaultValue, String category, String description) {
    super(name, defaultValue, category, description);
  }

  @Override
  public String getType() {
    return "int";
  }

  @Override
  protected Integer parseString(String value) {
    return Integer.parseInt(value);
  }
}
