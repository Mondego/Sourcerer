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
package edu.uci.ics.sourcerer.codecrawler.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.UrlFilterPluginManager;
import edu.uci.ics.sourcerer.common.LoggerUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 * A util class to retrieve specific properties for the crawler.
 * Singleton.
 */
public class CrawlerProperties extends Properties {
	
	private static final long serialVersionUID = 412348710932847L;
	private static final boolean debugEnabled = LoggerUtils.isDebugEnabled(UrlFilterPluginManager.class);
	
	public static final String DEFAULT_FILE_NAME = "crawler.properties";
	public static final String SEPARATOR = ".";
	public static final String CRAWLER = "crawler";
	public static final String PAUSE_BETWEEN_PAGES = "pauseBetweenPages";
	public static final String LINK_PARSER_PLUGINS = "linkParserPlugins";
	public static final String PARSER_PLUGINS = "parserPlugins";
	public static final String URL_FILTER_PLUGINS = "urlFilterPlugins";
	public static final String CLASS_NAME_SEPARATORS = ";";
	public static final String INIT_URL = "initUrl";
	public static final String DB_GATEWAY_FACTORY = "dbGatewayFactory";
	
	private static CrawlerProperties singleton = new CrawlerProperties();
	
	public static CrawlerProperties getInstance() {
		return singleton;
	}
	
	public static CrawlerProperties loadPropertiesFile(String filename) throws CrawlerPropertiesException {
		return (singleton = new CrawlerProperties(filename));
	}

	protected CrawlerProperties() {
	}
	
	protected CrawlerProperties(String loadFromFileName) throws CrawlerPropertiesException {	
		try {
			super.load(new FileInputStream(loadFromFileName));
		} catch (IOException ex) {
			if (debugEnabled)
				LoggerUtils.error("Unable to load crawler properties file: \"" + loadFromFileName + "\".");
			throw new CrawlerPropertiesException("Unable to load crawler properties file: \"" + loadFromFileName + "\"");
		}
	}
	
	/**
	 * This method goes throw the properties to pick out
	 * the Parser Plug-in class names. You might want to save
	 * the result for future use.
	 * @return <code>null</code> if there is plug-in or an array of
	 * the class names of the plug-ins.
	 */
	public String[] getParserPlugins() {
		String plugins = getProperty(CRAWLER+SEPARATOR+PARSER_PLUGINS);
		return getListOfClassNames(plugins);
	}
	
	/**
	 * @return The class name of the URL filter, or <code>null</code> if
	 * no plugin is found.
	 */
	public String[] getFilterPlugins() {
		String plugins = getProperty(CRAWLER+SEPARATOR+URL_FILTER_PLUGINS);
		return getListOfClassNames(plugins);
	}
	
	public String[] getLinkParserPlugins() {
		String plugins = getProperty(CRAWLER+SEPARATOR+LINK_PARSER_PLUGINS);
		return getListOfClassNames(plugins);
	}
	
	public String[] getInitUrls() {
		int idx = 1;
		ArrayList<String> list = new ArrayList<String>();
		String plugin = null;
		while ((plugin = getProperty(CRAWLER+SEPARATOR+INIT_URL+idx)) != null) {
			list.add(plugin);
			idx++;
		}
		
		if (list.size() == 0)
			return null;
		else {
			String[] result = new String[list.size()];
			for (int i = 0; i < list.size(); i++)
				result[i] = list.get(i);
			return result;
		}
	}
	
	public int getPauseBetweenPages() throws CrawlerPropertiesException {
		String pauseTxt = getProperty(CRAWLER+SEPARATOR+PAUSE_BETWEEN_PAGES);
		if (pauseTxt == null)
			return 0;		//default is no pausing
		try {
			return Integer.parseInt(pauseTxt);
		} catch (NumberFormatException e) {
			throw new CrawlerPropertiesException("Bad number format for property \"" + PAUSE_BETWEEN_PAGES +"\"");
		}
	}
	
	private String[] getListOfClassNames(String list) {
		if (list == null)
			return null;
		StringTokenizer tokenizer = new StringTokenizer(list, CLASS_NAME_SEPARATORS);
		if (tokenizer.countTokens() > 0) {
			String[] classNames = new String[tokenizer.countTokens()];
			int idx = 0;
			while (tokenizer.hasMoreTokens()) {
				classNames[idx++] = tokenizer.nextToken();
			}
			return classNames;
		}
		return null;
	}
	
	public String getDbGatewayFactory() {
		return getProperty(CRAWLER+SEPARATOR+DB_GATEWAY_FACTORY);
	}
}
