-- ----
-- jdk usage in eclipse projects
-- ----
select le.fqn, count(distinct jr1.lhs_jeid) as lfc
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