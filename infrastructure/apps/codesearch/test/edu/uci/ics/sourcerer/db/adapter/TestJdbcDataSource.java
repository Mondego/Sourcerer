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
package edu.uci.ics.sourcerer.db.adapter;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import edu.uci.ics.sourcerer.db.adapter.JdbcDataSource;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 *
 */
public class TestJdbcDataSource extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGetEntities(){
		JdbcDataSource dataSource = new JdbcDataSource();
	    Properties p = new Properties();
	    p.put("driver", "com.mysql.jdbc.Driver");
	    p.put("url", "jdbc:mysql://tagus.ics.uci.edu:3306/sourcerer");
	    p.put("user", "sourcerer-public");
	    p.put("password", "");


	    dataSource.init(p);
	    Iterator<Map<String, Object>> i = dataSource
	            .getData("select * from entities where project_id='2568'");
	    int count = 0;
	    
	    BigInteger entity_id = null;
	    String fqn = null;
	    String entity_type = null;
	    
	    while (i.hasNext()) {
	      Map<String, Object> map = i.next();
	      entity_id = (BigInteger) map.get("entity_id");
	      entity_type = (String) map.get("entity_type");
	      fqn = (String) map.get("fqn");
	      System.out.println(entity_id.toString() + " " + entity_type + " " + fqn);
	      count++;
	    }
	    
	    Assert.assertEquals(1261, count);
	    
	    LinkedList<String> l = new LinkedList<String>();
	    l.add("1");
	    l.add("2");
	    l.add("3");
	    System.out.println(l.toString());
	    
//	    Assert.assertEquals(Float.class, msrp.getClass());
//	    Assert.assertEquals(Long.class, trim_id.getClass());
	}

}
