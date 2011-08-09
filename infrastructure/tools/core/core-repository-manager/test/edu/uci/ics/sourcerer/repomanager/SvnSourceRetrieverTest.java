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
//import junit.framework.TestCase;
//import java.io.File;
//
///**
// * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
// * @created Jan 12, 2009
// *
// */
//public class SvnSourceRetrieverTest extends TestCase {
//	
//	
//	public void testSvnInfo(){
//		String svncoFolder = "./test/resources/svnco";
//		
//		deleteDir(new File(svncoFolder + "/content"));
//		new File(svncoFolder + "/svnstat.properties").delete();
//		new File(svncoFolder + "/content").mkdir();
//		
//		String link = 
//			/// will not work with credentials inlined here
//			//   "svn checkout https://msrp.dev.java.net/svn/msrp/trunk --username sourcererbot --password sourcerer***";
//			
//			/// works with guest guest (hardcoded inside the retriever); trouble with certificate
//			//"svn checkout https://msrp.dev.java.net/svn/msrp/trunk"; 
//			
//			/// works with guest guest
//			// "svn co http://svn.apache.org/repos/asf/commons/proper/io/trunk/src/main/"; 
//		
//			// "svn co https://agentopia.svn.sourceforge.net/svnroot/agentopia agentopia";
//			
//			 "svn checkout https://pdftable.dev.java.net/svn/pdftable pdftable --username username";
//			
//			// "svn co https://j-wings.svn.sourceforge.net/svnroot/j-wings j-wings";
//			//"svn co https://itext.svn.sourceforge.net/svnroot/itext itext";
//			// "svn checkout  http://orzjisp.googlecode.com/svn/trunk/ orzjisp-read-only";
//			// "svn co https://cml.svn.sourceforge.net/svnroot/cml/chemdraw/branches/chemdraw-0.1/src/main/java/org/xmlcml/cml/chemdraw/components/";
//			//"svn co https://cml.svn.sourceforge.net/svnroot/cml cml";
//		
//			// "svn co http://turanar.googlecode.com/svn2/trunk";
//			
//			/* has no trunk, just branches*/
//			// "svn checkout  http://ganttproject.googlecode.com/svn/trunk/ ganttproject-read-only";
//		
//			// has multiple trunks inside subprojects
//			// "svn checkout  http://creativecrew.googlecode.com/svn/trunk/ folder";
//			
//		SvnSourceRetriever r = new SvnSourceRetriever();
//		r.retreive(link, svncoFolder);
//	}
//	
//	// Deletes all files and subdirectories under dir.
//    // Returns true if all deletions were successful.
//    // If a deletion fails, the method stops attempting to delete and returns false.
//    public static boolean deleteDir(File dir) {
//        if (dir.isDirectory()) {
//            String[] children = dir.list();
//            for (int i=0; i<children.length; i++) {
//                boolean success = deleteDir(new File(dir, children[i]));
//                if (!success) {
//                    return false;
//                }
//            }
//        }
//    
//        // The directory is now empty so delete it
//        return dir.delete();
//    } 
//}
