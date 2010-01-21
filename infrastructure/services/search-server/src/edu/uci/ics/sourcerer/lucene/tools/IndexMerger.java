/**
 * @author <a href="sbajrach@ics.uci.edu">skb</a>
 *  created: Nov 30, 2007 
 */
package edu.uci.ics.sourcerer.lucene.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;


/**
 * 
 */
public class IndexMerger extends AbstractIndexCreatorWithRamDir {

	ArrayList<String> oldIndexPaths = new ArrayList<String>();
	int indexFolderCount;
	int luceneDocsCount;
	
	public void initOldIndexPaths(File indexDirsList) throws IOException, FileNotFoundException  {

		BufferedReader _reader = new BufferedReader(new FileReader(indexDirsList));
		
		oldIndexPaths.clear();
		
		String _line = _reader.readLine();
		while (_line != null) {
			
			if (_line.trim().length()>0) {
				oldIndexPaths.add(_line);
			}
			
			_line = _reader.readLine();
		}
	}
	
	
	@Override
	protected void calculateTotalDocuments() {
		totalDocuments = oldIndexPaths.size();
		System.out.println("Total index dirs listed: " + totalDocuments);
	}

	@Override
	protected void printProcessedDocumentsInfo() {
		System.out.println("Processed index dirs: " + indexFolderCount);
		System.out.println("Merged lucene documents: " + luceneDocsCount);
	}

	@Override
	protected void processDocuments() {
		
		indexFolderCount = 0;
		luceneDocsCount = 0;
		
		for(String singlePath : oldIndexPaths) {
			IndexReader ir;

			try {
				ir = IndexReader.open(singlePath);
				int docs = ir.numDocs();
				
				for(int i=0; i<docs; i++) {
					Document d = ir.document(i);
					
					// the numbers of folders processed so far is not what controls the flush
					// addDocument(indexFolderCount, d);
					
					luceneDocsCount++;
					
					// number of lucene documents processed in the buffer should control the flush
					addDocument(luceneDocsCount, d);
					
				}
				
				ir.close();
				
				indexFolderCount++;
				
			} catch (IOException e) {
				//TODO log
//				LoggerUtils.error("Cannot open (or close) index at: " + singlePath);
//				LoggerUtils.logException(this, e);
			}			
			
		}
	}

	@Override
	protected void validateDocumentProviders() {
		assert(oldIndexPaths.size()>0);
		
	}
	

}
