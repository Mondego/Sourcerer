/**
 * @author <a href="sbajrach@ics.uci.edu">skb</a>
 *  created: Dec 3, 2007 
 */
package edu.uci.ics.sourcerer.lucene.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.index.IndexReader;

/**
 * 
 */
public class IndexMergerRunner {
	
	String oldIndexdirs;
	String indexdir;
	int flushThreshold = 150;
	
	Option listOfIndexDirs;
	Option mergeIndexDir;
	Option indexFlushThreshold;
	
	Options options = new Options();
	CommandLine line;
	HelpFormatter formatter = new HelpFormatter();
	ArrayList<String> cmdLineErrors = new ArrayList<String>(3);
	
	public static void main(String[] args) {
		IndexMergerRunner imRunner = new IndexMergerRunner(args);
		imRunner.execute();
	}
	
	public IndexMergerRunner(String[] args) {
		
		cmdLineErrors.clear();
		
		listOfIndexDirs 
			= OptionBuilder.isRequired().withArgName("file")
		  	  .hasArg()
			  .withDescription("use given file that lists the absolute paths to index directories from the root, as produced by the *nix command `find`" )
			  .create("idirlist");
		
		mergeIndexDir 
			= OptionBuilder.isRequired().withArgName( "directory" )
			  .hasArg()
			  .withDescription("use given directory to produce the merged index in" )
			  .create( "newIndexdir" );
		
		indexFlushThreshold 
			= OptionBuilder.withType(new Integer(0)).withArgName("value")
			  .hasArg()
			  .withDescription("use the integer value as a flush threhold for index being produced")
			  .create( "flush" );
		
		options.addOption(listOfIndexDirs);
		options.addOption(mergeIndexDir);
		options.addOption(indexFlushThreshold);
		
		CommandLineParser parser = new GnuParser();
	    try {
	        line = parser.parse( options, args );
	    }
	    catch( ParseException exp ) {
	    	// checks if all required args are present
	    	System.err.println( "Failed parsing commandline arguments.\n" +
	    						"Reason: " + exp.getMessage() );
	    	exit();
	    }
	    
	    if (line.hasOption("flush")) {
	    	
	    	try {
	    		flushThreshold = Integer.valueOf(line.getOptionValue("flush")); 
	    	}
	    	catch (NumberFormatException nfe) {
	    		System.err.println("Option flush needs an integer value. Provided: " + line.getOptionValue("flush"));
	    		exit();
	    	}
	    }
	    	
	    // till cli2 is released validate files yourself :(
	    oldIndexdirs = line.getOptionValue("idirlist");
	    if(!new File(oldIndexdirs).exists()) addOptionError(oldIndexdirs + " does not exist.");
	    indexdir = line.getOptionValue("newIndexdir");
	    if(!new File(indexdir).exists()) addOptionError(indexdir + " does not exist.");
	    
	    if(cmdLineErrors.size()>0) {
	    	for (String s : cmdLineErrors) System.err.println(s);
	    	exit();
	    }
		
		
	}
	
	public void execute() {
		
		IndexMerger im = new IndexMerger();
		
		im.setFlushThreshold(flushThreshold);
		im.setIndexDir(indexdir);
		
		try{
			im.initOldIndexPaths(new File(oldIndexdirs));
		} catch (Exception e) { 
			System.err.println("IO expection while opening " + oldIndexdirs);
			e.printStackTrace();
			exit();
		}
		
		im.createIndex();
		
		IndexReader ir = null; 
		try {
			ir = IndexReader.open(indexdir);
		} catch (IOException e) {
			
			System.err.println("Could not open the index created. Possibly corrupted or failed.");
			e.printStackTrace();
		}
		
		if(ir !=null )
			System.out.println("Total docs in the merged index: " + ir.numDocs());
		
	}
	
	private void exit() {
		formatter.printHelp( "indexmerger", options );
    	System.exit(-1);
	}
	
	private void addOptionError(String errorMsg) {
		cmdLineErrors.add(errorMsg);
	}
	

}
