package edu.uci.ics.sourcerer.util.io;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;

public abstract class Property <T> {
  public static final Property<Boolean> PROMPT_MISSING = new BooleanProperty("prompt-missing", false, "General", "Prompt for missing properties.");
  
  protected String name;
  protected boolean initialized;
  protected T value;
  protected T defaultValue;
  
  protected String category;
  protected String description;
  
  protected boolean optional = false;

  protected Property(String name, String category, String description) {
    this.name = name;
    this.category = category;
    this.description = description;
  }
  
  protected Property(String name, T defaultValue, String category, String description) {
    this.name = name;
    this.defaultValue = defaultValue;
    this.category = category;
    this.description = description;
  }
  
  public Property<T> makeOptional() {
    optional = true;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public abstract String getType();
  
  public String getDescriptionWithDefault() {
    if (defaultValue == null) {
      return description;
    } else {
      return description + " Defaults to " + defaultValue + ".";
    }
  }
  
  public String toString() {
    return name;
  }
  
  public boolean isNotOptional() {
    return !optional;
  }
  
  public boolean hasValue() {
    return value != null || defaultValue != null;
  }
  
  public void setValue(T value) {
    this.value = value;
  }
  
  public synchronized T getValue() {
    if (!initialized) {
      initializeValue();
    }
    if (value == null) {
      PropertyManager.verifyUsage();
      throw new IllegalStateException(name + " never specified.");
    } else {
      return value;
    }
  }
  
  protected abstract T parseString(String value);
  
  private synchronized void initializeValue() {
    if (!initialized) {
      PropertyManager properties = PropertyManager.getProperties();
      String stringValue = properties.getValue(name);
      if (stringValue == null) {
        if (this != PROMPT_MISSING && PROMPT_MISSING.getValue()) {
          try {
            System.out.print("Please enter value for " + name + ":");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            stringValue = br.readLine();
            value = parseString(stringValue);
          } catch (IOException e) {
            value = defaultValue;
            logger.log(Level.SEVERE, "Unable to read value for " + name, e);
          }
        } else {
          value = defaultValue;
        }
      } else {
        value = parseString(stringValue);
      }
      initialized = true;
    }
  }
}
