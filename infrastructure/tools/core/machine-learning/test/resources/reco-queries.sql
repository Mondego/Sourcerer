select r.lhs_eid as eid, e.fqn, count(e.fqn) from relations as r 
	inner join entities as e on e.entity_id=r.rhs_jeid      
		inner join entities as e2 on r.lhs_eid=e2.entity_id     
where e2.entity_type='METHOD' 
group by e.fqn 
limit 10;

----

select eid, fqn, count(fqn) from
(
select r.lhs_eid as eid, e.fqn as fqn 
 from relations as r inner join entities as e 
  on r.rhs_eid=e.entity_id
union
select r.lhs_eid as eid, e.fqn as fqn 
 from relations as r inner join jar_entities as e 
  on r.rhs_jeid=e.entity_id
union
select r.lhs_eid as eid, e.fqn as fqn 
 from relations as r inner join library_entities as e 
  on r.rhs_leid=e.entity_id
) as union_sql
inner join entities as e on
 eid=e.entity_id
where e.entity_type='METHOD'
group by eid,fqn
limit 10

----
select r.lhs_eid, r.rhs_jeid, count(r.rhs_jeid) from relations as r 
 where r.rhs_jeid IS NOT NULL 
group by r.lhs_eid,r.rhs_jeid 
limit 100;

----
-- count the uses of jar enity fqns
----
select r.lhs_eid, r.rhs_jeid, je.fqn, count(je.fqn) 
 from relations as r 
 inner join jar_entities as je on je.entity_id=r.rhs_jeid
  inner join entities as e on r.lhs_eid=e.entity_id 
 where r.rhs_jeid IS NOT NULL AND e.entity_type='METHOD'
group by r.lhs_eid,je.fqn 
limit 100;

----

select r.lhs_eid, je.fqn, count(je.fqn) 
 from relations as r 
 inner join jar_entities as je on je.entity_id=r.rhs_jeid
  inner join entities as e on r.lhs_eid=e.entity_id 
 where r.rhs_jeid IS NOT NULL 
  AND r.relation_type <> 'MATCHES'
  AND e.entity_type='METHOD'
group by r.lhs_eid,je.fqn 
limit 100;


----
-- dump jdk uses to table
----
select r.lhs_eid, je.fqn, count(je.fqn)
INTO OUTFILE 'c:\\jdk.txt'
FIELDS TERMINATED BY '  '
LINES TERMINATED BY '\n'
 from relations as r
 inner join library_entities as je on je.entity_id=r.rhs_leid
  inner join entities as e on r.lhs_eid=e.entity_id
 where r.rhs_leid IS NOT NULL
  AND r.relation_type <> 'MATCHES'
  AND e.entity_type='METHOD'
group by r.lhs_eid,je.fqn


select r.lhs_eid, je.fqn 
from relations as r
inner join relations as r2 on r.rhs_eid=r2.lhs_eid
inner join jar_entities as je on je.entity_id=r2.rhs_jeid 
where r2.relation_type='MATCHES'
 and r2.rhs_jeid IS NOT NULL
limit 10;

select r.lhs_eid, r.rhs_eid, r.relation_type 
from relations as r
inner join entities as de on r.rhs_eid=de.entity_id
where de.entity_type='DUPLICATE'
limit 10;


select * from relations where relation_type='MATCHES';

-- counts of duplicate entity matches, with fqns
select lhs_eid, count(lhs_eid) as c, e.fqn as fqn
from relations as r
inner join entities as e on e.entity_id=r.lhs_eid
where r.relation_type='MATCHES' 
group by lhs_eid order by  c desc;


-- 803473 is a duplicate entity with 87 matches
select r.lhs_eid, r.rhs_jeid, je.fqn, je.entity_type, j.jar_id, j.name, j.path from relations as r 
inner join jar_entities as je  on je.entity_id=r.rhs_jeid 
inner join jars as j on je.jar_id=j.jar_id
where r.lhs_eid=803473
and r.relation_type='MATCHES'
and r.rhs_jeid IS NOT null;

-- relations with 803473 as targets
select * from relations where rhs_eid=803473;
select * from relations where lhs_eid=803473;
select * from relations where rhs_jeid=803473;
select * from relations where rhs_leid=803473;


