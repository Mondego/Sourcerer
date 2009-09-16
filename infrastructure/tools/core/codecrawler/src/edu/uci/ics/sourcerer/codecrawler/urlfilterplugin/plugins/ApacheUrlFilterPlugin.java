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
package edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.plugins;

import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.UrlFilterPlugin;
import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;
import edu.uci.ics.sourcerer.common.LoggerUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class ApacheUrlFilterPlugin extends UrlFilterPlugin {

	private static final boolean debugEnabled = LoggerUtils.isDebugEnabled(ApacheUrlFilterPlugin.class);
	
	public Set<String> filterUrl(String url, String containerUrl) {
		if (containerUrl.toLowerCase().contains("projects.apache.org/indexes/language.html")) {
			String[] levels = UrlUtils.getLevels(url);
			if (levels.length != 3)
				return null;
			if (!levels[0].equals("projects.apache.org"))
				return null;
			if (!levels[1].equals("projects"))
				return null;
			Set<String> result = new HashSet<String>();
			result.add(url);
			return result;
		}
		return null;
	}

}
