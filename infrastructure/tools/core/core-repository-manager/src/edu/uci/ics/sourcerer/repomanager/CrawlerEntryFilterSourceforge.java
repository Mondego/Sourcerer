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
public class CrawlerEntryFilterSourceforge implements ICrawlerEntryFilter {

	public void filter(Map<String, ProjectProperties> projects) {
		
		// HashMap<String, ProjectProperties> uniqProjects = new HashMap<String, ProjectProperties>();
		// <projectName , <projectName>>
		HashMap<String, ProjectProperties> uniqProjects = new HashMap<String, ProjectProperties>();
		
		final Iterator<String> _projIter = projects.keySet().iterator();
		while (_projIter.hasNext()) {
			
			String _projectKey = _projIter.next();
			ProjectProperties _properties = projects.get(_projectKey);
			
			if(!_properties.isJavaProject()){
				_projIter.remove();
			} else {
				
				
				String _subProjectNamePart = (_projectKey.startsWith("cvs") || _projectKey.startsWith("svn"))?"SCM":getProjectNamePart(_projectKey);
				
				if(_subProjectNamePart==null) { 
					// TODO log
					System.out.println(_properties.name + "\t" + _projectKey + "\t" + "[Discarding: invalid project name part]");
					continue; 
				}
				
				if (!_subProjectNamePart.equals("SCM")){
					if(_properties.sourceCertainty.equals("null") || _properties.sourceCertainty.trim().equals("") ||
							! (Float.parseFloat(_properties.sourceCertainty.replaceAll("[^0-9.]", "")) > 0.8) ){ 
						// TODO log
						System.out.println(_properties.name + "\t" + _projectKey + "\t" + "[Discarding: non source link]");
						continue;
					} else {
						// remove the mirror information from the links
						_properties.sourceUrl = _properties.sourceUrl.replaceFirst("\\?use_mirror=.*", "");
					}
				}
				
				if (!uniqProjects.containsKey(_properties.name)){
					uniqProjects.put(_properties.name, _properties);
				}
				
				
				// update the main properties so that it has the SCM properties
				// but preserve the packages if it exists
				if(_subProjectNamePart.equals("SCM")){
					ProjectProperties _currentProjectPropertiesFromUniqeProjects = uniqProjects.get(_properties.name);
					Map<String,ProjectProperties> _map = _currentProjectPropertiesFromUniqeProjects.packages;
					_currentProjectPropertiesFromUniqeProjects = _properties;
					_currentProjectPropertiesFromUniqeProjects.packages = _map;
					_currentProjectPropertiesFromUniqeProjects.extractedVersion = "$SCM";
					_currentProjectPropertiesFromUniqeProjects.versionGuessed = "$SCM";
					
					if(_currentProjectPropertiesFromUniqeProjects.releaseDate==null) 
						_currentProjectPropertiesFromUniqeProjects.releaseDate="null";
					else if(_currentProjectPropertiesFromUniqeProjects.releaseDate.trim().equals(""))
						_currentProjectPropertiesFromUniqeProjects.releaseDate="null";
					
					uniqProjects.put(_properties.name, _currentProjectPropertiesFromUniqeProjects);
				} 
				// work on the packages
				else {
				
					String _subProjectKey = _properties.name + "-" + _subProjectNamePart;
					
					Map<String, ProjectProperties> _packages = uniqProjects.get(_properties.name).packages;
					
					if(_packages==null){
					
						_packages = new HashMap<String, ProjectProperties>();
						_packages.put(_subProjectKey, _properties);
						uniqProjects.get(_properties.name).packages = _packages;
					
					} else if( _packages.containsKey(_subProjectKey)){
						
						// the existing properties is an older release compared to the current being read
						// and there was not any error in parsing the date
						if(! (_properties.compareReleaseDate(_packages.get(_subProjectKey))<1)){
							_packages.put(_subProjectKey, _properties);
						}
						
					} else {
						_packages.put(_subProjectKey, _properties);
					}
					
				}

			}
		}
		
		// empty the original projects list that was passed
		projects.clear();
		
		
		// populate it with new project info
		
		for(ProjectProperties properties : uniqProjects.values()){
			projects.put(properties.scmUrl.equals("null")?properties.sourceUrl:properties.scmUrl, properties);
		}
		
//		
// TODO: move this into a unit test ////////////////
//
//		System.out.println(uniqProjects.size());
//		
//		for(ProjectProperties properties : uniqProjects.values()){
//			
//			if(properties.packages==null){
//				System.out.println(properties.name + "\t" + properties.scmUrl + "\t" + properties.sourceUrl + "\t" + properties.extractedVersion);
//			}
//			
//			if(properties.scmUrl.equals("null")){
//				assert properties.packages!=null;
//			}
//			
//			if(properties.packages!=null){
//				
//				if(properties.packages.size()==0){
//					System.out.println(properties.name + "\t" + properties.scmUrl + "\t" + properties.sourceUrl + "\t" + properties.extractedVersion);			
//					for(String k : properties.packages.keySet()){
//					
//						ProjectProperties pp = properties.packages.get(k);
//						System.out.println(k + "\t" + pp.scmUrl + "\t" + pp.sourceUrl + "\t" + pp.extractedVersion);
//					
//					}
//				}
//			}
//			
//			if(properties.packages!=null){
//			
//				for(String k : properties.packages.keySet()){
//					ProjectProperties pp = properties.packages.get(k);
//					System.out.println(k + "\t" + pp.scmUrl + "\t" + pp.sourceUrl + "\t" + pp.extractedVersion);
//				}
//			}
//			
//		
//		}
/////////////////////////////////////////////////
		
	}