-- org.eclipse.swt.widgets.Control
select * from jar_entities where fqn='org.eclipse.swt.widgets.Control';

select * from jars;

select * from relations where rhs_eid=583064;

-- jars sorted with # of projects using them
select ju.jar_id, j.name, count(distinct project_id) as pcount from jar_uses as ju
inner join jars as j on j.jar_id=ju.jar_id
group by jar_id 
order by pcount desc;

-- jars and projects
select distinct ju.jar_id, j.name, p.project_id, p.name 
from jar_uses as ju
inner join jars as j on j.jar_id=ju.jar_id
inner join projects as p on p.project_id=ju.project_id;

-- 
select * from jar_entities
-- where jar_id=224
limit 100
;


-- all relations from source entities with PACKAGE entities
-- from jdk as target
select r.lhs_eid, e.fqn, e.entity_type, r.relation_type, 
	r.rhs_leid, le.entity_id as lib_entity_id, 
	le.fqn as lib_fqn, le.entity_type as lib_entity_type 
from relations as r
inner join entities as e on e.entity_id=r.lhs_eid
inner join library_entities as le on le.entity_id=r.rhs_leid
and le.entity_type='PACKAGE'
;


--
select r.lhs_eid, count(le.fqn)
from relations as r inner join library_entities as le
on r.rhs_leid=le.entity_id
group by r.lhs_eid
limit 10

--
select r.lhs_eid, count(distinct r.rhs_leid) as uc from relations as r
-- inner join library_entities as le 
-- on r.rhs_leid=le.entity_id group by r.lhs_eid 
order by uc desc 
limit 10;

-- backup sourcerer_t2
-- C:\mysql-5.1.40-win32\bin>mysqldump --opt --host localhost --port 3307 --user root  --password sourcerer_t2 > c:\sourcerer_t2_dump.sql

-- manuual http://dev.mysql.com/doc/refman/5.1/en/mysqldump.html

/*
1. mysqldump --opt --user=username --password database > dumbfile.sql


2. Edit the dump file and put these lines at the beginning:

SET AUTOCOMMIT = 0;
SET FOREIGN_KEY_CHECKS=0;


3. Put these lines at the end:

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;
SET AUTOCOMMIT = 1;


4. mysql --user=username --password database < dumpfile.sql

*/
-- =========================================================================
-- sourcerer_t2
-- =========================================================================

----
-- jar with most relations from project entities
----

select j.name, count(j.name) as jc from
relations as r
-- inner join entities as e on e.entity_id=r.lhs_eid
 inner join jar_entities as je on je.entity_id=r.rhs_jeid
  inner join jars as j on j.jar_id=je.jar_id
where r.rhs_jeid is not NULL
 and r.lhs_eid is NOT NULL
 and r.relation_type NOT IN ('MATCHES', 'INSIDE')
group by j.name
order by jc desc


-- count relations inside projects from jars
--
select j.name as jname, e.project_id as pid, p.name as pname, count(j.name) as jusecount from
relations as r
inner join entities as e on e.entity_id=r.lhs_eid
 inner join projects as p on p.project_id=e.project_id
 inner join jar_entities as je on je.entity_id=r.rhs_jeid
  inner join jars as j on j.jar_id=je.jar_id
where r.rhs_jeid is not NULL
 and r.lhs_eid is NOT NULL
 and r.relation_type NOT IN ('MATCHES', 'INSIDE')
group by j.name, e.project_id
order by j.name desc




-- jar use in projects
select j.name as jname,  count(j.name) as jusecount from
relations as r
inner join entities as e on e.entity_id=r.lhs_eid
 inner join jar_entities as je on je.entity_id=r.rhs_jeid
  inner join jars as j on j.jar_id=je.jar_id
where r.rhs_jeid is not NULL
 and r.lhs_eid is NOT NULL
 and r.relation_type NOT IN ('MATCHES', 'INSIDE')
group by e.project_id
order by j.name desc




-- dump entity_id, used_fqn, used_fqn_count

select r.lhs_eid, je.fqn, count(je.fqn) 
-- INTO OUTFILE 'c:\\jdk.txt' 
-- FIELDS TERMINATED BY '  ' 
-- LINES TERMINATED BY '\n'  
from relations as r
inner join library_entities as je 
 on je.entity_id=r.rhs_leid  
