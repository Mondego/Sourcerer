package com.Sourcerer.search.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("Sourcerer")
public interface SearchService extends RemoteService {
	
	public String update(String text);

}
