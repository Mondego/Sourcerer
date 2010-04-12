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

import sys

if not len(sys.argv) == 9:
    sys.exit(sys.argv[0] + " needs 8 arguments:\n  loeid_inclusive hieid_exclusive cluster_root_path index_pass java_home_path cluster_q_name eids_per_node batch_size")

PORT  = 9000
LOEID = int(sys.argv[1])
HIEID = int(sys.argv[2])

ROOT  = sys.argv[3]
PASS  = sys.argv[4]
JAVA_HOME = sys.argv[5]
Q = sys.argv[6]

EIDS_PER_NODE = int(sys.argv[7])
# maximum number of cluster nodes to submit to in a batch
BATCH_SIZE = int(sys.argv[8])

assert LOEID < HIEID
assert EIDS_PER_NODE < (HIEID - LOEID)

BATCH_RUNS = ROOT + "/batchruns"

print "generating batched runs for Range: ", LOEID, HIEID

batch = 1

_fpath = BATCH_RUNS + "/pass-" + PASS + "_batch-" + `0`  +  ".sh"
batchfile = open(_fpath, 'w')

# print "#!/bin/bash"
batchfile.write("#!/bin/bash\n")

for start in range(LOEID, HIEID, EIDS_PER_NODE):
    end = start + EIDS_PER_NODE
    if end > HIEID:
        end = HIEID
    
    # 1 min delay before each submission
    if(start > LOEID):
        batchfile.write("sleep 60") 
        batchfile.write("\n")
    
    # execute runqsub.sh 
    # low_eid hi_eid cluster_root_path pass_number java_home cluster_q_name
    _cmd =  ROOT+"/runqsub.sh", start, end, ROOT, PASS, JAVA_HOME, Q, PORT, " > ", BATCH_RUNS + "/" + `start` + "_" + `end` + ".out &"
    PORT = PORT + 1
    batchfile.write(' '.join([str(s) for  s in _cmd]))
    batchfile.write("\n")
    
    
    batch = batch + 1
    
    if batch % BATCH_SIZE == 0:
        batchfile.write('echo "Done submitting all jobs in batch ' + `(batch/BATCH_SIZE)-1` + '"\n')
        batchfile.close()
        _fpath = BATCH_RUNS + "/pass-" + PASS + "_batch-" + `batch/BATCH_SIZE`  + ".sh"
        batchfile = open(_fpath, 'w')
        #print "#!/bin/bash"
        batchfile.write("#!/bin/bash\n")
        
batchfile.close()        
print "done"


