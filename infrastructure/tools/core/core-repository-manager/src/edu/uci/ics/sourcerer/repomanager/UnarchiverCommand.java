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
//import java.io.FilenameFilter;
//import java.util.Properties;
//import java.util.logging.Logger;
//import java.util.logging.Level;
//
///**
// * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
// * @created Jan 13, 2009
// * 
// */
//public class UnarchiverCommand extends AbstractRepoCommand {
//
//	Unarchiver unarchiver = new Unarchiver();
//	
//	public void execute(File projectFolder) {
//		
//		// skip if code folder is not empty
//		if (!processCodeFolder(projectFolder.getAbsolutePath())){
//			return;
//		}
//		
//		File _downloadFolder = new File(projectFolder, Constants.getDownloadFolderName());
//		String _projectFolderPath = projectFolder.getAbsolutePath();
//		
//		Properties p = loadProjectProperties(projectFolder);
//		String _packagesSize = p.getProperty(ProjectProperties.PACKAGE_SIZE);
//		
//		if (_packagesSize==null) {
//			
//			boolean success = unarchive(_downloadFolder, _projectFolderPath, null);
//			if(!success) { 
//				String _contentFolderName = _projectFolderPath + File.separator + Constants.getSourceFolderName(); 
//				if (logger != null) {
//					logger.log(Level.INFO, "Extraction failed. Cleaning: " + _contentFolderName);
//				}
//				FileUtils.cleanDirectory(new File(_contentFolderName));
//			}
//
//		} else {
//		
//			int _pSize = Integer.parseInt(_packagesSize);
//			String _codeFolderPath = _projectFolderPath + File.separator + Constants.getSourceFolderName();
//			
//			for(int i=0;i<_pSize;i++){
//				String _packageSuffix = Constants.getPackageFolderName() 
//					+ Constants.getPackageFolderNameSeparator() 
//					+ ((int) i + 1);
//				
//				String packageFolderSrc = _downloadFolder.getAbsolutePath() + File.separator 
//					+ _packageSuffix ;
//				
//				String packageFolderDest = _codeFolderPath + File.separator 
//					+ _packageSuffix;  
//				
//				if(FileUtils.countChildren(packageFolderSrc)>0 && 
//						!new File(packageFolderDest).exists())
//					new File(packageFolderDest).mkdir();
//				
//				boolean success = unarchive(new File(packageFolderSrc), _projectFolderPath, _packageSuffix);
//				if(!success && new File(packageFolderDest).exists()) {
//					if (logger != null) {
//						logger.log(Level.INFO, "Extraction failed. Deleting: " + packageFolderDest);
//					}
//					FileUtils.deleteDir(new File(packageFolderDest));
//				}
//				
//			}
//		}
//
//	}
//
//	private boolean unarchive(File source, String destination, String packagePath) {
//		boolean success = false;
//		
//		String[] _archives = source.list(new FilenameFilter() {
//
//			public boolean accept(File dir, String name) {
//				// TODO Auto-generated method stub
//				return ArchivedFileExtensions
//						.extractSupportedArchiveExtension(name) != null ? true
//						: false;
//			}
//
//		});
//		
//		if (_archives != null && _archives.length > 0) {
//			
//			String _archiveFilePath = source.getAbsolutePath()
//					+ File.separator + _archives[0];
//
//			try {
//				unarchiver.unarchive(_archiveFilePath, destination, packagePath);
//				
//				if (logger != null) {
//					logger.log(Level.INFO, "Extracted " + _archiveFilePath
//							+ " to " + destination
//							+ "'s content folder");
//				}
//				success = true;
//				
//			} catch (Exception e) {
//				if (logger != null) {
//					logger.log(Level.SEVERE,
//							"Exception thrown while unarchiving. "
//									+ _archiveFilePath + " to "
//									+ destination
//									+ "'s content folder" + "\n"
//									+ e.getMessage());
//				}
//				e.printStackTrace();
//			}
//		}
//		
//		return success;
//	}
//}
