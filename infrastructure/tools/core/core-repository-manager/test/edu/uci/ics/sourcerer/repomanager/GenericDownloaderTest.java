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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class GenericDownloaderTest {

	/**
	 * Test method for {@link edu.uci.ics.sourcerer.repomanager.GenericDownloader#download(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testDownload() {
		
		String sourceUrl = 
		//	"https://swingx.dev.java.net/files/documents/2981/51057/swingx-2007_02_18-src.zip";
		//	"https://javatv-developers.dev.java.net/files/documents/9660/119814/PbpGameKColors.zip";
		//  "https://openinstaller.dev.java.net/files/documents/6533/67851/openInstaller-0.9.4-src.tar.gz";
		//	"https://DataForm.dev.java.net/files/documents/5287/46467/dataform-20061221.jar";
		
			"https://XtremePC.dev.java.net/files/documents/2834/11152/file_11152.dat/XtremePC%201.0.6.zip";
		
		// sourceforge url looks like this
		//"http://downloads.sourceforge.net/aisoccer/aisoccer_0.8_src_template.zip?use_mirror=internap";
		// need to remove the mirror information to make it work with the generic downloader	
		//	"http://downloads.sourceforge.net/aisoccer/aisoccer_0.8_src_template.zip"; // ?use_mirror=internap";
		
		// Or, convert
		// "http://downloads.sourceforge.net/aspvcs/aspvcs_0.1_alpha.zip?use_mirror=internap";
		//	into a URL like this:
		// "http://internap.dl.sourceforge.net/sourceforge/aspvcs/aspvcs_0.1_alpha.zip"; 
		
		
		//String sourceUrlJar = "https://easyframe.dev.java.net/files/documents/2111/43887/easyframe-0.6.jar";
		
		String projectFolder = "./test/resources/fromurl";
		
		GenericDownloader _d = new GenericDownloader();
		_d.download(sourceUrl, projectFolder);
		// _d.download(sourceUrlJar, projectFolder);
	}

}
