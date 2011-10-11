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
	protected long pause = 0;
	
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
		return RepoWalker.isContentFolderEmpty(projectFolderName, logger);
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	protected void pauseCommand(){
		if(pause > 0)
			try {
				Thread.sleep(pause);
			} catch (InterruptedException e) {
				// e.printStackTrace();
				if(logger!=null){
					logger.log(Level.WARNING, "Cannot pause command.");
				}
			}
	}
	
	@Override
	public long getPauseInMiliSec() {
		return this.pause;
	}

	@Override
	public void setPauseInMiliSec(long pause) {
		this.pause = pause;
	}
}
