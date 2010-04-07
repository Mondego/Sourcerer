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

finish = False
idle = False
sleeptime = 60 # seconds

host = sys.argv[1]
port = sys.argv[2]

SOLR_STAT = host + ' not contacted yet'

# wait for solr server to initialize before starting polling
time.sleep(60)

while (True):
	
	params = urllib.urlencode({'command': 'status','wt':'python'})
	headers = {"Content-type": "application/x-www-form-urlencoded",
	           "Accept": "text/plain"}
	conn = httplib.HTTPConnection(host, port)
	conn.request("POST", "/solr/scs/dataimport", params, headers)
	response = conn.getresponse()
	
	if not (response.status == 200):
		SOLR_STAT = host + ' Solr server did not send HTTP 200'
		break
	
	result = response.read()
	rsp = eval(result)
	
	# print "IMPORT RESPONSE: ", rsp['importResponse']
	
	status = rsp['status']
	
	for s in rsp['statusMessages']:
		if s == '':
			if rsp['statusMessages'][''].startswith('Indexing completed'):
				finish = True
	
	if status == 'idle':
		idle = True
	else:
		idle = False
	
	if finish or idle:
		if idle and finish:
			SOLR_STAT = host + ' Done Indexing'
		elif idle and (not finish):
			SOLR_STAT = host + ' Idle but not indexed'
		elif (not idle) and finish:
			SOLR_STAT = host + ' Not idle but indexing finished'
		
		print host + ' ' + str(datetime.now()) + ' ' + str(rsp['statusMessages'])
		break
	
	# wait for an hour before next poll
	print host + ' ' + str(datetime.now()) + ' ' + str(rsp['statusMessages'])
	time.sleep(sleeptime)

print SOLR_STAT
