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

import httplib, urllib, sys, time
from datetime import datetime

host = sys.argv[1]
port = sys.argv[2]
loeid = sys.argv[3]
hieid = sys.argv[4]

params = urllib.urlencode({'command': 'full-import', 'lo_eid_incl': loeid, 'hi_eid_excl': hieid, 'wt':'python'})

headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}

conn = httplib.HTTPConnection(host, port)
conn.request("POST", "/solr/scs/dataimport", params, headers)
response = conn.getresponse()
	
if not (response.status == 200):
	print host + ' Solr server did not send HTTP 200. Got ' + str(response.status) + '. Command: full-import. Range: ' + loeid + ' to ' + hieid
else:
	rsp = eval(response.read())
	print host + ' ' + str(datetime.now()) + ' ' + str(rsp)

