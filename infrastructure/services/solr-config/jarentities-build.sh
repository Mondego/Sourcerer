#!/bin/bash

# clean bin
rm -rf ./bin/*

# make first pass
mkdir ./bin/solr-server-pass1
cp -r ./deployment-base/* ./bin/solr-server-pass1
cp ./jarentity-conf/jarentity-schema-pass1.xml ./bin/solr-server-pass1/installation/solr/scs/conf/schema.xml
cp ./jarentity-conf/jarentity-db-data-config-pass1.xml ./bin/solr-server-pass1/installation/solr/scs/conf/db-data-config.xml
cp ./jarentity-conf/jarentity-solrconfig-pass1.xml ./bin/solr-server-pass1/installation/solr/scs/conf/solrconfig.xml
cp ./etc/jetty-pass1.xml ./bin/solr-server-pass1/etc/jetty.xml


# make second pass
mkdir ./bin/solr-server-pass2
cp -r ./deployment-base/* ./bin/solr-server-pass2
cp ./jarentity-conf/jarentity-schema-pass2.xml ./bin/solr-server-pass2/installation/solr/scs/conf/schema.xml
cp ./jarentity-conf/jarentity-db-data-config-pass2.xml ./bin/solr-server-pass2/installation/solr/scs/conf/db-data-config.xml
cp ./jarentity-conf/jarentity-solrconfig-pass2.xml ./bin/solr-server-pass2/installation/solr/scs/conf/solrconfig.xml
cp ./etc/jetty-pass2.xml ./bin/solr-server-pass2/etc/jetty.xml