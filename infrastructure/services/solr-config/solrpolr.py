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
from socket import socket

finish = False
idle = False
sleeptime = 1800 # 30mins, poll solr server every half hour

host = sys.argv[1]
port = sys.argv[2]

RANGE = sys.argv[3]
PASS  = sys.argv[4]

SOLR_STAT = host + ' not contacted yet'
pollcycle = 1

while (True):
    
    print host + ' - ' + str(datetime.now()) + ' - Entering poll cycle: ' + `pollcycle`
    
    params = urllib.urlencode({'command': 'status','wt':'python'})
    headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}
    conn = httplib.HTTPConnection(host, port, timeout=300) # 5 minutes timeout
    
    response = None
    try:
        conn.request("POST", "/solr/scs/dataimport", params, headers)
        response = conn.getresponse()
    except IOError, err:
        print >> sys.stderr, str(datetime.now()) + ' - ' + host + ' - Solr server had problem during polling (Cycle:' + `pollcycle` + ' Range:' + RANGE + ' Pass:' + PASS  + ') - ' + `err`
        break    
    
    assert not response == None
    
    if not (response.status == 200):
        print >> sys.stderr, str(datetime.now()) + ' - ' + host + ' - Solr server did not send HTTP 200 during polling (Cycle:' + `pollcycle` + ' Range:' + RANGE + ' Pass:' + PASS  + ') - ' + "Got: " + response.status
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
            SOLR_STAT = host + ' Done Indexing' + ' Range:' + RANGE + ' Pass:' + PASS
        elif idle and (not finish):
            SOLR_STAT = host + ' Idle but not indexed'  + ' Range:' + RANGE + ' Pass:' + PASS
            print >> sys.stderr, host + ' Possibly did not finish indexing'  + ' Range:' + RANGE + ' Pass:' + PASS
        elif (not idle) and finish:
            SOLR_STAT = host + ' Not idle but indexing finished'  + ' Range:' + RANGE + ' Pass:' + PASS
        
        print str(datetime.now()) + ' - ' + host + ' - ' + str(rsp['statusMessages'])
        break
    
    print str(datetime.now()) + ' - ' + host + ' - ' + str(rsp['statusMessages'])
    pollcycle = pollcycle + 1
    time.sleep(sleeptime)

print SOLR_STAT
