package uci.ics.sourcerer.extractor;

import static edu.uci.ics.sourcerer.repo.AbstractRepository.REPO_ROOT;
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
    
    PropertyManager.registerAndVerify(REPO_ROOT, INPUT, OUTPUT);
    runTests();
  }
}
