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

echo "* Make sure you had run buildbin.sh before you run this script *"
echo 'Run this script from <Sourcerer>/infrastructure/services/solr-config'

ROOT=./clusterroot

# clean clusterroot
rm -rf $ROOT/* 

mkdir $ROOT/indexroot
mkdir $ROOT/indexroot/pass1
mkdir $ROOT/indexroot/pass2

mkdir $ROOT/jobs
mkdir $ROOT/jobs/pass1
mkdir $ROOT/jobs/pass2

mkdir $ROOT/solrlogs
mkdir $ROOT/solrlogs/pass1
mkdir $ROOT/solrlogs/pass2

mkdir $ROOT/jettylogs
mkdir $ROOT/jettylogs/pass1
mkdir $ROOT/jettylogs/pass2

mkdir $ROOT/solrbin
mkdir $ROOT/batchruns

BIN=./bin
cp -r $BIN/ $ROOT/solrbin/

cp cleanop.sh $ROOT/
cp runindex.sh $ROOT/
cp runqsub.sh $ROOT/
cp solrpolr.py $ROOT/
cp indexsolrrange.py $ROOT/
cp abortindex.py $ROOT/
cp genqsubbatches.py $ROOT/
