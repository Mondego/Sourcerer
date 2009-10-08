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


/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public abstract class AbstractScmSourceRetriever implements ISourceRetriever {

	private String scmSourceFolder;

	protected void validateLocalFolder(String projectFolder) throws Exception {
		
		if (!projectFolder.endsWith(File.separator))
			this.scmSourceFolder = projectFolder + File.separator + Constants.getSourceFolderName();
		else
			this.scmSourceFolder = projectFolder + Constants.getSourceFolderName();
		
		if (! new File(this.scmSourceFolder).exists() ) {
			throw new Exception(
					this.scmSourceFolder
							+ " , the checkout destination folder, does not exist.");
		} 
	}

	/**
	 * only checks out if the localFolder is a valid sourcerer folder
	 */
	public boolean retreive(String sourceRetrieveExpression, String projectFolder) {

		try {
			validateLocalFolder(projectFolder);
			return checkout(sourceRetrieveExpression, projectFolder);
		} catch (Exception e) {
			// TODO Log this error
			// 		make this throwable; handle exception at client
			e.printStackTrace();
		}
		
		return false;

	}

	/**
	 * Override this to implement a SCM specific retriever. This method
	 * implements the behavior to check out contents from a remote SCM
	 * repository and dump the output of running checkout commands in the
	 * appropriate folder.
	 * 
	 * @param sourceRetrieveExpression
	 * @param projectFolder
	 * @return true if checkout succeeds false otherwise
	 */
	protected abstract boolean checkout(String sourceRetrieveExpression,
			String projectFolder);

	/**
	 * 
	 * @return location to store the status and info files after running scm
	 *         commands. The String has a trailing File.separator
	 */
	protected String getCheckoutFolder() {
		return this.scmSourceFolder;
	}

}
