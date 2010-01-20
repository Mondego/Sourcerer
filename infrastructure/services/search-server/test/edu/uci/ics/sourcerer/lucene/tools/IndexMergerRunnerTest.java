/**
 * @author <a href="sbajrach@ics.uci.edu">skb</a>
 *  created: Dec 3, 2007 
 */
package edu.uci.ics.sourcerer.lucene.tools;

import junit.framework.TestCase;

/**
 * 
 */
public class IndexMergerRunnerTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link edu.uci.ics.sourcerer.lucene.tools.IndexMergerRunner#execute()}.
	 */
	public void testExecute() {
		String[] options = new String[]{
				"-idirlist", "./src/test/resources/i2merge.list", 
				"-newIndexdir", "./src/test/resources/index.dir.merged", 
				"-flush", "4"};
		(new IndexMergerRunner(options)).execute();
	}

}
