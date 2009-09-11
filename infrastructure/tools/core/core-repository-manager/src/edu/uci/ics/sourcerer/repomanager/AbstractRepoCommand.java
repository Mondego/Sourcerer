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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public abstract class AbstractRepoCommand implements RepoCommand {

	protected Logger logger = null;
	
	abstract public void execute(File projectFolder);
	
	protected Properties loadProjectProperties(File projectFolder){
		Properties p = null;
		
		try {
			p = ProjectProperties.loadFromProjectFolder(projectFolder);
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
			// log:
			if(logger!=null){
				logger.log(Level.WARNING, " Project properties file not found in " + projectFolder.getName());
			}
			//return;
		} catch (IOException e) {
			//	e.printStackTrace();
			// log:
			if(logger!=null){
				logger.log(Level.WARNING, "IOException while opening project properties in " + projectFolder.getName());
			}
			//return;
		}
		
		return p;
	}
	
	protected boolean processCodeFolder(String projectFolderName) {
		boolean _processCodeFolder = true;
		
		File codeFolder = new File( new File(projectFolderName) , Constants.getSourceFolderName());
		String[] codeFiles = codeFolder.list();
		
		if(codeFiles == null){
			// should never happen
			logger.log(Level.SEVERE, "Cannot list the contents of source folder in " + projectFolderName);
			_processCodeFolder = false;
		}
		
		if(codeFiles.length>0){
			logger.log(Level.INFO, "Source folder " + codeFolder.getAbsolutePath() + " not empty.");
			_processCodeFolder = false;
		}

		return _processCodeFolder;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
}
