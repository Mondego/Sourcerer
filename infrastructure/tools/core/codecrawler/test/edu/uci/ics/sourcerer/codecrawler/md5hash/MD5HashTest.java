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
package edu.uci.ics.sourcerer.codecrawler.md5hash;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.TestCase;
import edu.uci.ics.sourcerer.codecrawler.md5hash.MD5Hash;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.parser.Document;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class MD5HashTest extends TestCase {

	private static final String[] URLS = {
		"http://www.htmlhelp.com/reference/html40/",
		"http://www.htmlhelp.com/reference/html40/",
		"http://sourceforge.net/export/rss2_projnews.php?group_id=141424&rss_fulltext=1",
		"http://sourceforge.net/export/rss2_sfnews.php?group_id=1&rss_fulltext=1"
	};

	private static final boolean[][] SAME_CONTENT = {
//          0      1      2      3      4      5
		{  true,  true, false, false},
		{  true,  true, false, false},
		{ false, false,  true, false},
		{ false, false, false,  true}
	};

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFile() {
		try {
			FileInputStream input = new FileInputStream("test/resources/md5testmsg.txt");
			MD5Hash hash1 = MD5Hash.digest(input);
			System.out.println(hash1.toString());
			input.close();
			input = new FileInputStream("test/resources/md5testmsg.txt");
			MD5Hash hash2 = MD5Hash.digest(input);
			System.out.println(hash2.toString());
			input.close();
			assertTrue(hash1.equals(hash2));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test() {
		//System.out.println(MD5Hash.digest("The quick brown fox jumps over the lazy dog").toString() + "\n**********");
		//System.out.println(MD5Hash.digest("The quick brown fox jumps over the lazy cog").toString() + "\n**********");
		try {
			for (int i = 0; i < URLS.length; i++) {
				for (int j = i; j < URLS.length; j++) {
					UrlString url1 = new UrlString(URLS[i]);
					UrlString url2 = new UrlString(URLS[j]);
					Document doc1 =	Document.openDocument(url1);
					System.out.println(url1 + " : " + doc1.getHashcode().toString());
					Document doc2 = Document.openDocument(url2);
					System.out.println(url2 + " : " + doc2.getHashcode().toString());
					assertTrue(doc1.getHashcode().equals(doc2.getHashcode()) == SAME_CONTENT[i][j]);
					System.out.println("OK!\n----------------------------");
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
