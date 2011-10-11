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

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public enum ArchivedFileExtensions {
	
	ZIP("zip"),
	JAR("jar"),
	WAR("war"),
	EAR("ear"),
	
	TAR("tar"),
	
	GZIP("tar.gz"),
	BZIP("tar.bz2"), 
	TGZ("tgz"), 
	TARBZ("tar.bz"),
	BZ("bz"),
	GZ("gz"),
	BZ2("bz2"),
	TBZ("tbz2");
	
	
	// other valid archives not yet supported by ANT
	// TARZ("tar.z"),	
	// SEVENZ("7z");
	/*
	 *  	
	 	// possible archives
	 	deb
	 	dmg
	 	dsc
	 	msi
	 	nbm
	 	rar
	 	rpm
	 	sar
	 	sit
	 	sxw
	 	tbz2
	 	xpi
	 	
	 	
	 */
	
	private String extension;
	
	ArchivedFileExtensions(String extension){
		this.extension = extension;
	}
	
	public String getExtension(){
		return this.extension;
	}
	
	/**
	 * 
	 * @param fileName the String representing the (full) path of the archive resource
	 * 			Url or File. The possible extension in fileName is compared after it i
	 * 			converted to lowercase
	 * @return null if fileName does not end with a supported extension
	 */
	public static ArchivedFileExtensions extractSupportedArchiveExtension(String fileName){
		
//		for(ArchivedFileExtensions ext: ArchivedFileExtensions.values()){
//			if(fileName.toLowerCase().endsWith(ext.getExtension()))
//				return ext;
//		}
		
		String _fName = fileName.toLowerCase();
		
		if(_fName.endsWith(ArchivedFileExtensions.GZIP.getExtension()))
			return ArchivedFileExtensions.GZIP;
		
		if(_fName.endsWith(ArchivedFileExtensions.BZIP.getExtension()))
			return ArchivedFileExtensions.BZIP;
		
		if(_fName.endsWith(ArchivedFileExtensions.TARBZ.getExtension()))
			return ArchivedFileExtensions.TARBZ;
		
		for(ArchivedFileExtensions ext: ArchivedFileExtensions.values()){
		if(fileName.toLowerCase().endsWith(ext.getExtension()))
			return ext;
	}
		
		return  null;
		
	}
	
	public static boolean isSupportedArchiveExtension(String extension){
		
		for(ArchivedFileExtensions exts: ArchivedFileExtensions.values()){
			if(exts.getExtension().equalsIgnoreCase(extension))
				return true;
		}
		
		return false;
	}
}
