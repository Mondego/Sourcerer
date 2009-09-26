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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class RepoFoldersListerCommand implements RepoCommand {

	Logger logger = null;
	
	List<String> folderNames = new LinkedList<String>();
	
	public void execute(File projectFolder) {
		folderNames.add(projectFolder.getAbsolutePath());
		if (logger != null) {
			logger.log(Level.INFO, "listing " + projectFolder.getName());
		}
	}
	
	public void printNames(){
		for(String _names: folderNames){
			System.out.println(_names);
		}
	}

	public void setLogger(Logger logger){
		this.logger = logger;
		
	}

	@Override
	public long getPauseInMiliSec() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPauseInMiliSec(long pause) {
		// TODO Auto-generated method stub
		
	}
}
