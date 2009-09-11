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

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class RepoWalker extends AbstractFileScanner {
	
	Logger logger;
	long pauseDuration = 0;
	
	public void setLogger(Logger logger){
		this.logger = logger;
	}
	
	public void setPauseDuration(long duration){
		this.pauseDuration = duration;
	}
	
	public RepoWalker(File repoRoot) {
		super(repoRoot);
		setMaxDepth(2);
		setAcceptDir(true);
		
		this.acceptFileCommand= new AcceptFileCommand() {

			public void accept(File f, int depth) {
				
				List<String> _folderContents = new ArrayList<String>(3);
				
				// the folder needs to contain:
				// properties files, download and checkout folder
				_folderContents.add(Constants.getSourceProjectPropertiesFileName());
				_folderContents.add(Constants.getDownloadFolderName());
				_folderContents.add(Constants.getSourceFolderName());
		
				if(depth==1 && hasChildren(f, _folderContents)){
					
					if(pauseDuration > 0)
						try {
							Thread.sleep(pauseDuration);
						} catch (InterruptedException e) {
							// e.printStackTrace();
							logger.log(Level.WARNING, "Cannot pause walk..");
						}
					
					operateOn(f);
				} 
					
			}
		};
		
	}

	String repoRoot;
	List<RepoCommand> repoCommands = new ArrayList<RepoCommand>();
	
	public void startWalk() throws IOException{
		scan();
	}
	
	/**
	 * 
	 * @param command
	 * @param index indicates the order in which this command will be executed.	
	 * 			clients should take care they use correct and unique index numbers
	 * 			for each command they are adding
	 */
	public void addCommand(RepoCommand command, int index){
		repoCommands.add(index, command);
	}
	
	public void clearCommands(){
		repoCommands.clear();
	}
	
	private void operateOn(File projectFolder){
		
		if(logger!=null) logger.log(Level.INFO, "Started working on: " + projectFolder.getAbsolutePath());
		
		for(int i=0;i<repoCommands.size();i++){
			RepoCommand command = repoCommands.get(i); 
			if(command!=null) command.execute(projectFolder);
		}
		
	}

	@Override
	protected File isAccepted(File currentFilesParent, String currentFileName) {
		
		if(!currentFileName.matches("[0-9]+")){
			return null;
		}
		
		File _currentFile = new File(currentFilesParent, currentFileName);
		if(!_currentFile.isDirectory()){
			return null;
		}
		
		return new File(currentFilesParent, currentFileName);
		
	}

	
	private boolean hasChildren(File dir, List<String> children){
		return Arrays.asList(dir.list()).containsAll(children);
	}
	
}