inner join entities as e 
 on r.lhs_eid=e.entity_id  
where ((r.rhs_leid IS NOT NULL)   
	AND (r.relation_type='USES'         
		OR r.relation_type='CALLS'      
		OR r.relation_type='INSTANTIATES'       
		OR r.relation_type='EXTENDS'    
		OR r.relation_type='IMPLEMENTS')   
	AND (e.entity_type='METHOD'          
		OR e.entity_type='CONSTRUCTOR'  
		OR e.entity_type='CLASS'        
		OR e.entity_type='UKNOWN')) 
group by r.lhs_eid,je.fqn 
limit 10;

select r.lhs_eid as eid, je.fqn as used_fqn, count(je.fqn) as ufcount 
from relations as r
inner join jar_entities as je 
on je.entity_id=r.rhs_jeid  
inner join entities as e 
on r.lhs_eid=e.entity_id  
where ((r.rhs_jeid IS NOT NULL)   
	AND (r.relation_type='USES'         
		OR r.relation_type='CALLS'      
		OR r.relation_type='INSTANTIATES'       
		OR r.relation_type='EXTENDS'    
		OR r.relation_type='IMPLEMENTS')   
	AND (e.entity_type='METHOD'          
		OR e.entity_type='CONSTRUCTOR'  
		OR e.entity_type='CLASS'        
		OR e.entity_type='UKNOWN')) 
group by r.lhs_eid,je.fqn 
limit 10;


select r.lhs_eid as eid, de.fqn as used_fqn, count(de.fqn) as ufcount
from relations as r
inner join entities as de
on (de.entity_id=r.rhs_eid AND de.entity_type='DUPLICATE')
inner join entities as e
on r.lhs_eid=e.entity_id 
where  ((r.rhs_eid IS NOT NULL)   
	AND (r.relation_type='USES'         
		OR r.relation_type='CALLS'      
		OR r.relation_type='INSTANTIATES'       
		OR r.relation_type='EXTENDS'    
		OR r.relation_type='IMPLEMENTS')   
	AND (e.entity_type='METHOD'          
		OR e.entity_type='CONSTRUCTOR'  
		OR e.entity_type='CLASS'        
		OR e.entity_type='UKNOWN')) 
group by r.lhs_eid,de.fqn 
limit 10;

-- ======
-- COUNTS
-- ======

-- count all entities with (1UNKNOWN)
-- result: 307 
select count(*) from entities where fqn like '%(1UNKNOWN)%';


-- count selected entity_types
-- Result: 201,359
--	565,822 for all entity types
select count(*) from entities where entity_type IN  ('CLASS','METHOD','CONSTRUCTOR', 'UNKNOWN')


-- count all relations to local entities (DUPLICATEs included) for
-- selected entities and relations
-- Result: 1,041,554 
--      1,090,576 for all entities but selected relations
--    2,202,054 for all entities and relations
select count(*) as local_ucount from relations as r 
inner join entities as e1 on (r.lhs_eid=e1.entity_id AND e1.entity_type IN ('CLASS','METHOD','CONSTRUCTOR', 'UNKNOWN'))
where rhs_eid IS NOT NULL
AND r.relation_type IN ('CALLS', 'INSTANTIATES', 'EXTENDS', 'IMPLEMENTS','USES','OVERRIDES')

-- count all relations to jdk entities for selected entities and relations
-- Result: 1,111,604 
--    2,270,964 for all entities but selected relations
--    2,689,754 for all entities and relations
select count(*) as jdk_ucount from relations as r
inner join entities as e1 on (r.lhs_eid=e1.entity_id AND e1.entity_type IN ('CLASS','METHOD','CONSTRUCTOR', 'UNKNOWN'))
where rhs_leid IS NOT NULL
AND r.relation_type IN ('CALLS', 'INSTANTIATES', 'EXTENDS', 'IMPLEMENTS', 'USES','OVERRIDES')

-- count all relations to jar entities on selected relations
-- Result: 70,296  
--    71,539 for all entities but selected relations
--    82,234 for all entities and relations
select count(*) as jar_ucount from relations as r
inner join entities as e1 on (r.lhs_eid=e1.entity_id AND e1.entity_type IN ('CLASS','METHOD','CONSTRUCTOR', 'UNKNOWN'))
where rhs_jeid IS NOT NULL
AND r.relation_type IN ('CALLS', 'INSTANTIATES', 'EXTENDS', 'IMPLEMENTS', 'USES','OVERRIDES')

