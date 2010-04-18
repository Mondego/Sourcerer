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
package edu.uci.ics.sourcerer.ml.db.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 8, 2009
 * 
 */
public class FileUsageWriter implements IUsageWriter {

	String outputFolder;
	final String FQN_FILE = "fqns.txt";
	final String ENTITY_ALLFQNCOUNT_FILE = "entity_allfqn_count.txt";

	boolean filteredUsage = false;

	public String USAGE_FILE = "entity_eachfqn_usage.txt";

	File fqnFile;
	File usageFile;
	File entityAllUsedFqnCountFile;

	BufferedWriter fqnFileWriter;
	BufferedWriter usageFileWriter;
	BufferedWriter entityAllUsedFqnCountWriter;

	public FileUsageWriter(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public FileUsageWriter(String outputFolder, boolean filteredUsage2) {
		this.outputFolder = outputFolder;
		this.filteredUsage = filteredUsage2;
	}

	public void openFiles() throws IOException {
		if (!outputFolder.endsWith(File.separator))
			outputFolder = outputFolder + File.separator;

		if (!filteredUsage) {
			fqnFile = new File(outputFolder + FQN_FILE);
			assert !fqnFile.exists();
			fqnFileWriter = new BufferedWriter(new FileWriter(fqnFile));

			entityAllUsedFqnCountFile = new File(outputFolder
					+ ENTITY_ALLFQNCOUNT_FILE);
			assert !entityAllUsedFqnCountFile.exists();
			entityAllUsedFqnCountWriter = new BufferedWriter(new FileWriter(
					entityAllUsedFqnCountFile));
		}

		usageFile = new File(outputFolder + USAGE_FILE);
		assert !usageFile.exists();
		usageFileWriter = new BufferedWriter(new FileWriter(usageFile));

	}

	public void closeFiles() throws IOException {

		if (!filteredUsage) {
			fqnFileWriter.close();
			fqnFileWriter = null;

			entityAllUsedFqnCountWriter.close();
			entityAllUsedFqnCountWriter = null;
		}

		usageFileWriter.close();
		usageFileWriter = null;

	}

	@Override
	public void writeFqnId(long fqnId, String fqn, int count) {
		try {
			fqnFileWriter.write(fqnId + "\t" + fqn + "\t" + count);
			fqnFileWriter.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void writeUsage(long entityId, long fqnId, int useCount) {
		try {
			usageFileWriter.write(entityId + "\t" + fqnId + "\t" + useCount);
			usageFileWriter.newLine();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void writeNumFqnsUsed(long entityId, int count) {
		try {
			entityAllUsedFqnCountWriter.write(entityId + "\t" + count);
			entityAllUsedFqnCountWriter.newLine();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
