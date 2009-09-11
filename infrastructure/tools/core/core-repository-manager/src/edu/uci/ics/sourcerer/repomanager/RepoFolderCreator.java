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

import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 * 
 * Creates the initial (empty) repository structure of folders
 * based on the filtered projects list
 * 
 * Creates 
 * - the output folders inside the project folder
 * - the project metadata (properties) file
 * - the script in the repository root that lets you download
 *   all the certificates needed to execute svn against secured
 *   svn servers (https://)
 */
public class RepoFolderCreator {

	private StringBuffer sslSvnUrls = new StringBuffer();
	
	private String repositoryRoot;
	private int startBatchNumber;
	private int delayBetweenCertDownload = 0;
	// should not be zero
	private int maxFoldersInBatch;
	private Map<String, ProjectProperties> filteredProjects;
	
	public void createFolders(){
		int _projectCount = 0;
		for(ProjectProperties _properties: filteredProjects.values()){
				createProjectFolder(generateNextProjectFolderPath(_projectCount++), _properties);
				
				String _scmLink = _properties.scmUrl;
					
				_scmLink = _scmLink.replaceAll("[\\s]+", " ");
				String[] _scmLinkParts = _scmLink.split(" ");
				if (_scmLinkParts.length >= 3 
						&& _scmLinkParts[0].trim().equalsIgnoreCase("svn") 
						&& _scmLinkParts[2].trim().startsWith("https://")){
					sslSvnUrls.append("\n svn ls " + _scmLinkParts[2]);
					
					// insert pause if needed
					if(delayBetweenCertDownload>0)
						sslSvnUrls.append("\n sleep " + delayBetweenCertDownload);
				}
						
		}
		
		if (sslSvnUrls.length()>0){
			
			StringBuffer _scriptInBuffer = new StringBuffer();
			
			// script top 
			_scriptInBuffer.append("#!/bin/bash\n");
			_scriptInBuffer.append("# getcert.sh\n");
			_scriptInBuffer.append("# automates the download of certifiates needed for svn checkout via https\n");
			_scriptInBuffer.append("yes \"p\" | {");
			
			// script middle
			_scriptInBuffer.append(sslSvnUrls);
			
			// script bottom
			_scriptInBuffer.append("\n}\n"); 
			_scriptInBuffer.append("echo \"done.\"");
			
			String _scriptAsString = _scriptInBuffer.toString();
			
			File _getCertScriptFile = new File(repositoryRoot + File.separator + Constants.getCertScriptFileName());
			
			try {
				FileUtility.writeStringToFile(_scriptAsString, _getCertScriptFile);
			} catch (IOException e) {
				// TODO Log this
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * create folder at 'path' with the following form: repositoryRoot/(b)atch_id/(c)heckout_id
	 * inside 'path' creates:
		  file   : project.properties -- project metadata file
		  folder : contents -- (holds either the unzipped content or the checkout from the scm)
		  folder : download -- (holds the file/zip that was downloaded (if downloaded))
	 * @param path
	 * @param properties
	 */
	private void createProjectFolder(String path, ProjectProperties properties){
		File _repoFolder = new File(path);
		
		if(_repoFolder.exists())
			throw new RuntimeException("Folder " + path + " already exists. Check your options.");
		
		_repoFolder.mkdirs();
		
		new File(path + File.separator + Constants.getSourceFolderName()).mkdir();
		
		new File(path + File.separator + Constants.getDownloadFolderName()).mkdir();
		
		String _propertiesFile = path + File.separator + Constants.getSourceProjectPropertiesFileName();
		
		try {
			properties.write(_propertiesFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Cannot write: " + _propertiesFile + "File not found Exception");
		} catch (IOException e) {
			throw new RuntimeException("Cannot write: " + _propertiesFile + "IO Exception");
		}

	}
	
	private String generateNextProjectFolderPath(int currentProjectCount){
		
		int _batchId = (currentProjectCount / maxFoldersInBatch);
		_batchId = startBatchNumber + _batchId;
		
		int _checkoutId = currentProjectCount % maxFoldersInBatch;
		
		return repositoryRoot + File.separator + _batchId + File.separator + _checkoutId;

	}

	public void setRepositoryRoot(String repositoryRoot) {
		this.repositoryRoot = repositoryRoot;
	}

	public void setStartBatchNumber(int startBatchNumber) {
		this.startBatchNumber = startBatchNumber;
	}

	public void setMaxFoldersInBatch(int maxFoldersInBatch) {
		this.maxFoldersInBatch = maxFoldersInBatch;
	}

	public void setFilteredProjects(Map<String, ProjectProperties> filteredProjects) {
		this.filteredProjects = filteredProjects;
	}

	public void setDelayBetweenCertDownload(int delayBetweenCertDownload) {
		this.delayBetweenCertDownload = delayBetweenCertDownload;
	}
}
