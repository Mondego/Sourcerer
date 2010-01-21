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
package org.apache.solr.handler.dataimport;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.junit.Assert;
import org.junit.Test;

import edu.uci.ics.sourcerer.search.analysis.FqnCleaningTransformer;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 23, 2009
 * 
 */
public class TransformerTest {

	@Test
	public void removeInit() {
		List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
		fields.add(getField("col1", "colname", "true", "false"));
		Context context = AbstractDataImportHandlerTest.getContext(null, null,
				null, 0, fields, null);
		Map<String, Object> src = new HashMap<String, Object>();
		String s = "(1UNKNOWN)a.bb.cc.<init>(x.v.cc)";
		src.put("col1", s);
		Map<String, Object> result = new FqnCleaningTransformer().transformRow(src,
				context);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals("(1UNKNOWN)a.bb.cc.(x.v.cc)", result.get("col1"));
		
	}
	
	@Test
	public void removeInitAndClean() {
		List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
		fields.add(getField("col1", "colname", "true", "true"));
		Context context = AbstractDataImportHandlerTest.getContext(null, null,
				null, 0, fields, null);
		Map<String, Object> src = new HashMap<String, Object>();
		String s = "(1UNKNOWN)a.bb.cc.<init>(x.v.cc)";
		src.put("col1", s);
		Map<String, Object> result = new FqnCleaningTransformer().transformRow(src,
				context);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals("a.bb.cc.(x.v.cc)", result.get("col1"));
		
	}

	public static Map<String, String> getField(String col, String name, String fi, String cf) {
		HashMap<String, String> vals = new HashMap<String, String>();
		vals.put("column", col);
		vals.put("name", name);
		vals.put("fix-init", fi);
		vals.put("clean-fqn", cf);
		return vals;
	}
}
