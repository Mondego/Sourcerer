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

if [ $# -ne 11 ] ; then
    echo '$HOSTNAME says: runindex.sh needs 11 arguments. (runindex.sh to be called by runqsub.sh, do not run it directly)' >&2
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
PORT=${11}

JOBDIR=$ROOT"/jobs/pass"$PASS

export JAVA_HOME
# note hardcoded python path
PATH=$PATH:$JAVA_HOME/bin:"/pkg/python/2.6.2/bin"
export PATH

time_start=`date +%s`

# log requests sent so far
echo "!REQ!	"`date`"    "$HOSTNAME" "$PORT" "$LOEID"    "$HIEID"    "$PASS

# change to the Solr directory to start the server from
cd $SOLR

SOLR_HOME=$JOBDIR"/"$LOEID"_"$HIEID"_solrhome"

# start Solr server
java -Xmx1200m -Djetty.requestlogs=$JETTYLOGDIR -Dsolr.data.dir=$INDEXDIR -Djava.util.logging.config.file=$LPROP -Dsolr.solr.home=$SOLR_HOME -Djetty.port=$PORT -jar start.jar $JETTYXML &

# wait 5 min before sending indexing command
sleep 300

# send indexing command 
python $ROOT/indexsolrrange.py $HOSTNAME $PORT $LOEID $HIEID

# wait 5 min before starting polling the index server
sleep 300

# start polling the Solr server
python $ROOT/solrpolr.py $HOSTNAME $PORT $LOEID"_"$HIEID $PASS # > $ROOT"/jobs/pass"$PASS"/"$LOEID"_"$HIEID".solrpolr.out"

# polling process ended (either indexing done or problem with Solr server)
time_end=`date +%s`
RUNTIME=`expr $(( $time_end - $time_start ))`
time_exec=`printf "%02d:%02d:%02d\n" $((RUNTIME/3600)) $((RUNTIME/60%60)) $((RUNTIME%60))`

# log responses received
echo "!RES!	"`date`"    "$HOSTNAME" "$PORT" "$LOEID"    "$HIEID"    "$PASS" "$RUNTIME"  "$time_exec

echo $HOSTNAME" :"$PORT" Indexing job request ended in time (hh:mm:ss) - "$time_exec". Pass "$PASS" Range "$LOEID"_"$HIEID" (check for problems in logs/stderr)" > $ROOT"/jobs/pass"$PASS"/"$LOEID"_"$HIEID".jobend.out"

