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

params = urllib.urlencode({'command': 'abort', 'wt':'python'})
headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}
conn = httplib.HTTPConnection(host, port, timeout=300)

response = None
try:
    conn.request("POST", "/solr/scs/dataimport", params, headers)
    response = conn.getresponse()
except IOError, err:
    sys.exit(str(datetime.now()) + " - " + host + ' - Solr server had problem when asked to abort indexing. ' + `err` + '\n')
	
if not (response.status == 200):
	print >> sys.stderr, str(datetime.now()) + " - " + host + ' - Solr server did not send HTTP 200 after abort command. Got ' + str(response.status)
else:
	rsp = eval(response.read())
	print str(datetime.now()) + " - " + host + ' - Solr server done after asked to abort indexing. ' + str(rsp)

