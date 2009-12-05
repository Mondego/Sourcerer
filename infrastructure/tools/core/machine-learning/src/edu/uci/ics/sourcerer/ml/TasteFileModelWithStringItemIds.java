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
package edu.uci.ics.sourcerer.ml;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.MemoryIDMigrator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.IDMigrator;

/**
 * @author <a href="mailto:bajracharya@gmail.com">Sushil Bajracharya</a>
 */
public class TasteFileModelWithStringItemIds extends FileDataModel {
	
	IDMigrator itemIDMigrator = new MemoryIDMigrator();
	
	public TasteFileModelWithStringItemIds(File dataFile)
			throws FileNotFoundException {
		super(dataFile);
	}
	
	protected long readItemIDFromString(String value){
		try {
			return itemIDMigrator.toLongID(value);
		} catch (TasteException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	

	public IDMigrator getIdMigrator(){
		  return this.itemIDMigrator;
	}

	
	
}
