package edu.uci.ics.sourcerer.ml.db.tools;

import java.io.IOException;

import org.junit.Test;

import edu.uci.ics.sourcerer.ml.db.tools.IUsageWriter;
import edu.uci.ics.sourcerer.ml.db.tools.FilteredUsageCalculator;


public class UsagePreCalculatorTest {
	@Test
	public void testUsageCalculator(){

		UsagePreCalculator c = new UsagePreCalculator();
		c.setWriter(new ConsoleWriter());
		
		c.init("jdbc:mysql://tagus.ics.uci.edu:3306/sourcerer_eclipse", 
				System.getProperty( "sourcerer.db.user")
				, 
				System.getProperty( "sourcerer.db.password"),
				UsagePreCalculator.EntityType.METHOD,
				true
				);
		c.writeUsage();
	}
	
	@Test
	public void testUsageCalculatorFile() throws IOException{

		String outputFolder = "test/data.test/";
		FileUsageWriter w = new FileUsageWriter(outputFolder);
		w.openFiles();
		UsagePreCalculator c = new UsagePreCalculator();
		
		c.setWriter(w);
		
		c.init("jdbc:mysql://tagus.ics.uci.edu:3306/sourcerer_eclipse", 
				System.getProperty( "sourcerer.db.user")
				, 
				System.getProperty( "sourcerer.db.password"),
				UsagePreCalculator.EntityType.METHOD,
				true
				);
		c.writeUsage();
		w.closeFiles();
	}
}

class ConsoleWriter implements IUsageWriter{

	

	@Override
	public void writeUsage(long entityId, long fqnId, int useCount) {
		System.out.print("--- entities --- ");
		System.out.println(entityId + "\t" + fqnId + "\t" + useCount);
	}

	@Override
	public void writeFqnId(long fqnId, String fqn, int countEntitiesUsing) {
		System.out.print("--- fqns     --- ");
		System.out.println(fqnId + "\t" + fqn + "\t" + countEntitiesUsing);
	}

	@Override
	public void writeNumFqnsUsed(long entityId, int count) {
		System.out.print("--- fqns cou --- ");
		System.out.println(entityId + "\t" + count);
	}
	
}