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

if [ $# -ne 2 ] ; then
    echo 'This script requires 2 arguments: clusterroot pass'
    exit 0
fi

CLUSTERROOT=$1
PASS=$2

JL=$CLUSTERROOT'/jettylogs/pass'$PASS'/*'
I=$CLUSTERROOT'/indexroot/pass'$PASS'/*'
SL=$CLUSTERROOT'/solrlogs/pass'$PASS'/*'
O=$CLUSTERROOT'/jobs/pass'$PASS'/*'

rm -rf $JL
rm -rf $I
rm -rf $SL
rm -rf $O

