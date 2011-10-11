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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class CrawlerEntryFilterJavanet implements ICrawlerEntryFilter {

	public void filter(Map<String, ProjectProperties> projects) {
		
		HashMap<String, ProjectProperties> uniqProjects = new HashMap<String, ProjectProperties>();
		
		final Iterator<String> _projIter = projects.keySet().iterator();
		while (_projIter.hasNext()) {
			
			String _projectKey = _projIter.next();
			ProjectProperties _properties = projects.get(_projectKey);
//			_properties.extractedVersion = "$SVN_REV";
			
			if(!_properties.isJavaProject()){
//				String _op = 
//				_properties.sourceUrl + " " +
//				_properties.downloadLink + " " +
//				_properties.languageGuessed + " " + 
//				_properties.name + " " +
//				_projectKey ;
//					System.out.println(_op);
				_projIter.remove();
			} else {
				
				if (uniqProjects.containsKey(_properties.name)){
					ProjectProperties _curProperties = uniqProjects.get(_properties.name);
					
					if (_curProperties.scmUrl.equals("null") /* && !_properties.scmUrl.equals("null") */ ){
						_curProperties.scmUrl = _properties.scmUrl;
					}
					
					if(isCurrentOlder(_curProperties, _properties)){
						_curProperties.sourceUrl = _properties.sourceUrl;
					}
					
				} else {
					uniqProjects.put(_properties.name, _properties);
				}
			}
		}
		
		/**
		 * Uncomment while testing to see some diagnostic info.. 
		 * Note that this reveals a handful of projects with missing scm links but downloads
		 * and lots of the projects do not have download
		 *
		int c = 0, s = 0;
		for(ProjectProperties _pp : uniqProjects.values()){
			if(_pp.scmUrl.equals("null")){
				System.out.println(_pp.name + " ++++ scmurl null, " + _pp.sourceUrl);
				s++;
			}
			
			if(_pp.sourceUrl == null || _pp.sourceUrl.equals("null")){
				// System.out.println(_pp.name + " *** dwnd null ");
			} else {
				c++;
				System.out.println(_pp.sourceUrl);
			}
			
			// System.out.println(_pp.name + " " + _pp.scmUrl + " " + _pp.sourceUrl);
		}
		System.out.println("with dwnloads " + c + ", without scm " + s);
		*/
		
		// empty the original projects list that was passed
		projects.clear();
		// populate it with new project info
		for(ProjectProperties _pp : uniqProjects.values()){
			projects.put(_pp.scmUrl.equals("null")?_pp.sourceUrl:_pp.scmUrl, _pp);
		}
		
	}
	
	/**
	 * if the file name of the current download is lexicographically smaller than
	 * new download return true else false
	 * 
	 * @param current
	 * @param newp
	 * @return 
	 */
	private boolean isCurrentOlder(ProjectProperties current, ProjectProperties newp){
		
		boolean _currentIsOlder = false;
		
		if(newp.sourceUrl == null || newp.sourceUrl.equals("null"))
			return false;
		
		if (!newp.sourceCertainty.equals("null") && Float.parseFloat(newp.sourceCertainty.replaceAll("[^0-9.]", "")) > 0.8 ){
			
			String _currentFileName = getFileName(current.sourceUrl);
			String _newFileName = getFileName(newp.sourceUrl);
			
			return (_currentFileName.compareTo(_newFileName) < 0);
			
		}
		
		return _currentIsOlder;
	}
	
	/**
	 * 
	 * @param downloadUrl http://something/.../filename
	 * @return filename
	 */
	private String getFileName(String downloadUrl){
		
		return downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1, downloadUrl.length());
	}

}
