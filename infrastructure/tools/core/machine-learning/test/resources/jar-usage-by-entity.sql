-- ----
-- jar usage in eclipse projects
-- ----
select je1.fqn, count(distinct jr1.lhs_jeid) as jfc
from jar_relations as jr1
inner join jar_entities as je1 on jr1.rhs_jeid=je1.entity_id
inner join jar_entities as je on jr1.lhs_jeid=je.entity_id
inner join jars as j on j.jar_id=je.jar_id
where j.name like '%eclipse.%'
group by je1.fqn
order by jfc desc