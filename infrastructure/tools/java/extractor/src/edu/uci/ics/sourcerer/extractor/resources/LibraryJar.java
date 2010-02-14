package edu.uci.ics.sourcerer.extractor.resources;

import org.eclipse.jdt.core.IClasspathEntry;

import edu.uci.ics.sourcerer.repo.extracted.ExtractedLibrary;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;

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
  
  public ExtractedLibrary getExtractedLibrary(ExtractedRepository repo) {
   return new ExtractedLibrary(repo.getLibsPath().getChild(entry.getPath().lastSegment()));
  }
}
