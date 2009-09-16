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
package edu.uci.ics.sourcerer.codecrawler.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.db.IGatewayFactory;
import edu.uci.ics.sourcerer.codecrawler.db.IHitGateway;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedGateway;
import edu.uci.ics.sourcerer.codecrawler.db.impl.VisitedGateway;
import edu.uci.ics.sourcerer.codecrawler.linkparserplugin.LinkParserPlugin;
import edu.uci.ics.sourcerer.codecrawler.linkparserplugin.LinkParserPluginLoadException;
import edu.uci.ics.sourcerer.codecrawler.linkparserplugin.LinkParserPluginManager;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.parser.Document;
import edu.uci.ics.sourcerer.codecrawler.parser.IDocumentParser;
import edu.uci.ics.sourcerer.codecrawler.parser.ParseErrorException;
import edu.uci.ics.sourcerer.codecrawler.parser.impl.DocumentParser;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.IParserPluginIdGenerator;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPlugin;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPluginLoadException;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPluginManager;
import edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.UrlFilterPlugin;
import edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.UrlFilterPluginLoadException;
import edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.UrlFilterPluginManager;
import edu.uci.ics.sourcerer.codecrawler.urlqueue.IUrlPriorityQueue;
import edu.uci.ics.sourcerer.codecrawler.urlqueue.impl.UrlPriorityQueue;
import edu.uci.ics.sourcerer.codecrawler.util.CrawlerProperties;
import edu.uci.ics.sourcerer.codecrawler.util.CrawlerPropertiesException;
import edu.uci.ics.sourcerer.codecrawler.util.DebugUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 * This Crawler is independent. All information or configuration 
 * that it needs is passed during the construction of this class.
 * It, therefore, can be used from any one, even a plug-in. 
 */
public class Crawler implements Runnable, ICrawlerInfoExchange, IParserPluginIdGenerator {
	
	private class CrawlerEventNotifier implements ICrawlerEventListener {
		
		private ICrawlerEventListener eventListener;
		
		public void setListener(ICrawlerEventListener eventListener) {
			this.eventListener = eventListener; 
		}

		public void onConnectionError(String url) {
			if (eventListener != null)
				eventListener.onConnectionError(url);
		}

		public void onCrawlingComplete() {
			if (eventListener != null)
				eventListener.onCrawlingComplete();
		}

		public void onCrawlingPause() {
			if (eventListener != null)
				eventListener.onCrawlingPause();
		}

		public void onCrawlingResume() {
			if (eventListener != null)
				eventListener.onCrawlingResume();
		}

		public void onCrawlingStart() {
			if (eventListener != null)
				eventListener.onCrawlingStart();
		}

		public void onDoneProcessingUrl(String url) {
			if (eventListener != null)
				eventListener.onDoneProcessingUrl(url);
		}

		public void onError(String errorMsg) {
			if (eventListener != null)
				eventListener.onError(errorMsg);
		}
		
		public void onException(String errorMsg) {
			if (eventListener != null)
				eventListener.onException(errorMsg);
		}

		public void onHitAdded(String hitDownloadString) {
			if (eventListener != null)
				eventListener.onHitAdded(hitDownloadString);
		}

		public void onInfo(String infoMsg) {
			if (eventListener != null)
				eventListener.onInfo(infoMsg);
		}

		public void onLinkAdded(String url) {
			if (eventListener != null)
				eventListener.onLinkAdded(url);
		}

		public void onParseError(String url, String errorMsg) {
			if (eventListener != null)
				eventListener.onParseError(url, errorMsg);
		}

		public void onProcessingNewUrl(String url) {
			if (eventListener != null)
				eventListener.onProcessingNewUrl(url);
		}
		
		public void onRefusingVisitedLink(String url) {
			if (eventListener != null)
				eventListener.onRefusingVisitedLink(url);
		}

		public void onSkippingNonText(String url) {
			if (eventListener != null)
				eventListener.onSkippingNonText(url);
		}

		public void onSkippingVisitedContent(String url) {
			if (eventListener != null)
				eventListener.onSkippingVisitedContent(url);
		}

		public void onWarn(String warnMsg) {
			if (eventListener != null)
				eventListener.onWarn(warnMsg);
		}
		
	}
	
	CrawlerProperties crawlerProperties;
	
	private CrawlerEventNotifier eventNotifier;
	
	private IGatewayFactory gatewayFactory;
	private IHitGateway hitGateway;
	private IVisitedGateway visitedGateway;
	private IUrlPriorityQueue urlPriorityQueue;
	
