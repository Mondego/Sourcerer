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

import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class TigrisDomainFilterPlugin extends DomainFilterPlugin {

	public TigrisDomainFilterPlugin() {
		super("tigris.org");
	}
	
	public Set<String> filterUrl(String url, String containerUrl) {
		Set<String> urlSet = super.filterUrl(url, containerUrl);
		HashSet<String> result = new HashSet<String>();
		if (urlSet != null) {
			for (String eachUrl : urlSet) {
				if (!eachUrl.contains("tigris.org"))
					continue;
				
				String sub = eachUrl.substring(eachUrl.indexOf("tigris.org/") + "tigris.org/".length()).toLowerCase();
				if (sub.length() == 0)
					result.add(eachUrl);
				else if (sub.contains("/projectlist"))
					result.add(eachUrl);
				else if (sub.contains("/projectdocumentlist"))
					result.add(eachUrl);
				else if (sub.startsWith("source/browse/")) {
					if (UrlUtils.getLevels(eachUrl).length == 4) {
						if (eachUrl.indexOf("?") > 0)
							result.add(eachUrl.substring(0, eachUrl.indexOf("?")));
						else
							result.add(eachUrl);
					}
				}
				else if (sub.contains("newsitemview"))
					continue;
				else if (sub.contains("browselist"))
					continue;
				else if (sub.contains("summarizelist"))
					continue;
				else if (sub.contains("readmsg"))
					continue;
				else
					result.add(eachUrl);
			}
			return result;
		}
		else
			return null;
	}
	
}
