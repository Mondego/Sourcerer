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
    echo 'This script requires 2 aruments: index-root merge-index-folder'
    echo 'index-root is parent folder for index under pass 1 or 2 (eg: <clusterroot>/indexroot/pass1)'
    exit 0
fi

INDEXROOT=$1
MERGEOUT=$2

I=`for x in \`find $INDEXROOT -name index -type d\`; do echo -n $x" " ; done;`

./run-index-merger.sh $MERGEOUT $I