package edu.uci.ics.sourcerer.eval;

import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

public class Main {
  public static void main(String[] args) {
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
    CalculateTopStats.calculate();
  }
}
