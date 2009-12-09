package edu.uci.ics.sourcerer.ml.db.tools;

import org.junit.Test;

import edu.uci.ics.sourcerer.ml.db.tools.IUsageWriter;
import edu.uci.ics.sourcerer.ml.db.tools.UsageCalculator;


public class UsageCalculatorTest {
	@Test
	public void testUsageCalculator(){
		String jdkFile = "test/data.big/jdk-usage.txt";
		String jarFile = "test/data.big/jar-usage.txt";
		String fqnFile = "test/data.big/popular-fqn.txt";
		UsageCalculator c = new UsageCalculator();
		c.setWriter(new ConsoleWriter());
		
		c.init("jdbc:mysql://localhost/sourcerer", 
				"sourcerer", 
				"", 
				1,
				1, 
				jdkFile, jarFile, fqnFile);
		
		c.writeUsage();
	}
}

class ConsoleWriter implements IUsageWriter{

	@Override
	public void writeFqnId(long fqnId, String fqn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeUsage(long entityId, long fqnId, int useCount, boolean isClass) {
		System.out.println(entityId + "\t" + fqnId + "\t" + useCount + "\t" + isClass);
	}
	
}