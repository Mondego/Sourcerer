package edu.uci.ics.sourcerer.lucene.tools;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;


public class IndexMergerTest extends BaseIndexingTestCase {

	protected void setUp() throws IOException {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCreateIndex() {
		String _iList = "test/resources/i2merge-list.txt";
		String _iDir = "/Users/shoeseal/DATA/merged";
		
		IndexMerger im = new IndexMerger();
		im.setFlushThreshold(1);
		im.setIndexDir(_iDir);
		try{
			im.initOldIndexPaths(new File(_iList));
		} catch (Exception e) { fail(); }
		
		im.createIndex();
		
		IndexReader ir = null; 
		try {
			ir = IndexReader.open(_iDir);
		} catch (IOException e) {
			
			fail("no index created");
			e.printStackTrace();
		}
		assertNotNull(ir);
		
		// i2merge.list has 3 lines
		//assertEquals(getDocSize() * 3, ir.numDocs());
	
	}

}
