/**
 * @author <a href="sbajrach@ics.uci.edu">skb</a>
 *  created: Dec 3, 2007 
 */
package edu.uci.ics.sourcerer.lucene.tools;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import junit.framework.TestCase;

/**
 * Creates two sample index files
 */
public abstract class BaseIndexingTestCase extends TestCase {
	
	protected String[] keywords = { "1", "2", "3" };

	protected String[] unindexed = { "Netherlands", "Italy", "Nepal" };

	protected String[] unstored = { "Amsterdam has lots of bridges",
			"Venice has lots of canals", "Kathmandu has lots of rivers" };

	protected String[] text = { "Amsterdam", "Venice", "Kathmandu" };

	protected Directory dir1, dir2, dir3;

	protected void setUp() throws IOException {

//		String indexDir = System.getProperty("java.io.tmpdir", "tmp")
//				+ System.getProperty("file.separator") + "index-dir";

		String indexDir1 = "./src/test/resources/index.dir.1";
		String indexDir2 = "./src/test/resources/index.dir.2";
		String indexDir3 = "./src/test/resources/index.dir.3";
		
		dir1 = FSDirectory.getDirectory(indexDir1);
		dir2 = FSDirectory.getDirectory(indexDir2);
		dir3 = FSDirectory.getDirectory(indexDir3);

		addDocuments(dir1);
		addDocuments(dir2);
		addDocuments(dir3);
		
	}

	protected void addDocuments(Directory dir) throws IOException {

		IndexWriter writer = new IndexWriter(dir, getAnalyzer(), true);
		writer.setUseCompoundFile(isCompound());

		for (int i = 0; i < keywords.length; i++) {
			Document doc = new Document();
			doc.add(new Field("id", keywords[i], Field.Store.YES,
					Field.Index.UN_TOKENIZED));
			doc.add(new Field("country", unindexed[i], Field.Store.YES,
					Field.Index.NO));
			doc.add(new Field("contents", unstored[i], Field.Store.NO,
					Field.Index.UN_TOKENIZED));
			doc.add(new Field("city", text[i], Field.Store.NO,
					Field.Index.UN_TOKENIZED));
			writer.addDocument(doc);
		}
		writer.optimize();
		writer.close();
	}

	protected Analyzer getAnalyzer() {
		return new SimpleAnalyzer();
	}

	protected boolean isCompound() {
		return true;
	}
	
	protected int getDocSize() {
		return keywords.length;
	}

}