	private String getProjectNamePart(String downloadUrl){
		
		String fileName = getFileName(downloadUrl).replaceFirst("\\?use_mirror=.*", "").toLowerCase();
		ArchivedFileExtensions extension = ArchivedFileExtensions.extractSupportedArchiveExtension(fileName);
		
		if (extension==null) return null;
		
		int endPos = fileName.length()-extension.getExtension().length()-1;
		
		if (endPos < 1) return null;
		
		String _subProjectNamePart =  fileName.substring(0, endPos);
		
		String _subProjectNamePartAlphabetic = _subProjectNamePart.replaceAll("[^a-zA-Z]","");
		
		String _subProjectNamePartAlphabeticVersionCut = _subProjectNamePartAlphabetic.replaceAll("alpha|beta|[^s]rc", "");
		
		if(_subProjectNamePartAlphabeticVersionCut.length()<1) return null;
		
		return _subProjectNamePartAlphabeticVersionCut;
		
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

/**
 *
 #Wed Mar 04 01:18:59 PST 2009
versionGuessed=$SCM
containerUrl=http\://sourceforge.net/cvs/?group_id\=42970
projectDescription=\  Anteater is an Ant-based functional testing framework for Web applications and Web services.   
contentDescription=cvs
package.releaseDate.2=2003-04-12 14\:00
package.releaseDate.1=2002-03-08 08\:00
license=Apache Software License
languageGuessed=Java
keywords=null
exractedVersion=$SCM
package.sourceUrl.2=http\://downloads.sourceforge.net/aft/anteater-0.9.16-src.tar.gz?use_mirror\=internap
crawledDate=2009-Feb-24
package.sourceUrl.1=http\://downloads.sourceforge.net/aft/anteater-0.9.tar.gz?use_mirror\=internap
package.versionGuessed.2=0.9.16
package.versionGuessed.1=0.9
fileExtensions=null
category=WWW/HTTP , Software Development
package.name.2=aft-anteatersrc
package.extractedVersion.2=0.9.16
package.name.1=aft-anteater
package.extractedVersion.1=0.9
sourceUrl=null
releaseDate=null
originRepositoryUrl=http\://sourceforge.net
scmUrl=cvs -d\:pserver\:anonymous@aft.cvs.sourceforge.net\:/cvsroot/aft login; cvs -z3 -d\:pserver\:anonymous@aft.cvs.sourceforge.net\:/cvsroot/aft co -P modulename
package.size=2
name=aft

 * 
 **/