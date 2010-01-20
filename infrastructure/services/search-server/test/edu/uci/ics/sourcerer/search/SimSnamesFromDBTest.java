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
package edu.uci.ics.sourcerer.search;

import junit.framework.TestCase;


/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 2, 2009
 *
 */
public class SimSnamesFromDBTest extends TestCase{
	public void testSimEntity(){
		SourcererGateway g = SourcererGateway.getInstance("", "",
				"jdbc:mysql://mondego.calit2.uci.edu:3307/sourcerer_test",
				System.getProperty( "sourcerer.db.user" ),
				System.getProperty( "sourcerer.db.password" ));
		String result = g.snamesViaSimEntitiesTC("598457");
		assertTrue(result.length()>0);
		System.out.println(result);
	}
}
