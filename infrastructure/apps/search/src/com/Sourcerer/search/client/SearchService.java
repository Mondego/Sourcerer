package com.Sourcerer.search.client;

import java.io.IOException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("Sourcerer")
public interface SearchService extends RemoteService {
	
	public String update(String text, int pageNumber) throws IOException;

}
