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
//
//import org.apache.tools.ant.BuildListener;
//import org.apache.tools.ant.Project;
//import org.apache.tools.ant.Target;
//import org.apache.tools.ant.taskdefs.Expand;
//import org.apache.tools.ant.taskdefs.Untar;
//import org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod;
//
///**
// * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
// * @created Jan 12, 2009
// *
// */
//public class Unarchiver {
//	
//	/**
//	 * @param sourceFilePath
//	 * @param projectFolder
//	 * @param packageFolder an absolute path inside the project folder, must start with file separator
//	 */
//	public void unarchive(String sourceFilePath, String projectFolder, String packageFolder){
//		
//		String _packageFolder = packageFolder==null?"":packageFolder;
//		if(!packageFolder.startsWith(File.separator))
//			_packageFolder = File.separator + _packageFolder;
//		
//		BuildListener antLogListener = new AntLogListener(projectFolder + File.separator + Constants.getUnarchiverLogFileName());
//		
// 		ArchivedFileExtensions _ext = ArchivedFileExtensions.extractSupportedArchiveExtension(sourceFilePath);
// 		
// 		if(_ext.equals(ArchivedFileExtensions.JAR) 
// 				|| _ext.equals(ArchivedFileExtensions.ZIP)
// 				|| _ext.equals(ArchivedFileExtensions.EAR)
// 				|| _ext.equals(ArchivedFileExtensions.WAR)){
// 		
// 			unzip(sourceFilePath, projectFolder, _packageFolder, antLogListener);
// 		
// 		}
// 		else if (_ext.equals(ArchivedFileExtensions.TAR) 
// 					|| _ext.equals(ArchivedFileExtensions.BZIP) 
// 					|| _ext.equals(ArchivedFileExtensions.GZIP) 
// 					|| _ext.equals(ArchivedFileExtensions.TGZ) 
// 					|| _ext.equals(ArchivedFileExtensions.BZ)
// 					|| _ext.equals(ArchivedFileExtensions.BZ2)
// 					|| _ext.equals(ArchivedFileExtensions.GZ)
// 					|| _ext.equals(ArchivedFileExtensions.TBZ)
// 					|| _ext.equals(ArchivedFileExtensions.TARBZ)){
// 		
// 			untar(sourceFilePath, projectFolder, _packageFolder, antLogListener, _ext);
// 		
// 		}
//	}
//	
//	/**
//	 * works for zip
//	 * TODO make it work for other archive formats
//	 * @param sourceFilePath
//	 * @param projectFolder
//	 */
//	public void unarchive(String sourceFilePath, String projectFolder){
//		
//		unarchive(sourceFilePath, projectFolder, null);
// 		
//	}
//
//	private void untar(String sourceFilePath, String projectFolder, String packageFolder,
//			BuildListener antLogListener, ArchivedFileExtensions ext) {
//		
//		Untar untarrer = new Untar();
//		
//		UntarCompressionMethod method = new UntarCompressionMethod();
//		if(ext.equals(ArchivedFileExtensions.GZIP) 
//				|| ext.equals(ArchivedFileExtensions.TGZ)
//				|| ext.equals(ArchivedFileExtensions.GZ))
//			method.setValue("gzip");
//		else if(ext.equals(ArchivedFileExtensions.BZIP) 
//				|| ext.equals(ArchivedFileExtensions.BZ)
//				|| ext.equals(ArchivedFileExtensions.BZ2)
//				|| ext.equals(ArchivedFileExtensions.TBZ)
//				|| ext.equals(ArchivedFileExtensions.TARBZ) )
//			method.setValue("bzip2");
//		
//		untarrer.setCompression(method);
//		
//		untarrer.setProject(new Project());
//		untarrer.getProject().addBuildListener(antLogListener);
//		untarrer.setOwningTarget(new Target());
//		
//		untarrer.setTaskType("untar");
//		untarrer.setTaskName("untar");
//		
//		File fSrc = new File(sourceFilePath);
//		File fDest = new File(projectFolder + File.separator + Constants.getSourceFolderName() + packageFolder);
//		
//		untarrer.setDest(fDest);
//		untarrer.setSrc(fSrc);
//		
//		untarrer.getProject().init();
//		untarrer.execute();
//		
//		fSrc = null;
//		fDest = null;
//		
//		
//		
//	}
//	
//	private void unzip(String sourceFilePath, String projectFolder, String packageFolder,
//			BuildListener antLogListener) {
//		Expand unzipper = new Expand();
//		unzipper.setProject(new Project());
//		unzipper.getProject().addBuildListener(antLogListener);
//		
//		unzipper.setOwningTarget(new Target());
//		
//		unzipper.setTaskType("unzip");
//		unzipper.setTaskName("unzip");
//		
//		File fSrc = new File(sourceFilePath);
//		File fDest = new File(projectFolder + File.separator + Constants.getSourceFolderName() + packageFolder);
//		
//		unzipper.setDest(fDest);
//		unzipper.setSrc(fSrc);
//		
//		unzipper.getProject().init();
//		unzipper.execute();
//		
//		fSrc = null;
//		fDest = null;
//	}
//}
