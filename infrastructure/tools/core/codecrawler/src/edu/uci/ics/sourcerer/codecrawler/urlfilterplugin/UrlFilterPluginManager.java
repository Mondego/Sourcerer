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
package edu.uci.ics.sourcerer.codecrawler.urlfilterplugin;

import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.sourcerer.codecrawler.util.DebugUtils;
import edu.uci.ics.sourcerer.common.LoggerUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class UrlFilterPluginManager extends UrlFilterPlugin {
	
	private static final boolean debugEnabled = LoggerUtils.isDebugEnabled(UrlFilterPluginManager.class);
	
	private HashSet<UrlFilterPlugin> plugins;

	public UrlFilterPluginManager() {
		plugins = new HashSet<UrlFilterPlugin>();
	}
	
	public boolean loadPlugins(String[] classNames) throws UrlFilterPluginLoadException {
		String notLoadedNames = null;
		if (classNames != null) {
			for (String className : classNames) {
				boolean exception = false;
				try {
					Class pluginClass = Class.forName(className);
					UrlFilterPlugin plugin = (UrlFilterPlugin)pluginClass.newInstance();
					plugins.add(plugin);
				} catch (ClassNotFoundException e) {
					if (debugEnabled)
						LoggerUtils.error("Failed to find URL Filter Plugin \"" + className + "\".");
					exception = true;
				} catch (InstantiationException e) {
					if (debugEnabled)
						LoggerUtils.error("Failed to instantiate URL Filter Plugin \"" + className + "\".");
					exception = true;
				} catch (IllegalAccessException e) {
					if (debugEnabled)
						LoggerUtils.error("Failed to access URL Filter Plugin \"" + className + "\".");
					exception = true;
				} catch (ClassCastException e) {
					if (debugEnabled)
						LoggerUtils.error("Failed to cast URL Filter Plugin \"" + className + 
								"\" to IUrlFilterPluginInterface.");
					exception = true;
				} catch (Exception e) {
					if (debugEnabled)
						LoggerUtils.error("Exception \"" + e.getMessage() + "thrown while loading URL Filter Plugin \"" 
								+ className + "\". Stack trace: " + DebugUtils.getStackTrace(e));
				}
				
				
				if (exception)
					if (notLoadedNames == null)
						notLoadedNames = className;
					else
						notLoadedNames += ", " + className;
			}
		}
		
		if (notLoadedNames != null)
			throw new UrlFilterPluginLoadException(notLoadedNames);
		
		return (plugins.size() > 0);
	}
	
	public Set<UrlFilterPlugin> getLoadedPlugins() {
		return plugins;
	}
	
	public Set<String> filterUrl(String url, String containerUrl) {
		if (plugins.size() == 0)
			return null;

		HashSet<String> urlSet = new HashSet<String>();
		
		for (IUrlFilter plugin : plugins) {
			Set<String> pluginSet = plugin.filterUrl(url, containerUrl);
			if (pluginSet != null)
				urlSet.addAll(pluginSet);
		}
		
		return urlSet;
	}
}
