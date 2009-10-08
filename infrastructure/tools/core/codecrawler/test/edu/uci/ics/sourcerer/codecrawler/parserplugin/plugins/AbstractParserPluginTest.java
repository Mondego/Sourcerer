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
package edu.uci.ics.sourcerer.codecrawler.parserplugin.plugins;

import junit.framework.TestCase;
import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.parser.Document;
import edu.uci.ics.sourcerer.codecrawler.parser.IDocumentParser;
import edu.uci.ics.sourcerer.codecrawler.parser.impl.DocumentParser;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.IParserPluginIdGenerator;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPluginLoadException;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPluginManager;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 23, 2009
 *
 */
public abstract class AbstractParserPluginTest extends TestCase {
	ParserPluginManager pluginMgr;
	IDocumentParser docParser;

	public void setUp() throws ParserPluginLoadException {

		pluginMgr = new ParserPluginManager();
		pluginMgr.setIdGenerator(new IParserPluginIdGenerator() {
			public long getNewHitId() {
				return 0;
			}
		});
		String[] pluginNames = getPluginNames();
		pluginMgr.loadPlugins(pluginNames);

		docParser = new DocumentParser(pluginMgr, null);

	}
	
	public abstract String[] getPluginNames();

	public void printHits(Document doc) {
		for (Hit hit : doc.getHits()){
			System.out.println("id\t" + hit.getId());
			System.out.println("link\t" + hit.getCheckoutString());
			System.out.println("project-name\t" + hit.getProjectName());
			System.out.println("project-description\t" + hit.getProjectDescription());
			System.out.println("category\t" + hit.getProjectCategory());
			System.out.println("lic\t" + hit.getProjectLicense());
			System.out.println("lang\t" + hit.getLanguage());
			System.out.println("version\t" + hit.getVersion());
			System.out.println("source\t" + hit.getSourceCode());
			System.out.println("rel-date\t" + hit.getReleaseDate());
			System.out.println("descp.\t" + hit.getDescription());
			System.out.println("container-url\t" + hit.getContainerUrl());
			System.out.println("keyw\t" + hit.getKeywords());
			System.out.println("file-ext.\t" + hit.getFileExtensions());
		}
			
	}
	
}
