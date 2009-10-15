package edu.uci.ics.sourcerer.util.io;

public abstract class PropertyValue <T> {
  public abstract void setValue(T value);
  public abstract T getValue();
  
  protected static class StringValue extends PropertyValue<String> {
    private String value = null;
    
    public void setValue(String value) {
      this.value = value;
    }
    
    public String getValue() {
      return value;
    }
  }
  
  protected static class BooleanValue extends PropertyValue<Boolean> {
    private Boolean value = false;
    
    public void setValue(Boolean value) {
      this.value = value;
    }
    
    public Boolean getValue() {
      return value;
    }
  }
}
