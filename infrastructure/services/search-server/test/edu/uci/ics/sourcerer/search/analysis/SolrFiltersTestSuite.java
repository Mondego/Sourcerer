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
package edu.uci.ics.sourcerer.search.analysis;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 *
 */
public class SolrFiltersTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for edu.uci.ics.sourcerer.search.analysis");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestCamelCase.class);
		suite.addTestSuite(TestLetterDigitSplitFilter.class);
		suite.addTestSuite(FqnFilterFactoryTest.class);
		suite.addTestSuite(FqnFilterFactoryTest2.class);
		suite.addTestSuite(ConstructorTypeParamArrayTest.class);
		//$JUnit-END$
		return suite;
	}

}
