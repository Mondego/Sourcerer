#!/bin/bash

# =====================================================================
# Sourcerer: An infrastructure for large-scale source code analysis.
# Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
# ===================================================================== 
# @author Sushil Bajracharya (bajracharya@gmail.com)

if [ $# -ne 3 ] ; then
    echo 'This script requires 3 arguments: user password db-url:port'
    echo 'Run this script from <Sourcerer>/infrastructure/services/solr-config'
    exit 0
fi


ant search-server -f ../../../bin/build.xml
cp ../../../bin/sourcerer-search.jar ./deployment-base/installation/solr/scs/lib/
cp ../../../bin/sourcerer-db.jar ./deployment-base/installation/solr/scs/lib/

# append to end of url to prevent timeout ?netTimeoutForStreamingResults=3660
url='jdbc:mysql:\/\/'$3'\/sourcerer_eclipse?netTimeoutForStreamingResults=10800'
user=$1
password=$2

jettyxmlin='./conf/jetty.xml'
jettyxml1='./bin/solr-server-pass1/etc/jetty.xml'
jettyxml2='./bin/solr-server-pass2/etc/jetty.xml'

dbcfg1in='./conf/db-data-config-pass1.xml'
dbcfg1out='./bin/solr-server-pass1/installation/solr/scs/conf/db-data-config.xml'


dbcfg2in='./conf/db-data-config-pass2.xml'
dbcfg2out='./bin/solr-server-pass2/installation/solr/scs/conf/db-data-config.xml'

# clean bin
rm -rf ./bin/*

# make first pass
mkdir ./bin/solr-server-pass1
cp -r ./deployment-base/* ./bin/solr-server-pass1
cp ./conf/schema-pass1.xml ./bin/solr-server-pass1/installation/solr/scs/conf/schema.xml
sed "s/!sourcerer.db.url!/$url/g;s/!sourcerer.db.user!/$user/g;s/!sourcerer.db.password!/$password/g" $dbcfg1in > $dbcfg1out
sed "s/!sourcerer.db.url!/$url/g;s/!sourcerer.db.user!/$user/g;s/!sourcerer.db.password!/$password/g" $jettyxmlin > $jettyxml1
cp ./conf/solrconfig-pass1.xml ./bin/solr-server-pass1/installation/solr/scs/conf/solrconfig.xml

# make second pass
mkdir ./bin/solr-server-pass2
cp -r ./deployment-base/* ./bin/solr-server-pass2
cp ./conf/schema-pass2.xml ./bin/solr-server-pass2/installation/solr/scs/conf/schema.xml
sed "s/!sourcerer.db.url!/$url/g;s/!sourcerer.db.user!/$user/g;s/!sourcerer.db.password!/$password/g" $dbcfg2in > $dbcfg2out 
sed "s/!sourcerer.db.url!/$url/g;s/!sourcerer.db.user!/$user/g;s/!sourcerer.db.password!/$password/g" $jettyxmlin > $jettyxml2
cp ./conf/solrconfig-pass2.xml ./bin/solr-server-pass2/installation/solr/scs/conf/solrconfig.xml
