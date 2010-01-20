# Indexing:
===========

Start solr by executing the following command from this folder

> java -Dsolr.solr.home="./installation/solr/" -jar start.jar

in this directory, and when Solr is started connect to 

  http://localhost:8983/solr/

See also README.txt in the solr subdirectory, and check
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
-- execute: ant example
-- From path: Sourcerer/infrastructure/services/solr-1.3-patched
-- copy solr.war into solr-config

latest version of sourcerer solr component
-- execute: ant search-server
-- From path: Sourcerer/bin
-- copy: sourcerer-search.jar to solr-config/deployment-base/installation/solr/scs/lib/
--- cp sourcerer-search.jar ../infrastructure/services/solr-config/deployment-base/installation/solr/scs/lib/