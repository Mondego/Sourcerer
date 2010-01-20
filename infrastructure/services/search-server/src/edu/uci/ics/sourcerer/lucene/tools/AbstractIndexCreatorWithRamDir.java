package edu.uci.ics.sourcerer.lucene.tools;

import java.io.IOException;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import edu.uci.ics.sourcerer.util.TimeUtil;

public abstract class AbstractIndexCreatorWithRamDir {

	private String indexDir;
	private int flushThreshold;
	private RAMDirectory ramDir;
	private FSDirectory fsDir;
	private IndexWriter fsWriter;
	private IndexWriter ramWriter;
	protected int totalDocuments;
	private long startTime;
	private long endTime;
	
//	protected static Logger logger = Logger.getLogger(AbstractIndexCreatorWithRamDir.class
//				.getPackage().getName());

	protected abstract void calculateTotalDocuments();
	protected abstract void processDocuments();
	protected abstract void validateDocumentProviders();
	protected abstract void printProcessedDocumentsInfo();

	public AbstractIndexCreatorWithRamDir() {
		super();
	}

	public void setIndexDir(String indexDir) {
		this.indexDir = indexDir;
	}

	public void setFlushThreshold(int flushThreshold) {
		this.flushThreshold = flushThreshold;
	}

	final public void createIndex() {
	
		startTime = System.currentTimeMillis();
		
		validateDocumentProviders();
	
		calculateTotalDocuments();
		
		setupIndexWriter();
	
		processDocuments();
	
		flushOptimizeAndWrite();
	
		printProcessedDocumentsInfo();
		
		System.out.println("Done creating Index.");
	
		endTime = System.currentTimeMillis();
	
		System.out.println("Indexing time: "
				+ TimeUtil.formatMs(endTime - startTime));
	
	}

	private void setupIndexWriter() {
		System.out.println("Verifying valid environment for index writer.");
		if (totalDocuments < 1)
			throw new RuntimeException(
					"[Error] At least one artifact needed for creating index. Aborting!");
	
		if (this.indexDir == null || this.indexDir.length() < 0)
			throw new RuntimeException(
					"[Error] no directory specified to create the index. Aborting!");
	
		if (flushThreshold < 1)
			throw new RuntimeException(
					"[Error] Flush threshold needs to be greater than 0. Aborting!");
	
		System.out.println("..Initializing Index writer");
		
		ramDir = new RAMDirectory();
	
		try {
			fsDir = FSDirectory.getDirectory(this.indexDir);
			fsWriter = new IndexWriter(fsDir, new SimpleAnalyzer(), true);
			ramWriter = new IndexWriter(ramDir, new SimpleAnalyzer(),
					true);
	
		} catch (IOException e) {
			throw new RuntimeException(
					"[ERROR] Problem in opening the directory for the index. "
							+ "Check if the folder exists.");
		}
	
		System.out.println("OK.. started processing documents");
	}

	private void flushOptimizeAndWrite() throws RuntimeException {
			try {
				ramWriter.flush();
				fsWriter.addIndexes(new Directory[] { ramDir });
				ramWriter.close();
				System.out.println(":");
	
				System.out.println("");
				System.out.println("Optimizing index in file.");
	
				fsWriter.commit();
				fsWriter.optimize();
	
				System.out.println("Closing Index Writer.");
				fsWriter.close();
	//
	//			} catch (CorruptIndexException cie) {
	//				throw new RuntimeException(
	//						"[ERROR] corrupt index exception while "
	//								+ "flushing the RAM Directory");
			} catch (IOException e) {
				throw new RuntimeException(
						"[ERROR] low-level IO error while adding a document or "
								+ "flushing the RAM Directory");
			}
		}

	final protected void addDocument(int documentCount, Document document) throws RuntimeException {
			try {
				ramWriter.addDocument(document);
	
				if (documentCount % this.flushThreshold == 0) {
					// _ramWriter.optimize();
					ramWriter.flush();
					fsWriter.addIndexes(new Directory[] { ramDir });
					ramWriter.close();
					// Printing flush info in the console
					System.out.print(":");
	
					ramWriter = new IndexWriter(ramDir,
							new SimpleAnalyzer(), true);
	
				}
	//
	//			} catch (CorruptIndexException cie) {
	//				throw new RuntimeException(
	//						"[ERROR] corrupt index exception while "
	//								+ "flushing the RAM Directory");
			} catch (IOException e) {
				throw new RuntimeException(
						"[ERROR] low-level IO error while adding a document or "
								+ "flushing the RAM Directory");
			}
		}

}