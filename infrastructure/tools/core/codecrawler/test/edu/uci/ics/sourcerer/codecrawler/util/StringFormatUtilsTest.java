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

import java.util.Date;

import edu.uci.ics.sourcerer.codecrawler.util.StringFormatUtils;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class StringFormatUtilsTest extends TestCase {

	public void testDate() {
		Date before = StringFormatUtils.parseDateIn("MM/d/yyyy", "09/03/2006");
		Date date = StringFormatUtils.parseDateIn("MMM dd, yyyy", "September 4, 2006");
		Date after = StringFormatUtils.parseDateIn("MMM/dd/yyyy", "September/5/2006");
		assertNotNull(before);
		assertNotNull(date);
		assertNotNull(after);
		assertTrue(before.before(date));
		assertTrue(after.after(date));
		
		assertEquals("Sep03,06", StringFormatUtils.formatDate("MMMdd,yy", before));
		assertEquals("2006-Sep-03", StringFormatUtils.formatDate("yyyy-MMM-dd", before));
		assertEquals("2006-September-03", StringFormatUtils.formatDate("yyyy-MMMM-dd", before));
	}
}
