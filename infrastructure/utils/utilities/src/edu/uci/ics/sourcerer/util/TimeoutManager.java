package edu.uci.ics.sourcerer.util;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;
import java.io.Closeable;
import java.util.Timer;
import java.util.TimerTask;

import edu.uci.ics.sourcerer.util.io.FileUtils;

public class TimeoutManager <T extends Closeable> {
  private Instantiator<T> instantiator;
  private final int TIMEOUT;
  private transient long lastTimeAccessed;
  private transient T instance;
  
  public TimeoutManager(Instantiator<T> instantiator, int timeout) {
    this.instantiator = instantiator;
    this.TIMEOUT = timeout;
    this.instance = null;
  }
  
  public synchronized T get() {
    lastTimeAccessed = System.currentTimeMillis();
    if (instance == null) {
      instance = instantiator.create();
      Timer timer = new Timer();
      TimerTask task = new TimerTask() {
        @Override
        public void run() {
          synchronized (TimeoutManager.this) {
            if (System.currentTimeMillis() - lastTimeAccessed > TIMEOUT) {
              logger.info("Timeout manager closing...");
              FileUtils.close(instance);
              instance = null;
              this.cancel();
            }
          }
        }
      };
      timer.schedule(task, TIMEOUT, TIMEOUT);
    }
    return instance;
  }
 
  public static interface Instantiator <T extends Closeable> {
    public T create();
  }
}