-- count all relations to DUPLICATE entities for selected relations and entities
-- Result: 3,882 
--   4,005 for all entities but selected relations
--   5,435 for all entities and relations
select count(*) as jar_ucount_duplicates 
from relations as r
inner join entities as e on (r.rhs_eid=e.entity_id AND e.entity_type='DUPLICATE')
inner join entities as e1 on (r.lhs_eid=e1.entity_id AND e1.entity_type IN ('CLASS','METHOD','CONSTRUCTOR', 'UNKNOWN'))
where rhs_eid IS NOT NULL
AND r.relation_type IN ('CALLS', 'INSTANTIATES', 'EXTENDS', 'IMPLEMENTS', 'USES','OVERRIDES')

-- realtion counts by relation_types for jdk usage
-- considering selected entity types and relations
-- Result:
--
-- EXTENDS	  1,195
-- IMPLEMENTS	  2,157
-- CALLS	196,392
-- INSTANTIATES	 36,708
-- USES	        875,152
--
-- all entities and relations
-- Result:
--
-- USES	        2,026,145
-- HOLDS	  233,918
-- CALLS 	  201,153
-- RETURNS	  110,611
-- INSTANTIATES	   40,221
-- THROWS	   30,683
-- CASTS	   18,495
-- READS	   17,119
-- CHECKS	    2,319
-- IMPLEMENTS	    2,250
-- ANNOTATED_BY	    1,903
-- HAS_BASE_TYPE    1,718
-- EXTENDS	    1,195
-- HAS_TYPE_ARGUMENT  828
-- HAS_ELEMENTS_OF    809
-- WRITES	      372
-- HAS_UPPER_BOUND     15
select r.relation_type, count(*) as jdk_ucount from relations as r
inner join entities as e1 on (r.lhs_eid=e1.entity_id AND e1.entity_type IN ('CLASS','METHOD','CONSTRUCTOR', 'UNKNOWN'))
where rhs_leid IS NOT NULL
AND r.relation_type IN ('CALLS', 'INSTANTIATES', 'EXTENDS', 'IMPLEMENTS','USES','OVERRIDES')
group by r.relation_type

-- relation counts for jar usage
-- Result:
-- -------
-- CALLS	52,469
-- USES	        14,590
-- INSTANTIATES	2,095
-- EXTENDS	  970
-- IMPLEMENTS	  172
select r.relation_type, count(*) as jar_ucount from relations as r
inner join entities as e1 on (r.lhs_eid=e1.entity_id AND e1.entity_type IN ('CLASS','METHOD','CONSTRUCTOR', 'UNKNOWN'))
where rhs_jeid IS NOT NULL
AND r.relation_type IN ('CALLS', 'INSTANTIATES', 'EXTENDS', 'IMPLEMENTS','USES','OVERRIDES')
group by r.relation_type
order by jar_ucount desc

-- all entities and relations
-- CALLS	52673
-- USES	15590
-- HOLDS	3671
-- READS	2619
-- INSTANTIATES	2128
-- MATCHES	1628
-- RETURNS	1406
-- EXTENDS	970
-- THROWS	543
-- CASTS	420
-- IMPLEMENTS	178
-- ANNOTATED_BY	139
-- CHECKS	129
-- WRITES	98
-- HAS_ELEMENTS_OF	32
-- HAS_TYPE_ARGUMENT	8
-- HAS_UPPER_BOUND	2


-- relation counts for local usage
-- Result:
--
-- USES	638,338
-- CALLS	344,518
-- INSTANTIATES	43,782
-- EXTENDS	8,657
-- IMPLEMENTS	6,259

