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

# !! THIS IS OBSELETE !! See/Use buildbin.sh


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
