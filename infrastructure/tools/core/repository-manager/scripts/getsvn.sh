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

svn=""
root_folder=""
CMD_CO=""
CMD_INFO=""

if [ "$1" = "co" ]
then
	echo "Checking out code from SVN into the repository folders.."
else
	echo "These commands will be executed for check outs. Use argument [co] (without the brackets) to check out the code."
fi

echo "Started at: "`date`
echo

for f in `find . -name project.properties`
do
	root_folder=${f/%project.properties/} 
	
	# skips https://
	svn=`grep 'svn co http\\\:' $f`
	svn=${svn/#scmUrl=/}
	
	CMD_CO=$svn" "$root_folder"content/"
	CMD_CO=${CMD_CO/co/"co -q --non-interactive"}

	CMD_INFO=${svn/ co / info }

	CMD_CO=`exec echo $CMD_CO | tr -d '\\'`
	CMD_INFO=`exec echo $CMD_INFO | tr -d '\\'`

	len=${#svn}
	# skip https://
	if [[ "$len" -gt 0 ]]
	then
		echo $CMD_CO
		
		if [ "$1" = "co" ]
		then
			$CMD_CO
			sleep 2
		fi

		echo $CMD_INFO' > '$root_folder'checkout.info'
		
		if [ "$1" = "co" ]
		then
			$CMD_INFO > $root_folder"checkout.info"
			sleep 5
		fi
		
		echo
	fi	
done

echo "Ended at: "`date`
echo "Done."
