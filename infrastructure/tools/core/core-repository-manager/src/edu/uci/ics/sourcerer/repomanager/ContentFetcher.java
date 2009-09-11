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
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 13, 2009
 *
 */
public class ContentFetcher {
	Logger logger;
	private RepoWalker walker;
	
	private ScmCoCommand scmCoCommand = new ScmCoCommand();
	private DownloaderCommand downloaderCommand = new DownloaderCommand();
	private UnarchiverCommand unarchiverCommand = new UnarchiverCommand();
	
	public ContentFetcher(String repoRoot){
		walker = new RepoWalker(new File(repoRoot));
		
		try {
			// WARNING hard coded name
			logger = LogFactory.getFileLogger("edu.uci.ics.sourcerer.repomanager.ContentFetcher", repoRoot + File.separator + "content-fetcher.log", true);
			
			walker.setLogger(logger);
			scmCoCommand.setLogger(logger);
			downloaderCommand.setLogger(logger);
			unarchiverCommand.setLogger(logger);
		} catch (SecurityException e) {
			System.err.println("Cannot obtain logger");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Cannot obtain logger");
			e.printStackTrace();
		}
		
		// these orders (indices) will dictate which command is executed first
		walker.addCommand(scmCoCommand, 0);
		walker.addCommand(downloaderCommand, 1);
		walker.addCommand(unarchiverCommand, 2);
	}
	
	public void setPauseDuration(long duration){
		walker.setPauseDuration(duration);
	}
	
	public void fetch(){
		
		if(logger!=null)
			logger.log(Level.INFO, "!!! STARTED FETCHING CONTENT !!!");
		
		try {
			walker.startWalk();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("IOException while fetching contents.");
			e.printStackTrace();
		}
		
		if(logger!=null)
			logger.log(Level.INFO, "!!! DONE FETCHING CONTENT !!!");
		
	}
}
