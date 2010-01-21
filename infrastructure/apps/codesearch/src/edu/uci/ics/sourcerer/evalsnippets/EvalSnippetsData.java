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
package edu.uci.ics.sourcerer.evalsnippets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 15, 2010
 *
 */
public class EvalSnippetsData {
	
	public static HashMap<String,String> schemes = new HashMap<String,String>();
	public static HashMap<String,String> queries = new HashMap<String,String>();
	
	static{
		schemes.put("Text + FQN + sname","S1");
		schemes.put("Text + Used FQNs/Javadoc + FQN + Jdk/Lib sim sname + sname", "S3");
		schemes.put("Text + Used FQNs/Javadoc + FQN + sname", "S2");
		schemes.put("Text + Used FQNs/Javadoc + FQN + Jdk/Lib Hamming sim sname + sname", "S4");
		schemes.put("Text + Used FQNs/Javadoc + FQN + Jdk/Lib Tanimoto sim sname + sname", "S5");
		
//		queries.put("run job in ui thread", "q19");
		queries.put("copy paste data from clipboard", "q1");
//		queries.put("open url in html browser", "q2");
//		queries.put("write to workbench error log", "q4");
//		queries.put("track mouse hover", "q3");
//		queries.put("track job status change", "q5");
//		queries.put("open file in external editor", "q6");
//		queries.put("batch workspace changes in single operation", "q7");
//		queries.put("remove problem marker from resource", "q8");
//		queries.put("highlight text range in editor", "q9");
//		queries.put("update status line", "q10");
//		queries.put("prompt user to select directory", "q11");
//		queries.put("use shared image from workbench", "q12");
//		queries.put("open dialog and ask yes no question", "q13");
//		queries.put("parse source string ast node", "q14");
//		queries.put("get return type from method declaration","q15");
//		queries.put("fill table background thread", "q16");
//		queries.put("platform debug tracing for plugin", "q17");
//		queries.put("get display created in current thread", "q18");
//		queries.put("open external file", "q20");
		
		
//		queries.put("update status line message from editor", "q21");
//		queries.put("display context sensitive help", "q22");
//		queries.put("get return type from method declaration", "q23");
//		queries.put("find running job instance", "q24");
//		queries.put("check if public modifier MethodDeclaration", "q25");
	}
	
	public static File judgementFile = new File("/Users/shoeseal/sandbox/pub/sourcerer/msr-2010/prog-data/Relevance.txt");
	
	/**
	 * 
	 * @return <qid!eid!schemeid : relevancy>
	 */
	public static HashMap<String,String> openJudgementFile(){
		HashMap<String,String> qryEidSchemeMap = new HashMap<String,String>();
		try {
		      //use buffering, reading one line at a time
		      //FileReader always assumes default encoding is OK!
		      BufferedReader input =  new BufferedReader(new FileReader(judgementFile));
		      try {
		        String line = null; //not declared within while loop
		        /*
		        * readLine is a bit quirky :
		        * it returns the content of a line MINUS the newline.
		        * it returns null only for the END of the stream.
		        * it returns an empty String if two newlines appear in a row.
		        */
		        String query = "";
		        String[] header = null;
		        while (( line = input.readLine()) != null){
		          if(line.trim().length()<=0) continue;
		          
		          if(line.startsWith("Query:")){
		        	  query = line.replaceFirst("Query:", "").trim();
		        	  query = queries.get(query);
		        	  header = null;
		        	  continue;
		          }
		          
		          if (header == null){
		        	  header = line.split(",");
		        	  continue;
		          }
		          
		          String[] cols = line.split(",");
		          assert cols.length == 7;
		          String eid = null;
		          
		          for(int i=0; i<cols.length-1; i++){
		        	  if(i==0){ 
		        		  eid = cols[0];
		        	  } else {
		        		  
//		        		   String r = query + "," + eid + "," + schemes.get(header[i]) + "," + cols[i];
//		        		   System.out.println(r);
		        		  
		        		   qryEidSchemeMap.put(query + "!" + eid + "!" + schemes.get(header[i]), cols[i]);
		        	  }
		          }
		      }
		      }
		      finally {
		        input.close();
		      }
		    }
		    catch (IOException ex){
		      ex.printStackTrace();
		    }
		    
		    return qryEidSchemeMap;
		
	}
	
	public static void main(String args[]){
		openJudgementFile();
	}
	
	

}
