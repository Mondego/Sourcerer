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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 * 
 * Note: Class copied from older Sourcerer code, originally implemented
 *       by Trung Ngo
 */
public class FileUtils {

	//private static final boolean debugEnabled = LoggerUtils.isDebugEnabled(FileUtils.class);

	/**
	 * determines if a file have the specified extension
 	 * @param file the source file
	 * @param ext the file extension (with no '.')
	 * @return
	 */
	public static final boolean isFileTypeOf(File file, String ext) {

		return isFileTypeOf(file.getName(), ext);
	}

	public static final boolean isFileTypeOf(String fileName, String[] exts) {
		for (int i=0; i < exts.length; i++) {
			if (isFileTypeOf(fileName, exts[i]))
				return true;
		}

		return false;
	}

	public static final String getRelativePath(String parent, String child) {
		// normalize path
		parent = parent.replace('\\','/');
		child = child.replace('\\','/');

		if (!child.startsWith(parent)) {
			throw new RuntimeException("the dir: " + child + " is not a sub dir of " + parent);
		}

		int k;
		if (parent.endsWith("/") || parent.endsWith("\\"))
			k = 0;
		else
			k = 1;
		return child.substring(parent.length() + k);
	}
	
	
	public static final boolean isFileTypeOf(String fileName, String ext) {
		return fileName.endsWith("." + ext);
	}

	public static final String getExtension(String fileName) {

		int k = fileName.lastIndexOf('.');
		if (k < 0)
			return "";
		else
			return fileName.substring(k + 1);

	}



	/**
	 * determines if a file exists
	 */
	public static final boolean exists(String fileName) {
		return new File(fileName).exists();
	}

	/**
	 * get the input stream of a file
	 * @param source
	 * @return an instance of InputStream class
	 */
	public static InputStream getInputStream(File source) {
		try {
			return new FileInputStream(source);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * get the input stream of file
	 * @param fileName the source file name
	 * @return an instance of InputStream class
	 */
	public static InputStream getInputStream(String fileName) {


		return getInputStream(new File(fileName));
	}


	/**
	 * Checks whether a given file is a symbolic link.
	 *
	 * <p>It doesn't really test for symbolic links but whether the
	 * canonical and absolute paths of the file are identical - this
	 * may lead to false positives on some platforms.</p>
	 *
	 * @param parent the parent directory of the file to test
	 * @param name the name of the file to test.
	 */
	public static boolean isSymbolicLink(File parent, String name)	throws IOException {

		File resolvedParent = new File(parent.getCanonicalPath());
		File toTest = new File(resolvedParent, name);
		return !toTest.getAbsolutePath().equals(toTest.getCanonicalPath());
	}


	/**
	 * TODO comment this
	 * @param parentDir
	 * @param child
	 * @return
	 */
	public static String makePath(String parentDir, String child) {
		File file = new File(parentDir, child);
		return file.getAbsolutePath();
	}


	/**
	 * delete an existing directory and its content
	 * @param dir the directoryto delete
	 */
	public static void deleteDir(File dir) {

		try {
			recursiveDeleteDir(dir);

		} catch (IOException e) {
			throw new RuntimeException("Unable to delete : "  + dir.getAbsolutePath());
		}
	}

	/**
	 * delete an existing directory (recursively delete all of its content too)
	 * @param dir the directory to delete
	 */
	private static void recursiveDeleteDir(File dir) throws IOException {
//		if (debugEnabled) {
//			LoggerUtils.debug(FileUtils.class, "---DeleteDir:" + dir.getAbsolutePath());
//		}

		if (!dir.exists()) {
			// cannot delete an empty directory
			throw new RuntimeException("Cannot delete an non-exist directory: " + dir.getAbsolutePath() );
		}else {

			if (!dir.isDirectory()) {
				// the current file is the file
				if (!dir.delete()) {

					throw new RuntimeException ("Unable to delete this file: " +
						dir.getAbsolutePath());
				}

			} else {
				// recursively remove all sub dirs
				String[] dirList = dir.list();

				if (dirList != null) {

					for (int i = 0; i < dirList.length; i++) {
						deleteDir(new File(dir.getAbsolutePath(), dirList[i]));
					}
				}

				// delete the directory
				dir.delete();

			}
		}
	}
}
