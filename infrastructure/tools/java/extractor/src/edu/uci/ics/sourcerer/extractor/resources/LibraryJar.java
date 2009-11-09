package edu.uci.ics.sourcerer.extractor.resources;

import java.io.File;

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
  
  public String getName() {
    return entry.getPath().lastSegment();
  }
  
  public String toString() {
    return getName();
  }
  
  public ExtractedLibrary getExtractedLibrary(ExtractedRepository repo) {
    return new ExtractedLibrary(new File(repo.getLibsDir(), entry.getPath().lastSegment()));
  }
}