select r.relation_type, count(*) as ucount from relations as r
inner join entities as e1 on (r.lhs_eid=e1.entity_id AND e1.entity_type IN ('CLASS','METHOD','CONSTRUCTOR', 'UNKNOWN'))
where rhs_eid IS NOT NULL
AND r.relation_type IN ('CALLS', 'INSTANTIATES', 'EXTENDS', 'IMPLEMENTS','USES','OVERRIDES')
group by r.relation_type
order by ucount desc
-- all entities and relations
/*
USES	678450
INSIDE	529638
CALLS	348263
READS	291409
WRITES	117840
HOLDS	116932
INSTANTIATES	48089
RETURNS	18758
CASTS	16416
THROWS	11597
EXTENDS	8657
IMPLEMENTS	7117
CHECKS	4531
HAS_TYPE_ARGUMENT	2485
HAS_ELEMENTS_OF	800
HAS_BASE_TYPE	453
PARAMETRIZED_BY	405
ANNOTATED_BY	130
HAS_UPPER_BOUND	80
HAS_LOWER_BOUND	4
*/

select r.rhs_eid as eid, e.fqn as ufqn
from relations as r
inner join entities as e
on r.rhs_eid=e.entity_id
UNION ALL
select r2.rhs_eid as eid, je.fqn as ufqn
from relations as r2
inner join jar_entities as je
on r2.rhs_eid=je.entity_id
UNION ALL
select r3.rhs_eid as eid, le.fqn as ufqn
from relations as r2
inner join jar_entities as je
on r2.rhs_eid=je.entity_id


-- source entity counts

/***

mysql> select count(*) from jar_entities where jclass_fid is NOT NULL;
+----------+
| count(*) |
+----------+
|   895615 | 
+----------+
1 row in set (13.73 sec)

mysql> select count(*) from entities;
+----------+
| count(*) |
+----------+
|   565822 | 
+----------+
1 row in set (0.01 sec)

mysql> select count(distinct fqn) from jar_entities where jclass_fid is NOT NULL;
+---------------------+
| count(distinct fqn) |
+---------------------+
|              429917 | 
+---------------------+
1 row in set (1 min 8.99 sec)

mysql> select count(distinct fqn) from entities;
+---------------------+
| count(distinct fqn) |
+---------------------+
|              297921 | 
+---------------------+
1 row in set (17.01 sec)

mysql> select count(distinct fqn) from (select fqn from entities union all (select fqn from jar_entities where jclass_fid is not null)) as u;
+---------------------+
| count(distinct fqn) |
+---------------------+
|              711164 | 
+---------------------+
1 row in set (2 min 16.64 sec)

**/


-- top used fqn,entities (jdk, lib, local, duplicate)

-- top users (all usage, jdk usage, lib usage)

select * from jar_entities as je
inner join jars as j
on je.jar_id=j.jar_id
where j.name like '%eclipse.%'
limit 10

-- count unique eclipse fqns
select count(distinct fqn) 
from jar_entities as je 
inner join jars as j 
on je.jar_id=j.jar_id 
where j.name like '%eclipse.%' 
and je.jclass_fid IS NOT NULL;

-- usage

select e.project_id, e.fqn 
from entities as e 
inner join relation as r on e.entity_id=r.lhs_eid
inner join jar_entities as je where je.entity_id=r.rhs_jeid
and je.jar_id=929;

--  count realtions from jars in projects, per project
select j.name as jname, e.project_id as pid, p.name as pname, count(j.name) as jusecount from
relations as r
inner join entities as e on e.entity_id=r.lhs_eid
 inner join projects as p on p.project_id=e.project_id
 inner join jar_entities as je on je.entity_id=r.rhs_jeid
  inner join jars as j on j.jar_id=je.jar_id
where r.rhs_jeid is not NULL
 and r.lhs_eid is NOT NULL
 and r.relation_type NOT IN ('MATCHES', 'INSIDE')
group by j.name, e.project_id
order by j.name desc



-- class usage per projects

select le.fqn, le.entity_id, count(le.fqn)
from relations as r
inner join library_entities as le
on le.entity_id=r.rhs_leid
inner join entities as e
on e.entity_id=r.lhs_eid

--
select * from relations as r
    inner join entities as e on r.lhs_eid=e.entity_id 
    and e.project_id=64
    and relation_type<>'MATCHES';


-- count relations types used in a project
-- with jar entities as target
select count(*), r.relation_type 
from relations as r     
inner join entities as e on r.lhs_eid=e.entity_id      
 and e.project_id=64     
and r.rhs_jeid IS NOT NULL     
and r.relation_type<>'MATCHES' 
group by r.relation_type 