	private LinkParserPluginManager linkParserPluginManager;
	private UrlFilterPluginManager urlFilterPluginManager;
	private ParserPluginManager parserPluginManager;
	
	private Boolean isPaused = false;
	private Boolean isStopped = true;
	
	private int pauseMilliSecs = 0;
	
	private String currentDomain;
	private String []initUrls;
	
	private HashSet<ICrawlerInfoExchange> infoExchangers = new HashSet<ICrawlerInfoExchange>();
	
	public Crawler(IGatewayFactory gatewayFactory,
			ICrawlerEventListener eventListener,
			CrawlerProperties crawlerProperties) {
		if (gatewayFactory == null)
			throw new NullPointerException("GatewayFactory is null");
		
		this.gatewayFactory = gatewayFactory;
		
		eventNotifier = new CrawlerEventNotifier();
		eventNotifier.setListener(eventListener);
		this.crawlerProperties = crawlerProperties;
	}
	
	private void load() {
		eventNotifier.onInfo("Using GatewayFactory: " + gatewayFactory.getClass().getName());
		
		try {
			pauseMilliSecs = crawlerProperties.getPauseBetweenPages();
			eventNotifier.onInfo("Loaded pausing " + pauseMilliSecs + " between pages.");
		} catch (CrawlerPropertiesException e) {
			pauseMilliSecs = 0;
			eventNotifier.onError(e.getMessage());
		}
		
		initUrls = crawlerProperties.getInitUrls();
		
		hitGateway = gatewayFactory.getHitGateway();
		visitedGateway = new VisitedGateway(gatewayFactory);
		urlPriorityQueue = new UrlPriorityQueue(visitedGateway, gatewayFactory);
		
		linkParserPluginManager = new LinkParserPluginManager();
		urlFilterPluginManager = new UrlFilterPluginManager();
		parserPluginManager = new ParserPluginManager();
		parserPluginManager.setIdGenerator(this);
		
		try {
			linkParserPluginManager.loadPlugins(crawlerProperties.getLinkParserPlugins());
		} catch (LinkParserPluginLoadException e) {
			eventNotifier.onError("Failed to load Link Parser Plug-in(s): " + e.getMessage());
		}

		{
			boolean noPluginLoaded = true;
			for (LinkParserPlugin plugin : linkParserPluginManager.getLoadedPlugins()) {
				noPluginLoaded = false;
				eventNotifier.onInfo("Loaded Link Parser plugin: " + plugin.getClass().getName());
			}
			if (noPluginLoaded)
				eventNotifier.onWarn("No Link Parser Plug-in has been loaded.");
		}
		
		try {
			urlFilterPluginManager.loadPlugins(crawlerProperties.getFilterPlugins());
		} catch (UrlFilterPluginLoadException e) {
			eventNotifier.onError("Failed to load URL Filter Plug-in(s): " + e.getMessage());
		}

		{
			boolean noPluginLoaded = true;
			for (UrlFilterPlugin plugin : urlFilterPluginManager.getLoadedPlugins()) {
				noPluginLoaded = false;
				eventNotifier.onInfo("Loaded URL Filter plugin: " + plugin.getClass().getName());
			}
			if (noPluginLoaded)
				eventNotifier.onWarn("No URL Filter Plug-in has been loaded.");
		}
		
		try {
			parserPluginManager.loadPlugins(crawlerProperties.getParserPlugins());
		} catch (ParserPluginLoadException e) {
			eventNotifier.onError("Failed to load Parser Plug-in(s): " + e.getMessage());
		}
		
		{
			boolean noPluginLoaded = true;
			for (ParserPlugin plugin : parserPluginManager.getLoadedPlugins()) {
				noPluginLoaded = false;
				eventNotifier.onInfo("Loaded Parser plugin: " + plugin.getClass().getName());
			}
			if (noPluginLoaded)
				eventNotifier.onWarn("No Parser Plug-in has been loaded.");
		}

		if (initUrls != null) {
			for (int i = 0; i < initUrls.length; i++) {
				try {
					urlPriorityQueue.addUrl(new UrlString(initUrls[i]));
				} catch (MalformedURLException e) {
				}
			}
		} else
			eventNotifier.onError("No init URLs to start with.");
	}

