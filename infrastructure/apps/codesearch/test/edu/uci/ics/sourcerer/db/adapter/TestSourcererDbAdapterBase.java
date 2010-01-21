package edu.uci.ics.sourcerer.db.adapter;

import java.util.Arrays;
import java.util.Properties;

import junit.framework.TestCase;

public class TestSourcererDbAdapterBase extends TestCase {
	
	JdbcDataSource dataSource = new JdbcDataSource();
    SourcererDbAdapter dba = new SourcererDbAdapter();
	
	protected void setUp() throws Exception {
		super.setUp();
		
		Properties p = new Properties();
	    p.put("driver", "com.mysql.jdbc.Driver");
	    p.put("url", "jdbc:mysql://mondego.calit2.uci.edu:3307/sourcerer_t2");
	    p.put("user", System.getProperty( "sourcerer.db.user" ));
	    p.put("password", System.getProperty( "sourcerer.db.password" ));
	    dataSource.init(p);
	    
	    dba.setDataSource(dataSource);
	}
}
