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
package edu.uci.ics.sourcerer.codecrawler;

import edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 15, 2009
 *
 */
public class DummyCrawlerListener implements ICrawlerEventListener {

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onConnectionError(java.lang.String)
	 */
	public void onConnectionError(String url) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onCrawlingComplete()
	 */
	public void onCrawlingComplete() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onCrawlingPause()
	 */
	public void onCrawlingPause() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onCrawlingResume()
	 */
	public void onCrawlingResume() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onCrawlingStart()
	 */
	public void onCrawlingStart() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onDoneProcessingUrl(java.lang.String)
	 */
	public void onDoneProcessingUrl(String url) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onError(java.lang.String)
	 */
	public void onError(String errorMsg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onException(java.lang.String)
	 */
	public void onException(String errorMsg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onHitAdded(java.lang.String)
	 */
	public void onHitAdded(String hitDownloadString) {
		// TODO Auto-generated method stub
		System.out.println("hit_added: " + hitDownloadString);

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onInfo(java.lang.String)
	 */
	public void onInfo(String infoMsg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onLinkAdded(java.lang.String)
	 */
	public void onLinkAdded(String url) {
		// TODO Auto-generated method stub
		System.out.println("lnk_added: " + url);

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onParseError(java.lang.String, java.lang.String)
	 */
	public void onParseError(String url, String errorMsg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onProcessingNewUrl(java.lang.String)
	 */
	public void onProcessingNewUrl(String url) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onRefusingVisitedLink(java.lang.String)
	 */
	public void onRefusingVisitedLink(String url) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onSkippingNonText(java.lang.String)
	 */
	public void onSkippingNonText(String url) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onSkippingVisitedContent(java.lang.String)
	 */
	public void onSkippingVisitedContent(String url) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.codecrawler.crawler.ICrawlerEventListener#onWarn(java.lang.String)
	 */
	public void onWarn(String warnMsg) {
		// TODO Auto-generated method stub

	}

}