-- list jars used with relations terminating

/**

*/

-- count usage of jar entities
select je1.fqn, count(je1.fqn) as jfc
from jar_relations as jr1 inner join jar_entities as je1
on jr1.rhs_jeid=je1.entity_id
where jr1.relation_id in
(
-- relations among eclipse jar entities
select jr.relation_id from
jar_relations as jr 
inner join jar_entities as je on jr.lhs_jeid=je.entity_id
inner join jars as j on j.jar_id=je.jar_id
where j.name like '%eclipse.%'
)
group by je1.fqn
order by jfc desc

--
-- count usage of jar entities in eclipse projects
select je1.fqn, count(je1.fqn) as jfc
INTO OUTFILE 'c:\\jar-usage.txt' 
FIELDS TERMINATED BY '	' 
LINES TERMINATED BY '\n' 
from jar_relations as jr1
inner join jar_entities as je1 on jr1.rhs_jeid=je1.entity_id
inner join jar_entities as je on jr1.lhs_jeid=je.entity_id
inner join jars as j on j.jar_id=je.jar_id
where j.name like '%eclipse.%'
group by je1.fqn
order by jfc desc


-- count usage of library entities
select le.fqn, count(le.fqn) as lfc
from jar_relations as jr1 
inner join library_entities as le on jr1.rhs_leid=le.entity_id
inner join jar_entities as je on jr1.lhs_jeid=je.entity_id
inner join jars as j on j.jar_id=je.jar_id
where j.name like '%eclipse.%'
group by le.fqn
order by lfc desc


--
select le.fqn, count(le.fqn) as lfc
INTO OUTFILE 'c:\\jdk-usage2.txt' 
FIELDS TERMINATED BY '	' 
LINES TERMINATED BY '\n' 
from library_entities as le 
inner join (jar_relations as jr1 
	inner join (jar_entities as je 
		inner join jars as j 
			on j.jar_id=je.jar_id 
			and j.name like '%eclipse.%')
		 on jr1.lhs_jeid=je.entity_id)
	 on jr1.rhs_leid=le.entity_id
group by le.fqn
order by lfc desc
limit 1000

--
select le.fqn, count(le.fqn) as lfc
INTO OUTFILE 'c:\\jdk-usage.txt' 
FIELDS TERMINATED BY '	' 
LINES TERMINATED BY '\n' 
from jar_relations as jr1
inner join library_entities as le on jr1.rhs_leid=le.entity_id
inner join jar_entities as je on jr1.lhs_jeid=je.entity_id
inner join jars as j on j.jar_id=je.jar_id
where j.name like '%eclipse.%'
group by le.fqn
order by lfc desc
-- limit 1000




select r.lhs_eid, je.fqn, count(je.fqn)
INTO OUTFILE 'c:\\jdk.txt'
FIELDS TERMINATED BY '  '
LINES TERMINATED BY '\n'
 from relations as r
 inner join library_entities as je on je.entity_id=r.rhs_leid
  inner join entities as e on r.lhs_eid=e.entity_id
 where r.rhs_leid IS NOT NULL
  AND r.relation_type <> 'MATCHES'
  AND e.entity_type='METHOD'
  
group by r.lhs_eid,je.fqn

---
-- list parents in relations
-- skip siblings
---
select 
distinct r1.relation_id, r1.lhs_jeid, r1.rhs_jeid, 
    r2.rhs_jeid as p1, r3.rhs_jeid as p2
from jar_relations as r1
inner join jar_relations as r2
on r1.lhs_jeid=r2.lhs_jeid AND
r2.relation_type='INSIDE'
inner join jar_relations as r3
on r1.rhs_jeid=r3.lhs_jeid AND
r3.relation_type='INSIDE'
where
r1.relation_type='CALLS'
and r2.rhs_jeid <> r3.rhs_jeid
and r1.lhs_jeid=189086; 


select r.relation_id, r.lhs_jeid, r.rhs_jeid, je.fqn 
from jar_relations  as r
inner join jar_entities as je on
je.entity_id=r.rhs_jeid
where 
lhs_jeid=189086 AND 
relation_type='CALLS' AND 
rhs_jeid IS NOT NULL;





