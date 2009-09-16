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
package edu.uci.ics.sourcerer.codecrawler.db.memimpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Hashtable;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.db.IHitGateway;
import edu.uci.ics.sourcerer.codecrawler.util.CrawlerProperties;
import edu.uci.ics.sourcerer.codecrawler.util.StringFormatUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class HitGateway implements IHitGateway {

	public static final String PROP_OUTPUTFILE_KEY = ".db.memimpl.hitGateway.outputfile";
	
	protected Hashtable<Long, Hit> table;
	protected HashMap<String, Hit> downloadStringMap;
	private long newId;
	private long nextId;
	
	protected PrintStream output;

	public HitGateway() {
		table = new Hashtable<Long, Hit>();
		downloadStringMap = new HashMap<String, Hit>();
		String filename = CrawlerProperties.getInstance().getProperty(CrawlerProperties.CRAWLER + ".db.memimpl.hitGateway.outputfile", "hits.txt");
		try {
			output = new PrintStream(new FileOutputStream(filename));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("HitGateway cannot open output file (" + filename + ")");
		}
		
		printFileHeader();
	}
	
	protected void printFileHeader()
	{
		output.println("ID\tDate\tLink\tProject name\tProject description\tProject category\tLicense\tLanguage\tVersion\tSource\tRelease date\t" +
		"Description\tContainer URL\tKeywords\tFile Extensions");
		output.println();
	}

	public Hit getNextHit() {
		synchronized(this) {
			nextId++;
			while ((nextId < newId) && (table.get(new Long(nextId)) == null))
				nextId++;
			return table.get(new Long(nextId));
		}
	}

	public Hit getHit(long id) {
		synchronized(this) {
			return table.get(new Long(id));
		}
	}

	public Hit getHit(String downloadString) {
		synchronized(this) {
			return downloadStringMap.get(downloadString);
		}
	}

	public void deleteHit(long id) {
		synchronized(this) {
			table.remove(new Long(id));
		}
	}

	public void saveHit(Hit hit) {
		/*
		Hit oldHit = downloadStringMap.get(hit.getCheckoutString());
		if (oldHit != null) {
			hit.setId(hit.getId());
		}
		 */
		
		synchronized(this) {
			table.put(new Long(hit.getId()), hit);
			downloadStringMap.put(hit.getCheckoutString(), hit);
		}
		
		output.println(String.format("%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
				hit.getId(), StringFormatUtils.formatDate("yyyy-MMM-dd", hit.getHitDate()),
				hit.getCheckoutString(), hit.getProjectName(), hit.getProjectDescription(),
				hit.getProjectCategory(), hit.getProjectLicense(), hit.getLanguage(),
				hit.getVersion(), hit.getSourceCode(), hit.getReleaseDate(), hit.getDescription(),
				hit.getContainerUrl(), hit.getKeywords(), hit.getFileExtensions()));
		output.flush();
	}

	public long getNewId() {
		synchronized(this) {
			newId++;
			while (table.get(new Long(newId)) != null)
				newId++;
			return newId;
		}
	}

}