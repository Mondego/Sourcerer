import json, os
from lxml.etree import *
from threading import Thread

PROJECT_COMPILE_SUCCESS = "projects_compile_merged_v2.json"
CRMAP = "project_map_compile_ready.json"
MOUNTLOCATION = "/home/sourcerer"
THREADCOUNT = 24

def relativeloc(path):
  return os.path.join("../../../repo/", path, "content")

def relativejar(path):
  c = -3 if "jar.jar" in path else -2
  return os.path.join("../../../repo/jars/project/", *path.split("/")[c:])

pcm = json.load(open(PROJECT_COMPILE_SUCCESS))
pcrmap = json.load(open(CRMAP))
ids = [id for id in pcm if pcm[id]["success"]]

def writebuilds(i, ids):
  x = 0
  for id in ids:
    buildf = pcm[id]["build_files"]["buildfile"]
    bfxml = fromstring(buildf)
    bfxml[2] = fromstring('<property name="src" location="' + relativeloc(pcrmap[id]["path"]) + '" />')
    for i in range(len(bfxml[6][1])):
      if "refid" not in bfxml[6][1][i].attrib:
        for j in range(len(bfxml[6][1][i])):
          bfxml[6][1][i][j] = fromstring('<pathelement path="' + relativejar(bfxml[6][1][i][j].attrib["path"]) + '" />')
    newbuild = tostring(bfxml, pretty_print= True)
    path = os.path.join(MOUNTLOCATION, "repo-bytecode", pcrmap[id]["path"])
    try:
      open(os.path.join(path, "build.xml"), "w").write(newbuild)
      open(os.path.join(path, "ivy.xml"), "w").write(pcm[id]["build_files"]["ivyfile"])
    except Exception:
      print id, path
    x += 1
    if x % 1000 == 0:
      print "Thread", i, x, "/", len(ids)

t = []
for i in range(THREADCOUNT):
  t.append(Thread(target=writebuilds, args=(i, ids[i::THREADCOUNT])))
  t[i].start()

for i in range(THREADCOUNT):
  t[i].join()
