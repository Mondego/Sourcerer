package edu.uci.ics.sourcerer.util.io;

public abstract class Property <T> {
  protected String name;
  protected PropertyValue<T> defaultValue;
  
  public boolean isStringProperty() {
    return false;
  }
  
  public boolean isBooleanProperty() {
    return false;
  }
  
  public boolean hasDefaultValue() {
    return defaultValue != null;
  }
  
  public PropertyValue<T> getDefaultValue() {
    return defaultValue;
  }
  
  public String getName() {
    return name;
  }
  
  public String toString() {
    return name;
  }
  
  public abstract PropertyValue<T> parseValue(String value);
}
