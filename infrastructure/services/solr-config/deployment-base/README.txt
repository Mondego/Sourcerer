# Indexing (on local machine):
==============================

Start solr by executing the following command from this folder

> java -Djetty.port=8983 -Dsolr.data.dir="/Users/shoeseal/DATA/solr.p1" -Djava.util.logging.config.file=logging.properties -Dsolr.solr.home="./installation/solr/" -jar start.jar

Pass 2, one instance ..
> java -Dsolr.data.dir="/Users/shoeseal/DATA/solr.p2.2" -Djava.util.logging.config.file=logging.properties -Dsolr.solr.home="./installation/solr/" -Djetty.port=8984 -Djetty.logs="./logs.1" -Dserver.id="Server2" -jar start.jar ./etc/jetty.xml 

Pass 2, second instance ..
> java -Dsolr.data.dir="/Users/shoeseal/DATA/solr.p2.3" -Djava.util.logging.config.file=logging.properties -Dsolr.solr.home="./installation/solr/" -Djetty.port=8985 -Djetty.logs="./logs.2" -Dserver.id="Server3" -jar start.jar ./etc/jetty.xml

in this directory, and when Solr is started connect to 

  http://localhost:8983/solr/

Check 
 http://wiki.apache.org/solr/DataImportHandler for detailed
usage guide and tutorial.

Indexing via Solr web interface

http://localhost:8983/solr/scs/dataimport?command=full-import
http://localhost:8983/solr/scs/dataimport?command=optimize
http://localhost:8983/solr/scs/dataimport?command=commit

change port to 8994 for second pass indexing



# Dependencies:
===============

patched version of solr.war
[patch required for field collpasing, used during search only;
 therefore for indexing standard solr distribution should suffice]
- execute: ant fetch-patch-solr-1.4
-- from path: Sourcerer/BIN
- execute: ant example
-- From path: Sourcerer/infrastructure/services/solr-1.4-patched
-- use solr.war, or copy into solr-config


for latest version of sourcerer solr component and building working solr
installations
- execute: buildbin.sh
-- From path: Sourcerer/../solr-config
