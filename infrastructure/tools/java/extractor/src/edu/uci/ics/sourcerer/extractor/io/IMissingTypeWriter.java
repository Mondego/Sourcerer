package edu.uci.ics.sourcerer.extractor.io;

public interface IMissingTypeWriter extends IExtractorWriter {
  public void writeMissingType(String fqn);
}
