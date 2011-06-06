package com.Sourcerer.search.server;

import java.util.Date;

import com.Sourcerer.search.client.SearchService;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SearchServiceImpl extends RemoteServiceServlet implements
		SearchService {

	public String update(String text) {
		String str = "Searched:  " + text ;
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}

}
