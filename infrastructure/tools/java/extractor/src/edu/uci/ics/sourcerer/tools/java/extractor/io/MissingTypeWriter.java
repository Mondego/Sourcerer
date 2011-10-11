package edu.uci.ics.sourcerer.tools.java.extractor.io;

import edu.uci.ics.sourcerer.tools.java.model.extracted.MissingTypeEX;

public interface MissingTypeWriter extends ExtractorWriter {
  public void writeMissingType(MissingTypeEX type);
}
