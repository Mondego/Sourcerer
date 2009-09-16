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
package edu.uci.ics.sourcerer.codecrawler.parserplugin;

import java.util.HashSet;
import java.util.Set;

import org.htmlparser.Parser;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.common.LoggerUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class ParserPluginManager extends ParserPlugin {

	private static final boolean debugEnabled = LoggerUtils.isDebugEnabled(ParserPluginManager.class);
	
	private HashSet<ParserPlugin> plugins;
	
	public ParserPluginManager() {
		plugins = new HashSet<ParserPlugin>();
	}
	
	/**
	 * IMPORTANT: call this method after calling setIdGeneartor()
	 */
	public boolean loadPlugins(String[] classNames) throws ParserPluginLoadException {
		String notLoadedNames = null;
		if (classNames != null) {
			for (String className : classNames) {
				boolean exception = false;
				try {
					Class pluginClass = Class.forName(className);
					ParserPlugin plugin = (ParserPlugin)pluginClass.newInstance();
					plugin.setIdGenerator(getIdGenerator());
					plugins.add(plugin);
				} catch (ClassNotFoundException e) {
					if (debugEnabled)
						LoggerUtils.error("Failed to find Parser Plugin \"" + className + "\".");
					exception = true;
				} catch (InstantiationException e) {
					if (debugEnabled)
						LoggerUtils.error("Failed to instantiate Parser Plugin \"" + className + "\".");
					exception = true;
				} catch (IllegalAccessException e) {
					if (debugEnabled)
						LoggerUtils.error("Failed to access Parser Plugin \"" + className + "\".");
					exception = true;
				} catch (ClassCastException e) {
					if (debugEnabled)
						LoggerUtils.error("Failed to cast Parser Plugin \"" + className + 
								"\" to ParserPlugin.");
					exception = true;
				}
				
				if (exception)
					if (notLoadedNames == null)
						notLoadedNames = className;
					else
						notLoadedNames += ", " + className;
			}
		}
		
		if (notLoadedNames != null)
			throw new ParserPluginLoadException(notLoadedNames);
		
		return (plugins.size() > 0);
	}
	
	public Set<ParserPlugin> getLoadedPlugins() {
		return plugins;
	}

	public Set<Hit> parseHits(Parser htmlParser, String referringUrl) {
		if (plugins.size() == 0)
			return null;
		
		HashSet<Hit> resultSet = null;
		for (ParserPlugin plugin : plugins) {
			htmlParser.reset();
			Set<Hit> parsed = plugin.parseHits(htmlParser, referringUrl);
			if (parsed != null) {
				if (resultSet == null)
					resultSet = new HashSet<Hit>();
				resultSet.addAll(parsed);
			}
		}
		
		return resultSet;
	}
	
}
