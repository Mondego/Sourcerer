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

L="../lib" # library path
LIB_INCLUDES=$L/mahout-core-0.3-SNAPSHOT.jar:$L/slf4j-api-1.5.8.jar:$L/slf4j-jcl-1.5.8.jar:$L/commons-logging-1.1.1.jar:$L/uncommons-maths-1.2.jar:$L/mysql-connector-java-5.1.7-bin.jar:$L/commons-dbcp-1.2.2.jar:$L/commons-pool-1.5.4.jar:$L/j2ee.jar
SW_JAR="sourcerer-ml.jar"

java -Xmx2048m -cp ./:$LIB_INCLUDES:./sourcerer-db.jar:./$SW_JAR edu.uci.ics.sourcerer.ml.db.tools.SimilarityWriterRunner $1 $2 $3 $4 $5 $6 $7 $8 $9 ${10} ${11} ${12} ${13} ${14} ${15} ${16}

#./run-similarity-writer.sh --output /Users/shoeseal/sandbox/Sourcerer/infrastructure/tools/core/machine-learning/sw.output --database-url jdbc:mysql://mondego.calit2.uci.edu:3307/sourcerer_t2 --database-user sourcerer --database-password sourcerer4us --fqn-use-file /Users/shoeseal/sandbox/Sourcerer/infrastructure/tools/core/machine-learning/test/data.big/usage.txt --similarity HAMMING_DISTANCE
#./run-similarity-writer.sh --output ../data/op/simw --database-url jdbc:mysql://mondego.calit2.uci.edu:3307/sourcerer_t2 --database-user <sourcerer.db.user> --database-password <sourcerer.db.user> --fqn-use-file ../data/ip/usage.txt --similarity TANIMOTO_COEFFICIENT

