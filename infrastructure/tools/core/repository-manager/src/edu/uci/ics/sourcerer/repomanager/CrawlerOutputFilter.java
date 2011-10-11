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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 * Filters the list of project checkouts or downloads from the raw
 * output file that the crawler produces. This eliminates duplicates,
 * skips non source links, only puts the latest version of downloads etc.
 *
 */
public class CrawlerOutputFilter {
	
	private String crawlerOutputFileLocation;
	
	/**
	 * Primary origin, such as: sourceforge, tigris, javanet, apache
	 */
	private String crawledRepositoryName;
	
	/**
	 * Keyed on the project's source url or scm url. 
	 */
	private Map<String, ProjectProperties> projects = new HashMap<String, ProjectProperties>();
	
	private ICrawlerEntryFilter filter;
	
	public void loadProjects() throws IOException{
		
		FileReader _fr = new FileReader(crawlerOutputFileLocation);
		BufferedReader _br = new BufferedReader(_fr);
		
		String _line;
		
		while((_line = _br.readLine()) != null) {
			// each line is _line
			
			String[] _cols = _line.split("\t");
			
			
			if(_cols.length!=15){
				// System.out.println("Discarded: "+ _line);
				continue;
			}
			
			String _projectKey = _cols[2];
			
			if(!ValidLinkPrefixes.matchesStartOf(_projectKey))
				continue;
			
			ProjectProperties _properties = new ProjectProperties();
			_properties.name = _cols[3];
			
			// discard duplicate source or scm links
			if(projects.containsKey(_projectKey))
				continue;
			
			// WARNING: hard coded constants
			if(_cols[2].startsWith("svn") || _cols[2].startsWith("cvs")){
				_properties.scmUrl = _cols[2];
			} else {
				_properties.sourceUrl = _cols[2];
			}
			
			if(_properties.scmUrl==null) _properties.scmUrl = "null";
			if(_properties.sourceUrl==null) _properties.sourceUrl = "null";
			
			
			_properties.crawledDate = _cols[1];
			_properties.projectDescription = _cols[4];
			_properties.category = _cols[5];
			_properties.license = _cols[6];
			_properties.languageGuessed = _cols[7];
			_properties.versionGuessed = _cols[8];
			
			// this is the default
			_properties.extractedVersion = _properties.versionGuessed;
			
			_properties.sourceCertainty = _cols[9];
			
			_properties.releaseDate = _cols[10];
			_properties.contentDescription = _cols[11];
			_properties.containerUrl = _cols[12];
			_properties.keywords = _cols[13];
			_properties.fileExtensions = _cols[14];	
			
			_properties.originRepositoryName = this.crawledRepositoryName;
			_properties.originRepositoryUrl = Enum.valueOf(Repositories.class, this.crawledRepositoryName).getUrl(); 
			
			projects.put(_projectKey, _properties);
			
		}
		
		_br.close();
		_fr.close();
		
		
	}
	
	public void filterProjects(){
		filter.filter(projects);
	}

	public Map<String, ProjectProperties> getProjects() {
		return projects;
	}

	public void setCrawlerOutputFileLocation(String crawlerOutputFileLocation) {
		this.crawlerOutputFileLocation = crawlerOutputFileLocation;
	}

	public void setCrawledRepositoryName(String crawledRepositoryName) {
		this.crawledRepositoryName = crawledRepositoryName;
	}

	public void setFilter(ICrawlerEntryFilter filter) {
		this.filter = filter;
	}

	
}



/** crawler output header 

00 ID	
01 Date	
02 Link	
03 Project-name	
04 Project-description	
05 Project-category	
06 License	
07 Language	
08 Version	
09 Source	
10 Release-date	
11 Description	    --> content description
12 Container-URL	
13 Keywords	
14 	File-Extensions

*/	

/* 

1	
2008-Nov-25	
svn co http://svn.apache.org/repos/asf/velocity/anakia/trunk	
Anakia	
null	
library	
Apache License Version 2.0	
Java	
null	
null	
null	
null	
http://projects.apache.org/projects/anakia.html	
null	
null

4	
2008-Nov-25	
cvs -d :pserver:guest@cvs.dev.java.net:/cvs login;cvs -d :pserver:guest@cvs.dev.java.net:/cvs checkout bean-workstation	
bean-workstation	
GUI program for creating Java Beans	
None	
http://www.gnu.org/licenses/old-licenses/gpl-2.0.html;GNU General Public License (GPL v. 2.0);	
Java?certainty=0.853050	
null	
null	
null	
null	
https://bean-workstation.dev.java.net/source/browse/bean-workstation/	
java=9	
null

1	
2008-Nov-25	
http://antelope.tigris.org/files/documents/1409/11486/AntelopeApp_3.5.0.zip	
antelope	
A graphical user interface for Ant.	
scm;construction	
http://www.apache.org/LICENSE.txt;Apache License;	
Java?certainty=0.688715	
3.5.0?certainty=0.900000	
null	
2008-Aug-31	
Antelope 3.5.0 Binary Application. Get this if you just want to run Antelope.	
http://antelope.tigris.org/servlets/ProjectDocumentList	
java=2;eclipse=1	
null

*/