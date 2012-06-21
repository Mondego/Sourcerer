package edu.uci.ics.sourcerer.util;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.Closeable;
import java.util.Timer;
import java.util.TimerTask;

import edu.uci.ics.sourcerer.util.io.IOUtils;

public class TimeoutManager <T extends Closeable> {
  private Instantiator<T> instantiator;
  private final int TIMEOUT;
  private transient long lastTimeAccessed;
  private transient T instance;
  private transient TimerTask task;
  
  public TimeoutManager(Instantiator<T> instantiator, int timeout) {
    this.instantiator = instantiator;
    this.TIMEOUT = timeout;
    this.instance = null;
  }
  
  public synchronized T get() {
    lastTimeAccessed = System.currentTimeMillis();
    if (instance == null) {
      instance = instantiator.create();
      if (instance != null) {
        Timer timer = new Timer();
        task = new TimerTask() {
          @Override
          public void run() {
            synchronized (TimeoutManager.this) {
              if (System.currentTimeMillis() - lastTimeAccessed > TIMEOUT) {
                logger.info("Timeout manager closing...");
                IOUtils.close(instance);
                instance = null;
                task = null;
                this.cancel();
              }
            }
          }
        };
        timer.schedule(task, TIMEOUT, TIMEOUT);
      }
    }
    return instance;
  }
  
  public synchronized void destroy() {
    if (task != null) {
      task.cancel();
      task.run();
    }
  }
 
  public static interface Instantiator <T extends Closeable> {
    public T create();
  }
}
