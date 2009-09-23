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
LIB_INCLUDES=$L/htmlparser-1.6.jar:$L/log4j-1.2.8.jar
CC_JAR="codecrawler.jar"

java -Xmx2048m -cp ./:$LIB_INCLUDES:./$CC_JAR edu.uci.ics.sourcerer.codecrawler.cmdimpl.CmdCrawler $1 $2 $3 $4