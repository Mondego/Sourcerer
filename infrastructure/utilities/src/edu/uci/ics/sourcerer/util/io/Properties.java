package edu.uci.ics.sourcerer.util.io;

import java.io.File;

import edu.uci.ics.sourcerer.util.io.properties.FileProperty;

public final class Properties {
  private Properties() {}
  
  public static final Property<File> OUTPUT = new FileProperty("output", "General", "General purpose output directory.");
  public static final Property<File> INPUT = new FileProperty("input", "General", "General purpose input file/directory.");
}
