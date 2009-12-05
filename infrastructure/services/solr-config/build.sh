#!/bin/bash

# clean bin
rm -rf ./bin/*

# make first pass
mkdir ./bin/solr-server-pass1
cp -r ./deployment-base/* ./bin/solr-server-pass1
cp ./conf/schema-pass1.xml ./bin/solr-server-pass1/installation/solr/scs/conf/schema.xml
cp ./conf/db-data-config-pass1.xml ./bin/solr-server-pass1/installation/solr/scs/conf/db-data-config.xml
cp ./conf/solrconfig-pass1.xml ./bin/solr-server-pass1/installation/solr/scs/conf/solrconfig.xml
cp ./etc/jetty-pass1.xml ./bin/solr-server-pass1/etc/jetty.xml


# make second pass
mkdir ./bin/solr-server-pass2
cp -r ./deployment-base/* ./bin/solr-server-pass2
cp ./conf/schema-pass2.xml ./bin/solr-server-pass2/installation/solr/scs/conf/schema.xml
cp ./conf/db-data-config-pass2.xml ./bin/solr-server-pass2/installation/solr/scs/conf/db-data-config.xml
cp ./conf/solrconfig-pass2.xml ./bin/solr-server-pass2/installation/solr/scs/conf/solrconfig.xml
cp ./etc/jetty-pass2.xml ./bin/solr-server-pass2/etc/jetty.xml