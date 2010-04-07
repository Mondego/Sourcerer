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
LIB_INCLUDES=$L/ant-1.7.0.jar:$L/ant-commons-net-1.7.0.jar:$L/ant-launcher-1.7.0.jar:$L/commons-cli-1.1.jar:$L/guice-1.0.jar:$L/svnclientadapter-svnant_1.2.1.jar
RM_JAR="core-repomanager.jar" # core repomanager jar

java -Xmx1024m -cp ./:$LIB_INCLUDES:./$RM_JAR edu.uci.ics.sourcerer.repomanager.ContentFetcherRunner $1 $2 $3 $4