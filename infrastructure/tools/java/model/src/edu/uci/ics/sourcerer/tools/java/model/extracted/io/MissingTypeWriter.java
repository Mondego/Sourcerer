package edu.uci.ics.sourcerer.tools.java.model.extracted.io;

import edu.uci.ics.sourcerer.tools.java.model.extracted.MissingTypeEX;

public interface MissingTypeWriter extends ExtractorWriter {
  public void writeMissingType(String fqn);
  public void writeMissingType(MissingTypeEX type);
}
