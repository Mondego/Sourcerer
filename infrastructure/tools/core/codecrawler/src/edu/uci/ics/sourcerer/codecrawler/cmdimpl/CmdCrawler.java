/*
 * Sourcerer: An infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package edu.uci.ics.sourcerer.codecrawler.cmdimpl;

import java.util.Date;

import edu.uci.ics.sourcerer.codecrawler.crawler.Crawler;
import edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener;
import edu.uci.ics.sourcerer.codecrawler.db.GatewayFactoryLoader;
import edu.uci.ics.sourcerer.codecrawler.db.IGatewayFactory;
import edu.uci.ics.sourcerer.codecrawler.util.CrawlerProperties;
import edu.uci.ics.sourcerer.codecrawler.util.CrawlerPropertiesException;
import edu.uci.ics.sourcerer.common.LoggerUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 *
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 15, 2009
 *
 */
public class CmdCrawler implements Runnable {
	
	private static final String BUILT_ON = "(Built on 2009/09/11 12:24 AM)";

	private class CrawlerListener implements ICrawlerEventListener {
		
		private String name;
		private int linksAdded;
		private int hitsAdded;
		
		public CrawlerListener(String name) {
			this.name = name;
		}
		
		public void onConnectionError(String url) {
			if (debugEnabled)
				LoggerUtils.debug("[" + name + "][INFO] Failed to open URL: " + url);
			println("[" + name + "][INFO] Failed to open URL: " + url);
		}
	
		public void onCrawlingComplete() {
			decreaseRunning();
			if (debugEnabled)
				LoggerUtils.debug("[" + name + "][INFO] Crawler has stopped.");	
			println("[" + name + "][INFO] Crawler has stopped.");			
		}
	
		public void onCrawlingPause() {
			if (debugEnabled)
				LoggerUtils.debug("[" + name + "][INFO] Crawler has paused.");	
			println("[" + name + "][INFO] Crawler has paused.");
		}
	
		public void onCrawlingResume() {
			if (debugEnabled)
				LoggerUtils.debug("[" + name + "][INFO] Crawler has resumed.");	
			println("[" + name + "][INFO] Crawler has resumed.");
		}
	
		public void onCrawlingStart() {
			if (debugEnabled)
				LoggerUtils.debug("[" + name + "][INFO] Crawler has started.");
			println("[" + name + "][INFO] Crawler has started.");
		}
	
		public void onDoneProcessingUrl(String url) {
			if (debugEnabled)
				LoggerUtils.debug(String.format("[%s][INFO] Links added:%d - HITs added:%d", name, linksAdded, hitsAdded));
			println(String.format("[%s][INFO] Links added:%d - HITs added:%d", name, linksAdded, hitsAdded));
		}
	
		public void onError(String errorMsg) {
			if (debugEnabled)
				LoggerUtils.error("[" + name + "]" + errorMsg);
			println("[" + name + "][ERROR] " + errorMsg);
		}
		
		public void onException(String errorMsg) {
			if (debugEnabled)
				LoggerUtils.error("[" + name + "][ERROR] " + errorMsg);
			println("[" + name + "][ERROR] " + errorMsg);
		}
	
		public void onHitAdded(String hitDownloadString) {
			if (debugEnabled)
				LoggerUtils.debug("[" + name + "] ****** Added HIT: " + hitDownloadString);
			println("[" + name + "][INFO] ****** HIT found: " + hitDownloadString);
			hitsAdded++;
		}
	
		public void onInfo(String infoMsg) {
			if (debugEnabled)
				LoggerUtils.debug("[" + name + "][INFO] " + infoMsg);
			println("[" + name + "][INFO] " + infoMsg);
		}
	
		public void onLinkAdded(String url) {
			linksAdded++;
		}
	
		public void onParseError(String url, String errorMsg) {
			if (debugEnabled)
				LoggerUtils.debug("[" + name + "][INFO] " + errorMsg);
			println("[" + name + "][INFO] " + errorMsg);
		}
	
		public void onProcessingNewUrl(String url) {
			if (debugEnabled)
				LoggerUtils.debug("[" + name + "] Processing " + url);
			println("[" + name + "][INFO] Processing " + url);
			linksAdded = 0;
			hitsAdded = 0;
		}
	
		public void onRefusingVisitedLink(String url) {
		}
	
		public void onSkippingNonText(String url) {
			if (debugEnabled)
				LoggerUtils.debug("[" + name + "][INFO] Skipping non-text document: " + url);
			println("[" + name + "][INFO] Skipping non-text document: " + url);
		}
	
