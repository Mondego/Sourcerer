package edu.uci.ics.sourcerer.search;

import java.util.List;

import junit.framework.TestCase;

public class SimSnamesViaSimServerTest extends TestCase{
	SourcererGateway g = SourcererGateway.getInstance(
			"http://kathmandu.ics.uci.edu:8984/solr/scs/mlt", 
			"",
			"",
			"jdbc:mysql://tagus.ics.uci.edu:3306/sourcerer_eclipse",
			System.getProperty("sourcerer.db.user"),
			System.getProperty("sourcerer.db.password"));

	public void testSim() {
		
		g.setSimServerUrl("http://mine8.ics.uci.edu:9010/similarity-server"
				// "http://localhost:8080/similarity-server"
				);
		
		String entityId = "45138875"; 
			// "6821103";
			// "42958433";
			// "42958349";
			//"42958434";

		System.out.println("-sim tc-");
		long start = System.currentTimeMillis();
		String fqns = g.snamesViaSimEntitiesTC(entityId);
		long end = System.currentTimeMillis();
		assertNotNull(fqns);
		//assertTrue(fqns.length()>0);
		// assertEquals(15, jsim.size());
		for(String f: fqns.split(" ")){
		System.out.println(f);
		}
		System.out.println("time " + Timeutil.formatElapsedTime(end - start));
		
		System.out.println("-sim hd-");
		start = System.currentTimeMillis();
		fqns = g.snamesViaSimEntitiesHD(entityId);
		end = System.currentTimeMillis();
		assertNotNull(fqns);
		//assertTrue(fqns.length()>0);
		// assertEquals(15, jsim.size());
		for(String f: fqns.split(" ")){
		System.out.println(f);
		}
		System.out.println("time " + Timeutil.formatElapsedTime(end - start));
	}
}
