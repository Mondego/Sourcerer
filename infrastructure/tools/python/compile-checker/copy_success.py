import compile_checker as cc

import os, zipfile, shelve, shutil, json
from threading import Thread, Lock
from subprocess import check_output, call, CalledProcessError, STDOUT

MOUNTLOCATION = "/home/sourcerer/repo"
SUCCESSMAP = "projects_compile_merged_v2.json"
CRMAP = "project_map_compile_ready.json"
PARTMAP = "project_compile_temp{0}.shelve"
COMPLETEMAP = "projects_compile_copy_status.json"
THREADCOUNT = 24

def makebuildfile(data, srcdir):
  open(os.path.join(srcdir, "ivy.xml"), "w").write(data["build_files"]["ivyfile"])
  open(os.path.join(srcdir, "build.xml"), "w").write(data["build_files"]["buildfile"])

def makeproject(sourcepath, data, srcdir):
  cc.copyrecursively(os.path.join(sourcepath, "content"), srcdir)
  return makebuildfile(data, srcdir)

def savebuildfiles(srcdir, sourcepath):
  dstpath = os.path.join("bytecode", sourcepath, "build")
  call(["mkdir", "-p", dstpath])
  cc.copyrecursively(os.path.join(srcdir, "build"), dstpath)

def createProjectAndCompile(path, srcdir, namelist, projectmap, sourcepaths, shelveobj):
  i = 0
  for id in namelist:
    id = str(id)
    if id in shelveobj:
      continue
    #print id
    succ = False
    output = ""
    try:
      makeproject(os.path.join(MOUNTLOCATION, sourcepaths[id]), projectmap[id], srcdir)
      succ, output = cc.compile(srcdir)
    except Exception, e:
      succ, output = False, str(e.message)
      print output
    if succ:
      savebuildfiles(srcdir, sourcepaths[id])
    shelveobj[id] = {"success": succ}
    cc.cleanup(srcdir)
    i += 1
    if i%10 == 0:
      print srcdir, i, "/", len(namelist)
  cc.clean(srcdir)


def main(threadcount, mountloc, cr_map, projectmap, successmaploc = None):
  shelvelist = []
  namelist = [id for id in projectmap if projectmap[id]["success"]]
  sourcepaths = {}
  for id in namelist:
    sourcepaths[id] = str(cr_map[id]["path"])
  threadlist = []
  for i in range(threadcount):
    tempdir = "src" + str(i)
    cc.cleanup(tempdir)
    shelvelist.append(shelve.open(PARTMAP.format(i)))
    if len(shelvelist[i]) > 0:
      keys = shelvelist[i]["Keys"]
    else :
      keys = namelist[i::threadcount]
      shelvelist[i]["Keys"] = keys
      shelvelist[i].sync()

    threadlist.append(
        Thread(
            target=createProjectAndCompile,
            args=(mountloc,
                  tempdir,
                  keys,
                  projectmap,
                  sourcepaths,
                  shelvelist[i])))
    threadlist[i].isDaemon = True
    threadlist[i].start()

  for i in range(threadcount):
    threadlist[i].join()

  final_success = {}
  for i in range(len(shelvelist)):
    final_success.update(shelvelist[i])
    del final_success["Keys"]
    call(["rm", PARTMAP.format(i)])

  if successmaploc:
    json.dump(
        final_success,
        open(successmaploc, "w"),
        sort_keys=True,
        indent=4,
        separators=(',', ': '))

  return final_success

if __name__ == "__main__":
  successmap = json.load(open(SUCCESSMAP, "r"))
  cr_map = json.load(open(CRMAP, "r"))
  main(THREADCOUNT, MOUNTLOCATION, cr_map, successmap, COMPLETEMAP)

