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

import java.text.MessageFormat;
import java.util.List;

import edu.uci.ics.sourcerer.search.HitFqnEntityId;
import edu.uci.ics.sourcerer.search.SourcererGateway;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 *
 */
public class MLTQueryGatewayTest extends TestCase {

	SourcererGateway g = SourcererGateway.getInstance("", "");
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testMLT(){
		
		String entityId = //"6821103";
				"3475569";
		
		
		
		System.out.println("--jdk--");
		long start = System.currentTimeMillis();
		List<HitFqnEntityId> jsim = g.searchMltViaJdkUse(entityId);
		long end = System.currentTimeMillis();
		assertNotNull(jsim);
		//assertEquals(15, jsim.size());
		for(HitFqnEntityId h: jsim){
			System.out.println(h.fqn);
		}
		System.out.println("time " +  formatElapsedTime(end-start) );
		
		System.out.println("--lib--");
		start = System.currentTimeMillis();
		List<HitFqnEntityId> libsim = g.searchMltViaLibUse(entityId);
		end = System.currentTimeMillis();
		assertNotNull(libsim);
		//assertEquals(15, libsim.size());
		for(HitFqnEntityId h: libsim){
			System.out.println(h.fqn);
		}
		System.out.println("time " +  formatElapsedTime(end-start) );
		
		// local disabled for now
//		System.out.println("--local--");
//		start = System.currentTimeMillis();
//		List<HitFqnEntityId> locsim = g.searchMltViaLocalUse(entityId);
//		end = System.currentTimeMillis();
//		assertNotNull(locsim);
//	//	assertEquals(15, locsim.size());
//		for(HitFqnEntityId h: locsim){
//			System.out.println(h.fqn);
//		}
//		System.out.println("time " +  formatElapsedTime(end-start) );
		
		
		
//		start = System.currentTimeMillis();
//		List<HitFqnEntityId> jlsim = g.searchMltViaJdkLibUsage(entityId);
//		end = System.currentTimeMillis();
//		assertNotNull(jlsim);
//		//assertEquals(15, jlsim.size());
//		for(HitFqnEntityId h: jlsim){
//			System.out.println(h.fqn);
//		}
//		System.out.println("time " +  formatElapsedTime(end-start) );
//		
//		System.out.println("");
//		System.out.println("=== code ====");
//		start = System.currentTimeMillis();
//		List<HitFqnEntityId> sim = g.searchMltViaLibUsage(entityId);
//		end = System.currentTimeMillis();
//		assertNotNull(sim);
//		//assertEquals(15, sim.size());
//		for(HitFqnEntityId h: sim){
//			System.out.println(h.fqn);
//			System.out.println(" ---- ");
//			System.out.println(g.getCode(h.entityId));
//		}
//		System.out.println("time " +  formatElapsedTime(end-start) );
//		System.out.println("=== code ====");
		System.out.println("\n\n===============");
		
		System.out.println("--jdk--");
		start = System.currentTimeMillis();
		System.out.println(g.mltSnamesViaJdkUse(entityId));
		end = System.currentTimeMillis();
		System.out.println("time " +  formatElapsedTime(end-start) );
		
		System.out.println("--lib--");
		start = System.currentTimeMillis();
		System.out.println(g.mltSnamesViaLibUse(entityId));
		end = System.currentTimeMillis();
		System.out.println("time " +  formatElapsedTime(end-start) );
		
		
		// local disabled for now
//		System.out.println("--local--");
//		start = System.currentTimeMillis();
//		System.out.println(g.mltSnamesViaLocalUse(entityId));
//		end = System.currentTimeMillis();
//		System.out.println("time " +  formatElapsedTime(end-start) );
		
		
	}
	
	public void _testAllEntities(){
		for(int i=0; i<6000000; i++){
			
			String eId = i+"";
			System.out.println(eId);
			System.out.println("--jdk--");
			long start = System.currentTimeMillis();
			System.out.println(g.mltSnamesViaJdkUse(eId));
			long end = System.currentTimeMillis();
			System.out.println(formatElapsedTime(end-start) );
			
			System.out.println("--lib--");
			start = System.currentTimeMillis();
			System.out.println(g.mltSnamesViaLibUse(eId));
			end = System.currentTimeMillis();
			System.out.println(formatElapsedTime(end-start) );
			
			// local disabled for now
//			System.out.println("--local--");
//			start = System.currentTimeMillis();
//			System.out.println(g.mltSnamesViaLocalUse(eId));
//			end = System.currentTimeMillis();
//			System.out.println(formatElapsedTime(end-start) );
		}
		
	}
	
	public String formatElapsedTime(long elapsedTime) {
        long millis = elapsedTime;
        millis = millis>1000?elapsedTime % 1000:millis;
        long seconds = millis / 1000;
        seconds = seconds > 60 ? seconds % 60 : seconds;
        long minutes = seconds / 60;
        Object[] PARAMS = {
              new Long(minutes), new Long(seconds), new Long(millis)
        };

        String format = "{2} msec";
        if ( seconds > 0 ) format = "{1} seconds "+format;
        if ( minutes > 0 ) format = "{0} minutes"+format;
        return MessageFormat.format(format, PARAMS);
    }

}
