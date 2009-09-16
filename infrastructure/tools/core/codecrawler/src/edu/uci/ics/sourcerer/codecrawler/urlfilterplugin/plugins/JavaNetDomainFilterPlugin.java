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
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.UrlFilterPlugin;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 15, 2009
 *
 */
public class JavaNetDomainFilterPlugin extends UrlFilterPlugin {


	public Set<String> filterUrl(String url, String containerUrl) {

		Set<String> urlSet = new HashSet<String>();

		Pattern projectNamePattern = Pattern
				.compile("http[s]?+://[^.]+[.]{1}dev[.]{1}java[.]{1}net[/]?+");
		// TODO consider getting this from the crawler config?
		String _projectListUrl = "http://community.java.net/projects/alpha.csp";

		if (containerUrl.equals(_projectListUrl)
				&& projectNamePattern.matcher(url).matches()) {

			String _sourceUrl = "";
			String _documentListUrl = "";

			String _projectName = DomainFilterPlugin.getFirstChunkFromUrl(url).split("\\.")[0];

			if (url.endsWith("/")) {
				_sourceUrl = url + "source/browse/" + _projectName;
				_documentListUrl = url + "servlets/ProjectDocumentList";
			} else {
				_sourceUrl = url + "/source/browse/" + _projectName;
				_documentListUrl = url + "/servlets/ProjectDocumentList";
			}

			urlSet.add(_sourceUrl);
			urlSet.add(_documentListUrl);

			return urlSet;

		} else {
			
			return null;
		
		}

	}
}
