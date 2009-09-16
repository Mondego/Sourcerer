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
package edu.uci.ics.sourcerer.codecrawler.util;

import edu.uci.ics.sourcerer.codecrawler.util.CertaintyUtils;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class CertaintyUtilsTest extends TestCase {

	public void testAddCertainty() {
		assertTrue(CertaintyUtils.addCertaintyToValue("Java", .907d).startsWith("Java?certainty=0.907"));
		assertTrue(CertaintyUtils.addCertaintyToValue("Java?", .907d).startsWith("Java?certainty=0.907"));
		assertTrue(CertaintyUtils.addCertaintyToValue("Java?version=1.4", .907d).startsWith("Java?version=1.4&certainty=0.907"));
		assertTrue(CertaintyUtils.addCertaintyToValue("Java?version=1.4&", .907d).startsWith("Java?version=1.4&certainty=0.907"));
	}
	
	public void testGetCertainty() {
		assertEquals(0.907d, CertaintyUtils.getCertainty("Java?certainty=0.907"));
		assertEquals(0.907d, CertaintyUtils.getCertainty("Java?version=1.4&certainty=0.907"));
		assertEquals(0.907d, CertaintyUtils.getCertainty("Java?certainty=0.907&version=1.4"));
		assertEquals(0.907d, CertaintyUtils.getCertainty("Java?version=1.4&&certainty=0.907&version=1.4"));
	}
}
