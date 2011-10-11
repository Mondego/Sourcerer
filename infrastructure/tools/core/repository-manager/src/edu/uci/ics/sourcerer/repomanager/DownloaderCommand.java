///*
// * Sourcerer: An infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// * 
// */
//package edu.uci.ics.sourcerer.repomanager;
//
//import java.io.File;
//import java.util.Arrays;
//import java.util.Properties;
//import java.util.logging.Level;
//
///**
// * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
// * @created Jan 13, 2009
// *
// */
//public class DownloaderCommand extends AbstractRepoCommand {
//
//	GenericDownloader downloader = new GenericDownloader();
//	
//	public void execute(File projectFolder) {
//
//		String projectFolderName = projectFolder.getAbsolutePath();
//		
//		// skip if code folder is not empty
//		if (!processCodeFolder(projectFolderName)){
//			return;
//		}
//		
//		// already has a scm checkout
//		if(Arrays.asList(projectFolder.list()).contains(Constants.getCvsCoOutputFileName()) 
//				|| Arrays.asList(projectFolder.list()).contains(Constants.getSvnStatOutputFileName())) {
//			logger.log(Level.INFO, "Content folder empty. Now, attempting download in project folder: " + projectFolderName);
//		}
//	
//		
//		Properties p = loadProjectProperties(projectFolder);
//		if (p==null) return;
//		
//		String _packagesSize = p.getProperty(ProjectProperties.PACKAGE_SIZE);
//		
//		if(_packagesSize != null){
//			int _pSize = Integer.parseInt(_packagesSize);
//			
//			for(int i=0;i<_pSize;i++){
//			
//				String packageFolderName  = Constants.getPackageFolderName() 
//					+ Constants.getPackageFolderNameSeparator() 
//					+ ((int) i + 1);
//				
//				String packageFolderPath = projectFolderName + File.separator 
//					+ Constants.getDownloadFolderName() + File.separator + packageFolderName;
//				
//				 
//				
//				if(!new File(packageFolderPath).exists())
//					new File(packageFolderPath).mkdir();
//				
//				String packageDownloadUrl = p.getProperty(ProjectProperties.PACKAGE_SOURCEURL + Constants.getPackageFolderNameSeparator() + ((int) i+1));
//				download(projectFolderName, packageDownloadUrl, packageFolderName);
//			
//			}
//			
//		} else {
//			// WARNING hardcoded string
//			// TODO need to check if this url is "null" ?
//			String _downloadUrl = p.getProperty("sourceUrl");
//			
//			download(projectFolderName, _downloadUrl, null);
//		}
//		
//	}
//
//	private boolean download(String projectFolderName, String downloadUrl, String packageFolderName) {
//		boolean success = false;
//		try{
//			success = downloader.download(downloadUrl, projectFolderName, packageFolderName);
//			if(logger!=null){
//				logger.log(Level.INFO, "Downloaded source file from " + downloadUrl + " to " + projectFolderName);
//			}
//			pauseCommand();
//		} catch (Exception e){
//			if(logger!=null){
//				logger.log(Level.SEVERE, "Exception while downloading source file from " + downloadUrl + 
//										 " to " + projectFolderName + "\n" + e.getMessage());
//			}
//			
//			e.printStackTrace();
//		}
//		
//		return success;
//	}	
//}
