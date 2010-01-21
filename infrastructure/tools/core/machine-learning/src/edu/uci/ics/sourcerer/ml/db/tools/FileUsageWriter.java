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
	final String USAGE_FILE = "usage.txt";
	
    File fqnFile;
    File usageFile;
	
    BufferedWriter fqnFileWriter;
    BufferedWriter usageFileWriter;
    
    public FileUsageWriter(String outputFolder){
    	this.outputFolder = outputFolder;
    }
    
	public void openFiles() throws IOException {
		if(!outputFolder.endsWith(File.separator))
			outputFolder = outputFolder + File.separator;
		
		fqnFile = new File(outputFolder + FQN_FILE);
		usageFile = new File(outputFolder + USAGE_FILE);
		
		assert !fqnFile.exists();
		assert !usageFile.exists();
		
		fqnFileWriter = new BufferedWriter(new FileWriter(fqnFile));
		usageFileWriter = new BufferedWriter(new FileWriter(usageFile));
		
	}
	
	public void closeFiles() throws IOException{
		fqnFileWriter.close();
		usageFileWriter.close();
		fqnFileWriter = null;
		usageFileWriter = null;
	}
	
	@Override
	public void writeFqnId(long fqnId, String fqn) {
		try {
			fqnFileWriter.write(fqnId + "\t" + fqn);
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

}
