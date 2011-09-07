package edu.uci.ics.sourcerer.tools.java.extractor.eclipse;

import org.eclipse.jdt.core.IClasspathEntry;

public class LibraryJar {
  private IClasspathEntry entry;
  
  protected LibraryJar(IClasspathEntry entry) {
    this.entry = entry;
  }
  
  protected IClasspathEntry getClasspathEntry() {
    return entry;
  }
  
  public String getPath() {
    return entry.getPath().toString();
  }
  
  public String getSourcePath() {
    if (entry.getSourceAttachmentPath() == null) {
      return null;
    } else {
      return entry.getSourceAttachmentPath().toString();
    }
  }
  
  public String getName() {
    return entry.getPath().lastSegment();
  }
  
  public String toString() {
    return getName();
  }
  
//  public ExtractedLibrary getExtractedLibrary(ExtractedRepository repo) {
//   return repo.getExtractedLibrary(entry.getPath().lastSegment());
//  }
}
