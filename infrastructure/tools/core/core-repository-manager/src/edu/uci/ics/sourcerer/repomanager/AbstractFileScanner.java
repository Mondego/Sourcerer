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
import java.io.IOException;


/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 * 
 * Note: Class copied from older Sourcerer code, originally implemented
 *       by Trung Ngo
 */
public abstract class AbstractFileScanner {

	public interface AcceptFileCommand {
		public void accept(File f, int depth);
	}

	protected AcceptFileCommand acceptFileCommand = null;

	/** the base directory where the filescanner starts scanning */
	private File baseDir;


	private int maxDepth = -1;	/* scan all subfolders */


	/**
	 * if acceptDir = true, the scanner will acceptFileCommand
	 * when it sees directory.
	 */
	private boolean acceptDir = false;

	/**
	 * indicates if the scanning algorithm should visit all directories
	 * pointed by symbolic links
	 */
	private boolean followSymbolicLinks = false;

	/**
	 *
	 */
	public AbstractFileScanner(File baseDir) {
		super();

		this.baseDir = baseDir;
	}


	/**
	 * return the file object if the file name is accepted
	 * @param file
	 * @return
	 */
	protected abstract File isAccepted(File dir, String file);


	/**
	 * scan all sub-directories of the base dir and get all
	 * files with that the call to isAccepted() method return true
	 *
	 */
	protected void scan() throws IOException {
		if (acceptFileCommand == null) {
			// TODO convert to ASSERT
			System.out.println("[ERROR] acceptFileCommand must be initialized!");
		}

		scanDir(baseDir, 0 /* initial depth */);
	}


	/**
	 * scan all sub dirs of the given directory
	 */
	private void scanDir(File dir, int depth) throws IOException {

		if (maxDepth >= 0 && depth >= maxDepth)
			// stop scanning because the current depth is above the limit
			return;


		// scan through all sub dirs and files
		String[] subDirOrFiles = dir.list();

		if (subDirOrFiles == null) {
			// the given dir param is not a directory
			String message = "IO error scanning directory: " + baseDir.getAbsolutePath();
			throw new IOException(message);

		} else {

			for (int i = 0; i < subDirOrFiles.length; i++) {
				String name = subDirOrFiles[i];

				boolean stopped = false;

				if (!followSymbolicLinks && FileUtils.isSymbolicLink(dir, name))
					stopped = true;

				if (!stopped) {
					File file = isAccepted(dir, name);

					if (file != null) {
						if (file.isDirectory()) {
							if (acceptDir)
								acceptFileCommand.accept(file, depth);

							scanDir(file, depth + 1	/* scan the subdir */);


						} else {
							//matchedFiles.add(file);
							acceptFileCommand.accept(file, depth);
						}

					}

				} // if (matched)

			} // for (...)
		} // if (subDirOrFiles ...)

	}


	/**
	 * @return
	 */
	public File getBaseDir() {
		return baseDir;
	}

	/**
	 * @return
	 */
	public boolean isFollowSymbolicLinks() {
		return followSymbolicLinks;
	}

	/**
	 * @param b
	 */
	public void setFollowSymbolicLinks(boolean b) {
		followSymbolicLinks = b;
	}

	/**
	 * @return
	 */
	public int getMaxDepth() {
		return maxDepth;
	}

	/**
	 * @param i
	 */
	public void setMaxDepth(int i) {
		maxDepth = i;
	}


	public boolean isAcceptDir() {
		return acceptDir;
	}


	public void setAcceptDir(boolean acceptDir) {
		this.acceptDir = acceptDir;
	}



}