	public void run() {
		isStopped = false;
		
		eventNotifier.onCrawlingStart();
		
		load();
		currentDomain = "";
	
		while (!isStopped()) {
			UrlString url;
			try {
				if ((url = urlPriorityQueue.getNextUrl()) == null)
					break;		//no more to crawl!
				
				synchronized(currentDomain) {
					currentDomain = url.getDomainName();
				}
	
				if (isPaused()) {
					eventNotifier.onCrawlingPause();
					while (isPaused()) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							//ignored
						}
					}
					eventNotifier.onCrawlingResume();
				}
	
				eventNotifier.onProcessingNewUrl(url.toString());
	
				//check for text document
				try {
					if (url.getContentType().indexOf("text") < 0) {
						eventNotifier.onSkippingNonText(url.toString());
						eventNotifier.onDoneProcessingUrl(url.toString());
						continue;
					}
				} catch (Exception e) {
					eventNotifier.onConnectionError(url.toString());
					eventNotifier.onDoneProcessingUrl(url.toString());
					continue;
				}
	
				//get the document
				Document doc = null;
				try {
					doc = Document.openDocument(url);
				} catch (IOException ex) {
					eventNotifier.onConnectionError(url.toString());
					eventNotifier.onDoneProcessingUrl(url.toString());
					continue;
				}
				
				eventNotifier.onInfo("Successfully retrieved the document.");
				
				//check and update visited content
				if (!visitedGateway.updateDocExpiration(doc.getHashcode())) {
					eventNotifier.onSkippingVisitedContent(url.toString());
					eventNotifier.onDoneProcessingUrl(url.toString());
					continue;
				}
				
				//parse the document
				IDocumentParser docParser = new DocumentParser(parserPluginManager, linkParserPluginManager);
				try {
					docParser.parseDocument(doc, url.toString());
				} catch (ParseErrorException ex) {
					eventNotifier.onParseError(url.toString(), ex.getMessage());
					eventNotifier.onDoneProcessingUrl(url.toString());
					continue;
				}
				
				eventNotifier.onInfo("Successfully parsed the document.");
	
				//update links
				for (UrlString link : doc.getLinks()) {
					// filter the links found in the document using the appropriate url filter.
					// the filtered urls will be added to the priority queue
					Set<String> urlSet = urlFilterPluginManager.filterUrl(link.toString(), url.toString());
					if (urlSet != null) {
						for (String eachString : urlSet) {
							try {
								UrlString eachUrl = new UrlString(eachString);					
								if (!visitedGateway.isUrlVisited(eachUrl)) {	//not visited							
									if (urlPriorityQueue.addUrl(eachUrl))
										eventNotifier.onLinkAdded(eachUrl.toString());
									notifyFoundUrl(eachUrl);
								}
							} catch (MalformedURLException e) {}
						}
					}
				}
				
				eventNotifier.onInfo("Successfully added links.");
	
				//update hits
				for (Hit hit : doc.getHits()) {
					hitGateway.saveHit(hit);
					eventNotifier.onHitAdded(hit.getCheckoutString());
				}
				
				eventNotifier.onDoneProcessingUrl(url.toString());
			} catch (Exception e) {
				eventNotifier.onException("[Exception] " + e.toString() + "   Stack trace:\n" +
						DebugUtils.getStackTrace(e));
			}
			
			if (pauseMilliSecs > 0) {
				try {
					Thread.sleep(pauseMilliSecs);
				} catch (InterruptedException e) {}
			}
		}

		isStopped = true;
		
		eventNotifier.onCrawlingComplete();
	}
	
	public void pause(boolean paused) {
		synchronized(isPaused) {
			isPaused = paused;
			this.notify();
		}
	}
	
	public void stop() {
		synchronized(isStopped) {
			isStopped = true;
			this.notify();
		}
	}
	
	public boolean isStopped() {
		synchronized(isStopped) {
			return isStopped;
		}
	}
	
	public boolean isPaused() {
		synchronized(isPaused) {
			return isPaused;
		}
	}
	
	public void setEventListener(ICrawlerEventListener eventListener) {
		eventNotifier.setListener(eventListener);
	}

	public void foundUrl(UrlString url) {
		boolean found;
		synchronized(currentDomain) {
			found = url.getDomainName().equals(currentDomain);
		}
		
		if (found) {
			urlPriorityQueue.addUrl(url);
		}
	
	}
	
	public void addInfoExchanger(ICrawlerInfoExchange exchanger) {
		infoExchangers.add(exchanger);
	}
	
	private void notifyFoundUrl(UrlString url) {
		for (ICrawlerInfoExchange exchanger : infoExchangers) {
			exchanger.foundUrl(url);
		}
	}

	public long getNewHitId() {
		return hitGateway.getNewId();
	}
}
