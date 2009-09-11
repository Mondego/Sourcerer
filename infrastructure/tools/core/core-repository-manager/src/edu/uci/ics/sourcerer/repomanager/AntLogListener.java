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

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class AntLogListener implements BuildListener {

	
	public void buildFinished(BuildEvent event) {

	}

	public void buildStarted(BuildEvent event) {

	}

	String logFileName;

	public String getLogFileName() {
		return this.logFileName;
	}

	public void setLogFileName(String fileName) {
		this.logFileName = fileName;
	}

	/**
	 * @param logFileName String representing full path/name of the log file
	 */
	public AntLogListener(String logFileName){
		this.logFileName = logFileName;
		try {
			logger = LogFactory.getFileLogger("edu.uci.ics.sourcerer.repomanager.repotasks", getLogFileName());
		} catch (SecurityException e) {
			// TODO write to console
			System.err.println("Cannot obtain logger");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO write to console
			System.err.println("Cannot obtain logger");
			e.printStackTrace();
		}
	}
	
	Logger logger;

	public void messageLogged(BuildEvent event) {

		if (logger != null) {
			switch (event.getPriority()) {
			case Project.MSG_ERR:
				logger.log(Level.SEVERE, event.getMessage());
				break;
			case Project.MSG_WARN:
				logger.log(Level.WARNING, event.getMessage());
				break;
			case Project.MSG_INFO:
				logger.log(Level.INFO, event.getMessage());
				break;
			case Project.MSG_VERBOSE:
				logger.log(Level.FINEST, event.getMessage());
				break;
			case Project.MSG_DEBUG:
				logger.log(Level.CONFIG, event.getMessage());
				break;
			default:
				logger.log(Level.SEVERE, event.getMessage());
				break;
			}
		}
	}

	public void targetFinished(BuildEvent event) {

	}

	public void targetStarted(BuildEvent event) {

	}

	public void taskFinished(BuildEvent event) {

	}

	public void taskStarted(BuildEvent event) {

	}

}
