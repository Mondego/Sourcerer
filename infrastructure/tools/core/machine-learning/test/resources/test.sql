
SELECT jdk_provider.fqn AS pfqn_jdk FROM library_entities AS jdk_provider 
				INNER JOIN 
				  (jar_relations AS jr INNER JOIN jar_entities AS je ON 
					jr.lhs_jeid=je.entity_id AND
				       (jr.relation_type='CALLS' OR
						jr.relation_type='INSTANTIATES' OR
						jr.relation_type='USES') AND		
					je.entity_id=6585252 )  ON  
				jr.rhs_leid=jdk_provider.entity_id;
				

				
				
select  
		distinct r1.relation_id as rid,  
		 -- r1.lhs_jeid as eid,  
		 je.fqn as pfqn_lib  
		from jar_relations as r1  
		
		inner join jar_relations as r2  
		 on r1.lhs_jeid=r2.lhs_jeid AND  
		 r2.relation_type='INSIDE'  
		inner join jar_relations as r3  
		 on r1.rhs_jeid=r3.lhs_jeid AND  
		 r3.relation_type='INSIDE'
		   
		inner join jar_entities as je   -- je is an API entity 
		 on r1.rhs_jeid=je.entity_id  AND 
		 IF(je.modifiers & 0x0001,1,0)=1    -- only public entities (API
		
		where
		r1.lhs_jeid=2978074 AND
		r1.relation_type IN ('CALLS','INSTANTIATES','USES') AND  
		r1.jar_id in (6,15,32,52,54,62,67,76,88,105,125,132,142,145,147,168,175,177,180,181,184,190,205,208,215,217,224,230,237,240,254,261,272,282,287,291,310,312,319,334,347,352,392,399,417,441,444,454,458,460,463,482,489,518,523,527,540,543,547,554,557,558,563,580,587,588,590,605,614,623,624,632,633,639,647,678,687,689,690,694,703,705,727,728,751,759,762,764,769,783,792,804,815,828,835,837,846,847,860,861,870,882,888,897,909,916,922,932,946,949,953,956,958,963,974,983,987,989,1002,1005,1012,1021,1022,1026,1033,1048,1050,1052,1053,1057,1060,1065,1082,1085,1087,1116,1117,1119,1126,1129,1149,1150,1153,1199,1203,1216,1220,1225,1229,1237,1249,1250,1253,1265,1274,1275,1276,1277,1309,1329,1340,1343,1348,1367,1368,1373,1383,1402,1413,1420,1428,1445,1483,1488,1489,1493,1503,1512,1521,1525,1536,1538,1562,1571,1572,1588,1603,1607,1618,1619,1620,1626,1634,1648,1655,1658,1659,1666,1670,1684,1686,1691,1692,1693,1728,1730,1746,1750,1793,1794,1800,1802,1817,1820,1825,1848,1851,1892,1907,1908,1909,1910,1911,1912,1913,1914,1915,1916,1917,1919,1920,1921,1922,1923,1925,1927,1928,1930,1931,1932,1933,1934,1935,1936,1937,1938,1940,1941,1944,1945,1946,1947,1948,1949,1950,1952,1953,1954,1955,1956,1957,1959,1960,1961,1962,1963,1964,1965,1966,1967,1969,1970,1971,1972,1973,1975,1977,1978,1979,1981,1983,1984,1985,1986,1987,1988,1989,1990,1991,1992,1993,1995,1997,1999,2000,2001,2002,2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,2016,2017,2018,2019,2020,2022,2024,2025,2026,2027,2028,2029,2030,2031,2032,2033,2035,2036,2038,2039,2041,2042,2043,2045,2046,2047,2048,2049,2050,2051,2052,2053,2054,2055,2056,2058,2059,2060,2061,2062,2063,2064,2065,2066,2069,2070,2071,2072,2073,2074,2075,2078,2079,2081,2082) AND
		NOT (r2.rhs_jeid = r3.rhs_jeid)   -- has subset of relations among jar entities, skips relations among siblings
	;

select NOT (1 = 2) as f, NOT (1 <> 2) as f2, ( 1 <> 2) as f3 from dual				
				
select  
		distinct r1.relation_id as rid,  
		 r1.lhs_jeid as eid,  
		 je.fqn as fqn  
		from jar_relations as r1  
		
		inner join jar_relations as r2  
		 on r1.lhs_jeid=r2.lhs_jeid AND  
		 r2.relation_type='INSIDE'  
		inner join jar_relations as r3  
		 on r1.rhs_jeid=r3.lhs_jeid AND  
		 r3.relation_type='INSIDE'
		   
		inner join jar_entities as je   -- je is an API entity 
		 on r1.rhs_jeid=je.entity_id  AND 
		 IF(je.modifiers & 0x0001,1,0)=1    -- only public entities (API
		inner join jar_entities as users  
		 on users.entity_id=r1.lhs_jeid  
		-- inner join jars as j  
		-- on j.jar_id=users.jar_id  
		where  
		r1.relation_type IN ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES') AND  
		r2.rhs_jeid <> r3.rhs_jeid and   -- has subset of relations among jar entities, skips relations among siblings
		-- j.name like '%eclipse.%' AND    -- choose relations from eclipse projects
		users.entity_type in ('CLASS','METHOD','CONSTRUCTOR')   -- limit to these types for users
		and r1.lhs_jeid=2978073;