package edu.uci.ics.sourcerer.extractor;

import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.OUTPUT_REPO;
import static edu.uci.ics.sourcerer.util.io.Properties.*;

import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

public class Main {
  public static void runTests() {
    ExtractorOutputTest.runTest();
  }
  
  public static void main(String[] args) {
    PropertyManager.initializeProperties();
    Logging.initializeLogger();
    
    PropertyManager.registerAndVerify(INPUT_REPO, OUTPUT_REPO, OUTPUT);
    runTests();
  }
}
