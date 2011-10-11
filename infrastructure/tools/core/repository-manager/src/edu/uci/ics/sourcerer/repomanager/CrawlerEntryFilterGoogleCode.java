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
package edu.uci.ics.sourcerer.repomanager;

import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 23, 2009
 *
 */
public class CrawlerEntryFilterGoogleCode implements ICrawlerEntryFilter {


	@Override
	public void filter(Map<String, ProjectProperties> projects) {
		// - filter mercurial entries for now
		// - googlecode has only svn and mrcurial
		// - each entry from the crawler's ouput is already unique
		final Iterator<String> _projIter = projects.keySet().iterator();
		while (_projIter.hasNext()) {
			
			String _projectKey = _projIter.next();
			ProjectProperties _properties = projects.get(_projectKey);
			
			if(!_properties.scmUrl.startsWith("svn")){
				_projIter.remove();
			}
			
			_properties.extractedVersion = "$SCM";
			_properties.versionGuessed = "$SCM";
		}

	}

}
