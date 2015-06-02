import json, MySQLdb, os
from subprocess import check_output, call, CalledProcessError, STDOUT
from threading import Thread, Lock

CREDS = "creds.json"
PACKAGELIST = "needed_packages.json"
PACKAGE_DEPENDENCY_GUESS = "package_dependency_guess.json"
JARPROJECTINDEX = "/home/sourcerer/repo/jars/project-index.txt"
MOUNTPATH = "/home/sourcerer/repo"
JARPACKMAP = "jar_package_map.json"
MULTIJARPACKMAP = "multi_jar_package_map.json"
THREADCOUNT = 24

creds = json.load(open(CREDS, "r"))
db = MySQLdb.connect(host = creds["host"],
                     user = creds["user"],
                     passwd = creds["passwd"],
                     db = "utilization")
localjarmap = {}
multijarmap = {}
lock = Lock()
def makedict(ptype, name, version, groop, path, hash):
  return {
      "ptype": ptype,
      "name": name,
      "version": version,
      "groop": groop,
      "path": path,
      "hash": hash
  }

def normalized(version):
  return ".".join(
      ["{0:06d}".format(int(part)) if part.isdigit() else part 
       for part in version.replace("-", ".").split(".")])

def bestpick(packagelist):
  return makedict(*(packagelist[sorted(
      [(i, normalized(packagelist[i][2])) for i in range(len(packagelist))],
      key = lambda x: x[1],
      reverse = True)[0][0]]))

def process(allrows):
  namepackagemap = {}
  for ptype, name, version, groop, path, hash in allrows:
    namepackagemap.setdefault(name, []).append((ptype, name, version, groop, path, hash))
  return [bestpick(namepackagemap[name]) for name in namepackagemap]

def shorten(name):
  return ".".join(name.split(".")[:-1])

def getFromLocaljar(packname):
  if packname in localjarmap:
    return [{
        "ptype": "JAR",
        "name": "jar.jar",
        "path": localjarmap[packname]
    }]
  else:
    return []

def findPossiblePackages(packname, original):
  cur = db.cursor()
  try: 
    cur.execute("select project_type, name, version, groop, path, hash from projects where groop = %s;", (packname,))
  except Exception, e:
    print e, packname
    return []
  db.commit()
  count = int(cur.rowcount)
  if count != 0:
    return process(cur.fetchall())
  newname = shorten(packname)
  if newname != "" and newname != "org":
    return findPossiblePackages(newname, original)
  return []

def packagename(line):
  return ".".join(line.split("/")[:-1])

def addtomap(jfile):
  try:
    d = dict([(packagename(line), os.path.join(jfile, "jar.jar")) 
              for line in check_output(
                  [
                    "jar",
                    "tf",
                    os.path.join(MOUNTPATH, jfile, "jar.jar")],
                  stderr = STDOUT).split("\n")
              if ".class" in line])
    localjarmap.update(d)
    for k, v in d.items():
      with lock:
        multijarmap.setdefault(k, []).append(v)
  except CalledProcessError:
    print jfile

def makeJarpart(jfiles):
  for jfile in jfiles:
    addtomap(jfile)

def makeJarmap(hashfile):
  jfiles = [
      path for hash, i, path in [
          line.split() for line in open(hashfile, "r").readlines()[2:-1]]]
  threadlist = []
  print len(jfiles)
  for i in range(THREADCOUNT):
    threadlist.append(Thread(target = makeJarpart, args=(jfiles[i::THREADCOUNT],)))
    threadlist[i].setDaemon(True)
    threadlist[i].start()

  for i in range(THREADCOUNT):
    threadlist[i].join()
  json.dump(localjarmap, 
      open(JARPACKMAP, "w"),
      sort_keys=True, 
      indent=4, 
      separators=(',', ': '))
  json.dump(multijarmap,
      open(MULTIJARPACKMAP, "w"),
      sort_keys=True,
      indent=4,
      separators=(',', ': '))


def findBestPackage(packname):
  depends = getFromLocaljar(packname)
  if depends != []:
    return depends
  return findPossiblePackages(packname, packname)
if __name__ == "__main__":
  makeJarmap(JARPROJECTINDEX)
  json.dump(
      dict(
          [(packname, findBestPackage(packname)) 
          for packname in json.load(open(PACKAGELIST, "r"))["all"]]),
      open(PACKAGE_DEPENDENCY_GUESS, "w"),
      sort_keys=True, 
      indent=4, 
      separators=(',', ': '))

