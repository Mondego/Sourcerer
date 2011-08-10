package com.Sourcerer.search.server;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.Sourcerer.search.client.SearchService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.uci.ics.sourcerer.search.adapter.SearchAdapter;
import edu.uci.ics.sourcerer.search.adapter.SearchResult;
import edu.uci.ics.sourcerer.search.adapter.SingleResult;

public class SearchServiceImpl extends RemoteServiceServlet implements
		SearchService {

	private HashMap<String, List<SingleResult>> data = new HashMap<String, List<SingleResult>>();
	
	public String update(String text, int pageNumber)throws IOException {
		List<SingleResult> results;
		
		StringBuilder builder = new StringBuilder();
		if(data.get(text+pageNumber) == null){
		
			SearchResult result = SearchAdapter.search(text);
			results = result.getResults((pageNumber-1)*10, pageNumber*10);
			data.put(text+pageNumber, results);
		}
		else{
			results = data.get(text+pageNumber);
		}
		for(int i = 0; i < results.size(); i++){
			SingleResult temp = results.get(i);
			builder.append(temp.getEntityID()).append("<br>").append(temp.getEntityName()).append("\n").append(temp.getFilePath()).append("\n");
		}
		
		return builder.toString();
	}

}