		public void onSkippingVisitedContent(String url) {
			if (debugEnabled)
				LoggerUtils.debug("[" + name + "][INFO] Skipping visited content: " + url);
			println("[" + name + "][INFO] Skipping visited content: " + url);
		}
	
		public void onWarn(String warnMsg) {
			if (debugEnabled)
				LoggerUtils.warn("[" + name + "]" + warnMsg);
			println("[" + name + "][WARN] " + warnMsg);
		}
	}

	//**************************************************
	
	private static final boolean debugEnabled = LoggerUtils.isDebugEnabled(CmdCrawler.class);

	public static final int DEFAULT_CRAWLERS = 2;
	
	private Crawler[] crawlers;
	private boolean failed = false;
	private Integer running;

	public CmdCrawler(String propertiesFile, int crawlerCount) {
		try {
			CrawlerProperties.loadPropertiesFile(propertiesFile);
		} catch (CrawlerPropertiesException e) {
			println("[ERROR] " + e.getMessage());
			failed = true;
			return;
		}
		
		IGatewayFactory gatewayFactory;
		try {
			gatewayFactory = GatewayFactoryLoader.loadGatewayFactory(CrawlerProperties.getInstance());
		} catch (RuntimeException e) {
			println("[ERROR] " + e.getMessage());
			failed = true;
			return;
		}
		
		crawlers = new Crawler[crawlerCount];
		for (int i = 0; i < crawlers.length; i++)
		{
			crawlers[i] = new Crawler(gatewayFactory,
					new CrawlerListener(String.format("Crawler%2d", i)),
					CrawlerProperties.getInstance());
		}
		
		for (int i = 0; i < crawlers.length; i++)
			for (int j = 0; j < crawlers.length; j++)
				if (i != j) {
					crawlers[i].addInfoExchanger(crawlers[j]);
				}
	}
	
	public void run() {
		if (failed){
			printHelp();
			return;
		}
		
		if (debugEnabled)
			LoggerUtils.debug("\n##############################################\n" + 
					new Date().toString() +
					"\n##############################################");
		
		running = crawlers.length;
		for (Crawler crawler : crawlers)
			new Thread(crawler).start();
		
		while (getRunning() > 0) {
			try {
				if (getRunning() < crawlers.length) {
					if (debugEnabled)
						LoggerUtils.debug("[INFO] Restarting inactive crawlers...");
					println("[INFO] Restarting inactive crawlers...");
					for (Crawler crawler : crawlers) {
						if (crawler.isStopped()) {
							new Thread(crawler).start();
							increaseRunning();
						}
					}
				}
				Thread.sleep(3000);
			} catch (InterruptedException e) {}
		}
		
		println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		println("!!!!! CRAWLING COMPLETED !!!!!");
		println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}
	
	private synchronized void println(String msg) {
		System.out.println(msg);
	}
	
	protected int decreaseRunning() {
		synchronized(running) {
			return (--running);
		}
	}
	
	protected int getRunning() {
		synchronized(running) {
			return running;
		}
	}
	
	protected int increaseRunning() {
		synchronized(running) {
			return (++running);
		}
	}
	
	public static void main(String[] args) {
		String propertiesFile = CrawlerProperties.DEFAULT_FILE_NAME;
		int crawlers = DEFAULT_CRAWLERS;
		
		boolean argHelp = false;
		
		for (String arg : args) {
			if (arg.startsWith("-properties=")) {
				propertiesFile = arg.substring("-properties=".length());
			} else if (arg.startsWith("-crawlers=")) {
				try {
					crawlers = Integer.parseInt(arg.substring("-crawlers=".length()));
				} catch (NumberFormatException e) {
					argHelp = true;
				}
			} else {
				argHelp = true;
			}
		}
		
		if (argHelp) {
			printHelp();
		}
		else {
			System.out.println("SOURCERER Code Crawler " + BUILT_ON);
			CmdCrawler crawler = new CmdCrawler(propertiesFile, crawlers);
			crawler.run();
		}
	}
	
	private static void printHelp() {
		System.out.println(
				
				"Accepted arguments:\n" +
				"  -properties=[properties file]  Set the properties file.\n" +
				"                                 Default is 'crawler.properties'.\n" +
				"  -crawlers=[number]             Set the number of con-current crawlers.\n" +
				"                                 Default value is 2." +
				"\n\n"
				);
	}

}