---
-- Q.E export jar entity id, used fqn, used fqn count
---
select jr.lhs_jeid, je.fqn, count(je.fqn) as ucount
from 
(
select 
 distinct r1.relation_id, 
 r1.lhs_jeid as lhs_jeid, 
 r1.rhs_jeid as rhs_jeid, 
 r2.rhs_jeid as lp, 
 r3.rhs_jeid as rp
from jar_relations as r1
inner join jar_relations as r2
on r1.lhs_jeid=r2.lhs_jeid AND
   r2.relation_type='INSIDE'
inner join jar_relations as r3
on r1.rhs_jeid=r3.lhs_jeid AND
   r3.relation_type='INSIDE'
where
r1.relation_type IN ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES')
and r2.rhs_jeid <> r3.rhs_jeid
) as jr -- jr has subset of relations among entities, skips relations among siblings
inner join jar_entities as je -- je is an API entity
 on jr.rhs_jeid=je.entity_id  AND
 IF(je.modifiers & 0x0001,1,0)=1 -- only public entities (API)
inner join jar_entities as je1 -- je1 is user
 on je1.entity_id=jr.lhs_jeid AND 
 je1.entity_type in ('CLASS','METHOD','CONSTRUCTOR')
inner join jars as j 
 on j.jar_id=je1.jar_id AND 
 j.name like '%eclipse.%'
group by jr.lhs_jeid, je.fqn
order by ucount desc



---
-- union of uses
---
select eid, fqn, count(fqn)
from
(
select jr.lhs_jeid as eid, je.fqn as fqn
from jar_entities as je -- je is an API entity
inner join jar_relations as jr
 on jr.rhs_jeid=je.entity_id
inner join jar_entities as je1 -- je1 is user
 on je1.entity_id=jr.lhs_jeid
inner join jars as j 
 on j.jar_id=je1.jar_id
where 
 j.name like '%eclipse.%' AND
 je1.entity_type in ('CLASS','METHOD','CONSTRUCTOR') AND
 jr.relation_type in ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES') AND
 IF(je.modifiers & 0x0001,1,0)=1 -- only public entities (API)
UNION ALL
select jr.lhs_jeid as eid, je.fqn as fqn
from library_entities as le -- le is an API entity
inner join jar_relations as jr
 on jr.rhs_leid=le.entity_id
inner join jar_entities as je1 -- je1 is user
 on je1.entity_id=jr.lhs_jeid
inner join jars as j 
 on j.jar_id=je1.jar_id
where 
 j.name like '%eclipse.%' AND
 je1.entity_type in ('CLASS','METHOD','CONSTRUCTOR') AND
 jr.relation_type in ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES') AND
 IF(le.modifiers & 0x0001,1,0)=1 -- only public entities (API)
) as usql
group by eid, fqn


---
-- union of uses alternate (w/o skipping relations among siblings)
---
select eid, fqn, count(fqn) as fc
from
(
-- jar entities
select 
 jr.lhs_jeid as eid, je.fqn as fqn, 
 je1.entity_type as etype, je1.jar_id as jid
from jar_relations as jr
inner join jar_entities as je -- je is an API entity
 on jr.rhs_jeid=je.entity_id
inner join jar_entities as je1 -- je1 is user
 on je1.entity_id=jr.lhs_jeid
where 
 jr.relation_type in ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES') AND
 IF(je.modifiers & 0x0001,1,0)=1 -- only public entities (API)

UNION ALL
-- library entities
select 
 jr_2.lhs_jeid as eid, le.fqn as fqn,
 je1_2.entity_type as etype, je1_2.jar_id as jid
from jar_relations as jr_2
inner join library_entities as le -- le is an API entity
 on jr_2.rhs_leid=le.entity_id
inner join jar_entities as je1_2 -- je1_2 is user
 on je1_2.entity_id=jr_2.lhs_jeid
where 
 jr_2.relation_type in ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES') AND
 IF(le.modifiers & 0x0001,1,0)=1 -- only public entities (API)
) as usql
inner join jars as j 
 on j.jar_id=jid
where 
 j.name like '%eclipse.%' AND
 etype in ('CLASS','METHOD','CONSTRUCTOR') AND
group by eid, fqn
order by fc desc


