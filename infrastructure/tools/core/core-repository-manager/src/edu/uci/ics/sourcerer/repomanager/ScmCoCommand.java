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
import java.util.Properties;
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
			if(logger!=null){
				logger.log(Level.WARNING, "SCM entry null in " + propertiesFileName);
			}
		}else if(sourceRetrieveExpression.startsWith("cvs")){
			
			cvsRetriever.retreive(sourceRetrieveExpression, projectFolderName);

			if(logger!=null){
				logger.log(Level.INFO, "CVS CO, 1st attempt, executed from " + propertiesFileName 
						+ " cvs_expression:" + sourceRetrieveExpression);
			}
			pauseCommand();
			
			// if cvs co failed and cvs url is from sourceforge, retry with "./" as modulename
			if(didCvsCoFail(projectFolderName)){
				
				// TODO do this for other repositories too
				if(sourceRetrieveExpression.indexOf("cvs.sourceforge.net") > -1){
					
					if(logger!=null) logger.log(Level.INFO, "CVS CO failed, removing CVS error file in: " + projectFolderName );
					deleteCvsErrorFile(projectFolderName);
					if(logger!=null) logger.log(Level.INFO, "CVS CO failed, cleaning content folder: " + cvsRetriever.getCheckoutFolder() );
					FileUtils.cleanDirectory(new File(cvsRetriever.getCheckoutFolder()));
					
					sourceRetrieveExpression = sourceRetrieveExpression.replaceAll("co -P modulename","co -P ./");
					cvsRetriever.retreive(sourceRetrieveExpression, projectFolderName);
					if(logger!=null){
						logger.log(Level.INFO, "CVS CO, 2nd attempt with './' as module name, executed from " 
								+ propertiesFileName  + " cvs_expression:" + sourceRetrieveExpression);
					}
					
					if(didCvsCoFail(projectFolderName)){
						if(logger!=null){
							logger.log(Level.INFO, "CVS CO failed on 2nd attempt, cleaning content folder: " + cvsRetriever.getCheckoutFolder() );
						}
						FileUtils.cleanDirectory(new File(cvsRetriever.getCheckoutFolder()));
					}
					
					pauseCommand();
				} else {
					if(logger!=null) logger.log(Level.INFO, "CVS CO failed, cleaning content folder: " + cvsRetriever.getCheckoutFolder() );
					FileUtils.cleanDirectory(new File(cvsRetriever.getCheckoutFolder()));
				}
			}
			
		} else if(sourceRetrieveExpression.startsWith("svn")){
			
			boolean svnHasError = ! svnRetriever.retreive(sourceRetrieveExpression, projectFolderName);

			if(logger!=null){
				logger.log(Level.INFO, "SVN CO executed from " + propertiesFileName);
			}
			pauseCommand();
			
			if(svnHasError) {
				if(logger!=null) logger.log(Level.INFO, "SVN CO failed, cleaning content folder: " + svnRetriever.getCheckoutFolder() );
				cleanContentFolderForFailedSvnCo(projectFolderName);
			}
			
		} else {

			if(logger!=null){
				logger.log(Level.WARNING, "Unknown SCM entry in " + propertiesFileName);
			}	
		}
	}

	/**
	 * @param projectFolderName
	 */
	private void deleteCvsErrorFile(String projectFolderName) {
		String errorFileName = projectFolderName  + File.separator + Constants.getCvsErrorFileName();
		File errorFile = new File(errorFileName);
		if(!errorFile.delete()){
			if(logger!=null) logger.log(Level.WARNING, "CVS error file not removed :" + errorFileName);
		}
	}

	private boolean didCvsCoFail(String projectFolderName) {
		
		String errorLineTxt = "cvs [status aborted]: no repository";
		String cvsErrorFile = projectFolderName  + File.separator + Constants.getCvsErrorFileName();
		String cvsCoFile = projectFolderName  + File.separator + Constants.getCvsCoOutputFileName();
		
		String errorLineFromFile = FileUtils.getLastNonEmptyLine(cvsErrorFile); 
		
		if(	// cvs.error ends with line: cvs [status aborted]: no repository
			 (errorLineFromFile !=null && errorLineFromFile.equals(errorLineTxt))||
				// cvsco.out has no entry	
				(FileUtils.getLastNonEmptyLine(cvsCoFile) == null)
				){
			return true;
		} else {
			return false;
		}
		
		// you can also check, but no need
		// cvsstat.out not empty
	}

//	private boolean didSvnCoRunAlready(String projectFolderName) {
//		// is there a svnstat.properties file in the root
//		String svnStatFilePath = projectFolderName + File.separator + Constants.getSvnStatOutputFileName();
//		return FileUtils.exists(svnStatFilePath);
//	}

	
	private void cleanContentFolderForFailedSvnCo(String projectFolderName) {
		
		String contentFolderName = svnRetriever.getCheckoutFolder();
		FileUtils.cleanDirectory(new File(contentFolderName));
		
//		String svnFolderName = contentFolderName + File.separator + ".svn";
//		
//		if(FileUtils.exists(svnFolderName) && FileUtils.countChildren(contentFolderName)==1){
//			if(logger!=null) logger.log(Level.INFO, "SVN CO brought nothing, cleaning content folder: " + svnRetriever.getCheckoutFolder());
//			// delete .svn folder inside the content folder
//			FileUtils.deleteDir(new File(svnFolderName));
//			
//		} 
	}
}
