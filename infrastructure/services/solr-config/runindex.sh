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

if [ $# -ne 10 ] ; then
    echo '$HOSTNAME says: expecting 10 arguments; runindex.sh to be called by runqsub.sh. Do not run it directly.'
    exit 0
fi

INDEXDIR=$1
LPROP=$2
SOLR=$3
LOEID=$4
HIEID=$5
ROOT=$6
PASS=$7
JETTYLOGDIR=$8
JAVA_HOME=$9"/"
JETTYXML=${10}

export JAVA_HOME
PATH=$PATH:$JAVA_HOME/bin
export PATH

java -Djetty.requestlogs=$JETTYLOGDIR -Dsolr.data.dir=$INDEXDIR -Djava.util.logging.config.file=$LPROP -Dsolr.solr.home=$SOLR"/installation/solr/" -Djetty.port=8983 -jar start.jar $JETTYXML &
sleep 60s
python $ROOT/indexsolrrange.py $HOSTNAME 8983 $LOEID $HIEID
sleep 30s
python $ROOT/solrpolr.py $HOSTNAME 8983 > $ROOT"/jobs/pass"$PASS/$LOEID"_"$HIEID".solrstat.txt"
echo $HOSTNAME Indexing Job done. Pass $PASS Range $LOEID"_"$HIEID > $ROOT"/jobs/pass"$PASS/$LOEID"_"$HIEID".jobstat.txt"