-- == QUERY DOES NOT RUN ==
---
-- union of uses (skipping relations among siblings)
---
select eid, fqn, count(fqn) as fc
from
(
-----------
-- USAGE 1
-----------
-- jar entity_ids and jar fqns used
select jr.lhs_jeid as eid, je.fqn
from 
(
select 
 distinct r1.relation_id, 
 r1.lhs_jeid as lhs_jeid, 
 r1.rhs_jeid as rhs_jeid, 
 r2.rhs_jeid as lp, -- parent of user entity
 r3.rhs_jeid as rp  -- parent of API entity
from jar_relations as r1
inner join jar_relations as r2
on r1.lhs_jeid=r2.lhs_jeid AND
   r2.relation_type='INSIDE'
inner join jar_relations as r3
on r1.rhs_jeid=r3.lhs_jeid AND
   r3.relation_type='INSIDE'
where
r1.relation_type IN ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES')
and r2.rhs_jeid <> r3.rhs_jeid
) as jr -- jr has subset of relations among jar entities, skips relations among siblings
inner join jar_entities as je -- je is an API entity
 on jr.rhs_jeid=je.entity_id  AND
 IF(je.modifiers & 0x0001,1,0)=1 -- only public entities (API)
UNION ALL
-----------
-- USAGE 2
-----------
-- jar entity_ids and library fqns used
select 
 jr_2.lhs_jeid as eid, le.fqn as fqn
from jar_relations as jr_2
inner join library_entities as le -- le is an API entity
 on jr_2.rhs_leid=le.entity_id
where 
 jr_2.relation_type in ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES') AND
 IF(le.modifiers & 0x0001,1,0)=1 -- only public entities (API)
) as union_sql
inner join jar_entities as users 
on users.entity_id=eid
inner join jars as j 
 on j.jar_id=users.jar_id
where 
 j.name like '%eclipse.%' AND -- choose relations from eclipse projects
 users.entity_type in ('CLASS','METHOD','CONSTRUCTOR') -- limit to these types for users
group by eid, fqn
order by fc desc


-- ==============
-- JUST THE UNION

-- ---------
-- USAGE 1
-- ---------
-- jar entity_ids and jar fqns used
select 
 distinct r1.relation_id as rid, 
 r1.lhs_jeid as eid, 
 je.fqn as fqn
 -- r1.rhs_jeid as rhs_jeid, 
 -- r2.rhs_jeid as lp, -- parent of user entity
 -- r3.rhs_jeid as rp  -- parent of API entity
from jar_relations as r1
inner join jar_relations as r2
on r1.lhs_jeid=r2.lhs_jeid AND
   r2.relation_type='INSIDE'
inner join jar_relations as r3
on r1.rhs_jeid=r3.lhs_jeid AND
   r3.relation_type='INSIDE'
inner join jar_entities as je -- je is an API entity
 on r1.rhs_jeid=je.entity_id  AND
 IF(je.modifiers & 0x0001,1,0)=1 -- only public entities (API)
inner join jar_entities as users 
 on users.entity_id=r1.lhs_jeid
inner join jars as j 
 on j.jar_id=users.jar_id
where
r1.relation_type IN ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES') and 
r2.rhs_jeid <> r3.rhs_jeid and
-- has subset of relations among jar entities, skips relations among siblings
j.name like '%eclipse.%' AND -- choose relations from eclipse projects
users.entity_type in ('CLASS','METHOD','CONSTRUCTOR') -- limit to these types for users
UNION ALL
-- ---------
-- USAGE 2
-- ---------
-- jar entity_ids and library fqns used
select 
 distinct jr_2.relation_id as rid, jr_2.lhs_jeid as eid, le.fqn as fqn
from jar_relations as jr_2
inner join library_entities as le -- le is an API entity
 on jr_2.rhs_leid=le.entity_id
inner join jar_entities as users 
 on users.entity_id=jr_2.lhs_jeid
inner join jars as j 
 on j.jar_id=users.jar_id
where 
 jr_2.relation_type in ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES') AND
 IF(le.modifiers & 0x0001,1,0)=1 AND -- only public entities (API)
 j.name like '%eclipse.%' AND -- choose relations from eclipse projects
 users.entity_type in ('CLASS','METHOD','CONSTRUCTOR') -- limit to these types for users

