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
package edu.uci.ics.sourcerer.scs.server;


import java.util.LinkedList;
import java.util.List;
import java.util.Properties;



import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.uci.ics.sourcerer.db.adapter.JdbcDataSource;
import edu.uci.ics.sourcerer.db.adapter.SourcererDbAdapter;
import edu.uci.ics.sourcerer.scs.client.ERTables;
import edu.uci.ics.sourcerer.scs.client.SourcererDBService;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;
import edu.uci.ics.sourcerer.scs.common.client.HitFqnEntityId;
import edu.uci.ics.sourcerer.scs.common.client.UsedFqn;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 22, 2009
 *
 */
public class SourcererDBServiceImpl extends RemoteServiceServlet implements
	SourcererDBService {
	
	JdbcDataSource ds = new JdbcDataSource();
	SourcererDbAdapter dba = new SourcererDbAdapter();
	
	public void init(){
		
		String dbUrl = getInitParameter("db-url");
		String dbUser = getInitParameter("db-user");
		String dbPassword = getInitParameter("db-password");
		
		Properties p = new Properties();
	    p.put("driver", "com.mysql.jdbc.Driver");
	    p.put("url", dbUrl);
	    p.put("user", dbUser);
	    p.put("password", dbPassword);
	    ds.init(p);
	    
	    dba.setDataSource(ds);
	}

	public ERTables getERTables(List<String> hitEntities) {
		
		return dba.buildDbForHitEntities(hitEntities);
		
	}

	public UsedFqn fillUsedFqnDetails(HitFqnEntityId fqn, EntityCategory cat) {
		return dba.fillUsedFqnDetails(fqn, cat);
	}
	
	public List<UsedFqn> fillUsedFqnDetails(List<HitFqnEntityId> fqns, EntityCategory cat) {
		return dba.fillUsedFqnsDetails(fqns, cat);
	}
	
}