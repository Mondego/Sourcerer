package edu.uci.ics.sourcerer.extractor.resources;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.eclipse.core.runtime.NullProgressMonitor;

public class ProgressMonitor extends NullProgressMonitor {
  private long delay;
  private Timer timer;
  
  public ProgressMonitor(long delay) {
    this.delay = delay;
    this.timer = new Timer();
  }
  
  public void beginTask(final String name, int totalWork) {
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        logger.log(Level.SEVERE, "Canceling task");
        setCanceled(true);
      }
    };
    timer.schedule(task, delay);
  }
  
  public void done() {
    timer.cancel();
  }
}
