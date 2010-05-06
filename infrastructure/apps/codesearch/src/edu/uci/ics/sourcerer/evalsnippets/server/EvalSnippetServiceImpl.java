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
package edu.uci.ics.sourcerer.evalsnippets.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.uci.ics.sourcerer.db.adapter.JdbcDataSource;
import edu.uci.ics.sourcerer.db.adapter.SourcererDbAdapter;
import edu.uci.ics.sourcerer.evalsnippets.client.EvalSnippetService;
import edu.uci.ics.sourcerer.scs.common.client.EntityType;
import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 9, 2010
 *
 */
public class EvalSnippetServiceImpl extends RemoteServiceServlet implements
		EvalSnippetService {

	private static final long serialVersionUID = -2592376163130201916L;
	JdbcDataSource dataSource = new JdbcDataSource();
    SourcererDbAdapter dba = new SourcererDbAdapter();
      
	
	public void init(){
		
		String dbUrl = "jdbc:mysql://tagus.ics.uci.edu:3306/sourcerer_eclipse";//getInitParameter("db-url");
		String dbUser = "sourcerer";//getInitParameter("db-user");
		String dbPassword = "sourcerer4us";//getInitParameter("db-password");
		
		Properties p = new Properties();
	    p.put("driver", "com.mysql.jdbc.Driver");
	    p.put("url", dbUrl);
	    p.put("user", dbUser);
	    p.put("password", dbPassword);
	    dataSource.init(p);
	    
	    dba.setDataSource(dataSource);
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.ics.sourcerer.evalsnippets.client.EvalSnippetService#getSnippetsInBulk(java.lang.String)
	 */
	public Map<String, Map<String, String>> getSnippetsInBulk(String query) {
		
		return SnippetServiceAdapter.getSnippetsInBulk(query, dba);
	}
	
	public Set<SearchHeuristic> getSchemes() {
		return SnippetServiceAdapter.getSchemes();
	}

}
