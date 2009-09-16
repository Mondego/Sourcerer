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
package edu.uci.ics.sourcerer.codecrawler.network;

import java.io.InputStream;
import java.io.InputStreamReader;

import edu.uci.ics.sourcerer.codecrawler.network.NetworkInputStreamFactory;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class NetworkInputStreamFactoryTest extends TestCase {

	private char[] buffer = new char[1024];

	public void test() {
		try {
			System.out.println("Dumping content:\n------------------------------");
			InputStream bufferStream =
				NetworkInputStreamFactory.getBufferedInputStream(new UrlString("http://www.yahoo.com"));
			InputStreamReader reader = new InputStreamReader(bufferStream);
			int charsRead;
			int totalRead = 0;
			while ((charsRead = reader.read(buffer)) >= 0) {
				for (int i = 0; i < charsRead; i++)
					System.out.print(buffer[i]);
				//System.out.println(charsRead);
				totalRead += charsRead;
			}
			System.out.println("Total bytes: " + totalRead);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
