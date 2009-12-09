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
	final String USAGE_FILE_CLASS = "usage_class.txt";
	final String USAGE_FILE_METHOD = "usage_method.txt";
	
    File fqnFile;
    File usageFileClass;
    File usageFileMethod;
	
    BufferedWriter fqnFileWriter;
    BufferedWriter usageFileWriterClass;
    BufferedWriter usageFileWriterMethod;
    
    public FileUsageWriter(String outputFolder){
    	this.outputFolder = outputFolder;
    }
    
	public void openFiles() throws IOException {
		if(!outputFolder.endsWith(File.separator))
			outputFolder = outputFolder + File.separator;
		
		fqnFile = new File(outputFolder + FQN_FILE);
		usageFileClass = new File(outputFolder + USAGE_FILE_CLASS);
		usageFileMethod = new File(outputFolder + USAGE_FILE_METHOD);
		
		assert !fqnFile.exists();
		assert !usageFileClass.exists();
		assert !usageFileMethod.exists();
		
		fqnFileWriter = new BufferedWriter(new FileWriter(fqnFile));
		usageFileWriterClass = new BufferedWriter(new FileWriter(usageFileClass));
		usageFileWriterMethod = new BufferedWriter(new FileWriter(usageFileMethod));
		
	}
	
	public void closeFiles() throws IOException{
		fqnFileWriter.close();
		usageFileWriterClass.close();
		usageFileWriterMethod.close();
		fqnFileWriter = null;
		usageFileWriterClass = null;
		usageFileWriterMethod = null;
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
	public void writeUsage(long entityId, long fqnId, int useCount, boolean isClass) {
		try {
			
			if(isClass){
				usageFileWriterClass.write(entityId + "\t" + fqnId + "\t" + useCount);
				usageFileWriterClass.newLine();
			} else {
				usageFileWriterMethod.write(entityId + "\t" + fqnId + "\t" + useCount);
				usageFileWriterMethod.newLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
