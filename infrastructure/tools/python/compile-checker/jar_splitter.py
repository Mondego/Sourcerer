import json, MySQLdb, os, zipfile, hashlib
from subprocess import check_output, call, CalledProcessError, STDOUT
from threading import Thread, Lock

JARPROJECTINDEX = "/home/sourcerer/repo/jars/project/"
MOUNTPATH = "/home/sourcerer/repo"
HASHFILE = "/home/sourcerer/repo/jars/project-index.txt"

jfiles = [
      path for hash, i, path in [
          line.split() for line in open(HASHFILE, "r").readlines()[2:-1]]]
threadlist = []
print len(jfiles)

def cleanup(srcdir):
  if os.path.exists(srcdir):
    call(["rm", "-r", srcdir])
  os.mkdir(srcdir)

def createGroups(seedlist, part):
  if ".class" in part:
    return seedlist
  parts = [createGroups(seedlist[:] + [item], part[item]) for item in part]
  return parts[0] if len(parts) == 1 else parts

def unpack(items, itemlist):
  if type(items[0]) == str:
    itemlist.append(tuple(items))
    return
  for item in items:
    unpack(item, itemlist)

def find(pack, groupmap):
  packtup = tuple(pack.split("."))
  if packtup in groupmap:
    return groupmap[packtup]
  return find(".".join(packtup[:-1]), groupmap)

def partition(dirlist):
  part = {}
  packages = set()
  for line in dirlist:
    if line.strip().endswith(".class"):
      packages.add(".".join(line.split("/")[:-1]))
      classname = line.split("/")[-1]
      buildstr = "part.setdefault('" + "', {}).setdefault('".join(line.split("/")[:-1]) + "', {}).setdefault('.class', []).append(classname)"
      eval(buildstr)
  groups = []
  unpack(createGroups([], part), groups)
  groupmap = dict([(groups[i], i) for i in range(len(groups))])
  grouppacks = {}
  for pack in packages:
    grouppacks.setdefault(groups[find(pack, groupmap)], set()).add(pack)
  reversemap = {}
  for gtype in grouppacks:
    for item in grouppacks[gtype]:
      reversemap.setdefault(item, []).extend(gtype)
  finalpart = {}
  for line in dirlist:
    if line.strip().endswith(".class"):
      finalpart.setdefault(reversemap[".".join(line.split("/")[:-1]], []).append(line)
  return finalpart, grouppacks

def unzip(zipFilePath, destDir):
  with zipfile.ZipFile(zipFilePath) as zipf:
    zipf.extractall(destDir)

idlock = Lock()
idcount = -1
def getid():
  with idLock:
    idcount += 1
    return idcount
recordlock = Lock()
jarmaps = {}
packagemaps = {}
hash_file = {}
def record(jfile, jid, packs, eq, hexdigest):
  jf = eq if eq!=jid else jid
  with recordlock:
    jarmaps.setdefault(jf, []).append(jfile)
    for pack in packs:
      packagemaps.setdefault(pack, set()).add(jf)

def getEquivalent(hexd, jf):
  with recordlock:
    return hash_file.setdefault(hexd, jf)
      

def splitfile(playg, jfile):
  try:  
    jarpath = os.path.join(MOUNTPATH, jfile, "jar.jar")
    unzip(jarpath, playg)
    dirs = check_output(["jar", "tf", jarpath], stderr = STDOUT).split("\n")
    finalpart, grouppart = partition(dirs)
    i = 0
    dirset = set(dirs)
    for parts in finalpart:
      jid = getid()
      name = "newjars/" + jid + ".jar"
      zipf = zipfile.ZipFile(name, "w"):
      for line in finalpart[parts]:
        zipf.write(os.path.join(playg, line))
      m = hashlib.md5()
      m.update(open(name, "rb").read())
      eq = getEquivalent(m.hexdigest(), jid)
      record(jfile, jid, grouppart[parts], eq, m.hexdigest())
      if eq != jid:
        zipf.close()
        check_output(["rm", name])
      else:
        if "META-INF/MANIFEST.MF" in dirset:
          zipf.write(os.path.join(playg, "META-INF/MANIFEST.MF"))
        zipf.close()

  except Exception, e:
    print jfile, e

splitfile("onejar", jfiles[0])
print jarmaps
print packagemaps
  

#dirs =  check_output(["jar", "tf", "/home/sourcerer/repo/jars/project/0/1/jar.jar"], stderr = STDOUT).split("\n")
