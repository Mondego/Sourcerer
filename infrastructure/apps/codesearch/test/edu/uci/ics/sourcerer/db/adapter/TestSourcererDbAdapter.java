///*
// * Sourcerer: An infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// * 
// */
//package edu.uci.ics.sourcerer.db.adapter;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.Set;
//
//import edu.uci.ics.sourcerer.db.adapter.client.Entity;
//import edu.uci.ics.sourcerer.scs.client.ERTables;
//
//
//
//import junit.framework.TestCase;
//
///**
// * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
// * @created Sep 10, 2009
// *
// */
//public class TestSourcererDbAdapter extends TestSourcererDbAdapterBase {
//
//	String[] hitEids = new String[]{
//			"5804931",
//			"6934165",
//			"6934164",
//			"1204509",
//			"1204495",
//			"6871915",
//			"6871916",
//			"389052",
//			"6871912",
//			"6790250",
//			"6790214",
//			"6790215",
//			"6790216",
//			"6790217"};
//	
//	
//	// unzip file folder
//	String[] hitEids2 = new String[]{
//			"2603439", "1483927", "4423048",
//			"6880117", "2436647", "1561464", "2432998", "6964504", "3017046",
//			"2603438" };
//	
//	// unizip file folder
//	String[] hitEids3 = new String[]{
//			"1483927"};
//	
//	
//	protected void setUp() throws Exception {
//		super.setUp();
//		
//	    dba.setDataSource(dataSource);
//	}
//
//	protected void tearDown() throws Exception {
//		super.tearDown();
//	}
//	
//	public void testDbAdapterForERTables(){
//		
//	}
//	
//	private List<String> convertSetToList(Set<String> set) {
//	    ArrayList<String> al = new ArrayList<String>();
//	    
//	    for(String s: set){
//	    	al.add(s);
//	    }
//	    
//	    return al;
//	}
//	
//	
////	public void testDbAdapter(){
////		
////	    List<Entity> el = dba.getUsedJdkEntities();
////	    assertNotNull(el);
////	    System.out.println("== JDK ==");
////	    printEntities(el);
////	    
////	    el = dba.getUsedLibEntities();
////	    assertNotNull(el);
////	    System.out.println("== LIBRARIES ==");
////	    printEntities(el);
////	    
////	    System.err.print("eid: " + el.get(0).fqn + " used by: ");
////	    for(Long l: new LinkedList<Long>(dba.getUsersInHits(el.get(0)))){
////	    	System.err.print(l.longValue() + " ");
////	    }
////	    System.out.println("");
////	    
////	    el = dba.getUsedLocalEntities();
////	    assertNotNull(el);
////	    System.out.println("== LOCAL ==");
////	    printEntities(el);
////	    
////	    System.err.println("=========");
////	    
////	    printEntities(dba.getUsedEntities());
////	    
////	}
////	
////	private void printEntities(List<Entity> el){
////		for(Entity e : el){
////	    	Entity parent = dba.getParent(e);
////	    	String pfqn = "null";
////	    	if(parent != null) pfqn = parent.fqn; 
////	    	System.out.println(
////	    			e.entityId + " " + 
////	    			e.type.name() + " " + 
////	    			dba.getUseCount(e) + " " + 
////	    			e.useCount + "<-- " +
////	    			e.parentId + "<-- " +
////	    			pfqn + " " + 
////	    			e.fqn);
////	    }
////	}
//	
//	public void _test1(){
//		Set<Long> s = new HashSet<Long>();
//		s.add(new Long(122));
//		assertTrue(s.contains(Long.parseLong("122")));
//		
//		Map<Long, String> m = new HashMap<Long, String>();
//		m.put(new Long(122), "value 122");
//		assertEquals("value 122", m.get(Long.parseLong("122")));
//		
//	}
//
//}
