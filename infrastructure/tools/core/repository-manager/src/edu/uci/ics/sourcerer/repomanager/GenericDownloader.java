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
//import java.net.MalformedURLException;
//import java.net.URL;
//
//import org.apache.tools.ant.BuildListener;
//import org.apache.tools.ant.Project;
//import org.apache.tools.ant.Target;
//import org.apache.tools.ant.taskdefs.Get;
//
//
///**
// * 
// * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
// * @created Jan 12, 2009
// * 
// * This class downloads a remote file as pointed by a URL.
// * This works for protocols: http, https, ftp
// * The URL has to end with a valid file format that the exploder
// * can extract.
// */
//public class GenericDownloader {
//	
//	/*
//	 * TODO produce download info file
//	 * @param resourceLink
//	 * @param projectFolder
//	 */
//	public void download(String resourceLink, String projectFolder){
//		download(resourceLink, projectFolder, null);
//		
//	}
//	
//	/**
//	 * 
//	 * @param resourceLink
//	 * @param projectFolder
//	 * @param packageFolder
//	 * @return true if the downloaded file exists inside the content folder, 
//	 * 	false if the downloaded file does not exist inside the content folder
//	 */
//	public boolean download(String resourceLink, String projectFolder, String packageFolder){
//		BuildListener antLogListener = new AntLogListener(projectFolder + File.separator + Constants.getDownloaderLogFileName());
//		
//		ArchivedFileExtensions ext = ArchivedFileExtensions.extractSupportedArchiveExtension(resourceLink);
//		if(ext == null){
//			// TODO log skipped/error
//			return false;
//		}
//		
//		Get downloadTask = new Get();
//		
//		// ant project specific
//		downloadTask.setProject(new Project());
//		downloadTask.getProject().addBuildListener(antLogListener);
//		
//		downloadTask.getProject().init();
//		downloadTask.setTaskType("get");
//		downloadTask.setTaskName("download");
//		downloadTask.setOwningTarget(new Target());
//		
//		try {
//			URL url = new URL(resourceLink);
//			downloadTask.setSrc(url);
//		} catch (MalformedURLException e) {
//			// TODO Log error
//			e.printStackTrace();
//		}
//		
//		if(!projectFolder.endsWith(File.separator))
//			projectFolder = projectFolder + File.separator;
//		
//		String _downloadFileName = produceDownloadFileName(resourceLink);
//		
//		String _destFolder = "";
//		if(packageFolder==null)
//			_destFolder = projectFolder + File.separator + Constants.getDownloadFolderName() + File.separator;
//		else {
//			if(!packageFolder.startsWith(File.separator)) packageFolder = File.separator + packageFolder;
//			_destFolder = projectFolder + File.separator + Constants.getDownloadFolderName() + packageFolder + File.separator;
//		}
//			
//		
//		downloadTask.setDest(new File( _destFolder +  _downloadFileName));
//		downloadTask.execute();
//		
//		return FileUtils.exists(_destFolder + _downloadFileName);
//	}
//	
//	public static String produceDownloadFileName(String urlString){
//		return FileUtility.extractFileNameWithExtension(urlString);
//	}
//	
//}
