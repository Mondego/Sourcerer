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
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class ScmCoCommand extends AbstractRepoCommand  {

	CvsSourceRetriever cvsRetriever = new CvsSourceRetriever();
	SvnSourceRetriever svnRetriever = new SvnSourceRetriever();
	
	public void execute(File projectFolder) {

		Properties p = loadProjectProperties(projectFolder);
		if (p==null) return;
		
		String projectFolderName = projectFolder.getAbsolutePath();
		String propertiesFileName =  projectFolderName + File.separator + Constants.getSourceProjectPropertiesFileName();
		
		// skip if code folder is not empty
		if (!processCodeFolder(projectFolderName)){
			return;
		}
		
		String sourceRetrieveExpression = p.getProperty("scmUrl"); //"cvs -d :pserver:guest@cvs.dev.java.net:/cvs login;cvs -d :pserver:guest@cvs.dev.java.net:/cvs checkout ss74j";
		if(sourceRetrieveExpression.equals("null")){
			// log: ??
			if(logger!=null){
				logger.log(Level.WARNING, "SCM entry null in " + propertiesFileName);
			}
		}else if(sourceRetrieveExpression.startsWith("cvs")){
			cvsRetriever.retreive(sourceRetrieveExpression, projectFolderName);
			// log: executed ScmCoCommand on projectFolder
			if(logger!=null){
				logger.log(Level.INFO, "CVS CO executed from " + propertiesFileName);
			}
			
		} else if(sourceRetrieveExpression.startsWith("svn")){
			
			svnRetriever.retreive(sourceRetrieveExpression, projectFolderName);
			// log: executed ScmCoCommand on projectFolder
			if(logger!=null){
				logger.log(Level.INFO, "SVN CO executed from " + propertiesFileName);
			}
			
			
		} else {
			// log: unsupported
			if(logger!=null){
				logger.log(Level.WARNING, "Unknown SCM entry in " + propertiesFileName);
			}	
		}
		
	}

	
	
	

}
