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


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.junit.Assert;
import org.junit.Test;

import edu.uci.ics.sourcerer.search.analysis.EidToSimSnamesTransformer;
import edu.uci.ics.sourcerer.search.analysis.FqnCleaningTransformer;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 23, 2009
 * 
 */
public class EidToSimSnamesTransformerTest {

	@Test
	public void removeInit() {
		List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
		
		fields.add(getField("sim_fqns_via_jdkLib_use", "sim_sname_contents_via_jdkLib_use", "true"));
		fields.add(getField("simTC_fqns_via_jdkLib_use", "simTC_sname_contents_via_jdkLib_use", "true"));
		fields.add(getField("simHD_fqns_via_jdkLib_use", "simHD_sname_contents_via_jdkLib_use", "true"));
		HashMap<String,String> c = new HashMap<String,String>();
		c.put("column","code_text");
		c.put("name","full_text");
		fields.add(c);
		
		HashMap<String,String> ef1 = new HashMap<String,String>();
		ef1.put("sim-server-url","http://localhost:8080/similarity-server");
		ef1.put("mlt-server-url","http://localhost:8983/solr/scs/mlt");
		ef1.put("http-timeout","1");
		
		Context context = AbstractDataImportHandlerTest.getContext(null, null,
				null, Context.FULL_DUMP, fields, ef1);
		
		Map<String, Object> src = new HashMap<String, Object>();
		
		 src.put("etype", "CONSTRUCTOR");
		 src.put("eid", new BigInteger("42958433"));
		// src.put("eid", new BigInteger("42959451"));
		
//		src.put("etype", "METHOD");
//		src.put("eid", new BigInteger("42983551"));
		
		
		List<Map<String,Object>> rows = new ArrayList<Map<String,Object>>();
		rows.add(src);
		
		Map<String, Object> result = (Map<String, Object>) new EidToSimSnamesTransformer().transformRow(
				src,
				context);
		
		Assert.assertNotNull(result);
		
	}
	
	public static Map<String, String> getField(String col, String name, String fix) {
		HashMap<String, String> vals = new HashMap<String, String>();
		vals.put("column", col);
		vals.put("name", name);
		vals.put("fix-init", fix);
		return vals;
	}
}
