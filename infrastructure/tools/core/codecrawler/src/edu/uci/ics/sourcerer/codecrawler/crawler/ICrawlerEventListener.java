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

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 */
public interface ICrawlerEventListener {

	/**
	 * @param url
	 */
	void onProcessingNewUrl(String url);
	void onDoneProcessingUrl(String url);
	void onSkippingVisitedContent(String url);
	void onSkippingNonText(String url);
	void onConnectionError(String url);
	void onParseError(String url, String errorMsg);
	void onLinkAdded(String url);
	void onRefusingVisitedLink(String url);
	void onHitAdded(String hitDownloadString);
	
	void onError(String errorMsg);
	void onInfo(String infoMsg);
	void onWarn(String warnMsg);
	
	void onException(String errorMsg);
	
	void onCrawlingStart();
	void onCrawlingComplete();
	void onCrawlingPause();
	void onCrawlingResume();
	
}
