import json

MISSINGPACKAGES = "project_package_depends.json"
COMPILEMAP = "compile_list_no_depends.json"
PACKAGETONEWJARS = "package_newjar_updated.json"
SMALLTOBIGJARS = "small_to_big_jars.json" 
PROJECTNEWJAR = "project_newjar_depends.json"
PROJECTFAILED = "project_failed_depends.json"

missing_packs = json.load(open(MISSINGPACKAGES))
compile_map = json,load(open(COMPILEMAP))
package_newjars = json.load(open(PACKAGETONEWJARS))
small_to_big = json.load(open(SMALLTOBIGJARS))

def choosebest(jarlist):
  oldjarmap = {}
  for i in range(len(jarlist)):
    for jar in jarlist[i]:
      oldjarmap.setdefault(i, set()).add(small_to_big[jar])
  minjar = min([len(v) for k,v in oldjarmap.items()])
  return oldjarmap[(i for i in oldjarmap if len(oldjarmap[i]) == minjar).next()]

def greedyget(packs):
  filemap = {}
  for pack in packs:
    for jar in package_newjars[pack]:
      filemap.setdefault(jar, set()).add(pack)
  mostcount = max([len(filemap[item]) for item in filemap])
  jars = [item for item in filemap if len(filemap[item]) == mostcount]
  finallist = []
  for jar in jars:
    nopacks = [pack for pack in packs if pack not in filemap[jar]]
    restjars = greedyget(nopacks)
    for i in range(len(restjars)):
      restjars[i].append(jar)
    finallist.extend(restjars)
  return finallist


def getMissing(packlist):
  found = []
  missing = []
  for pack in packlist:
    if pack in package_newjars:
      if type(package_newjars[pack]) == dict:
        found.extend(package_newjars[pack]["part"])
      else:
        found.append(pack)
    else:
      missing.append(pack)
  if missing:
    return missing, False
  return choosebest(greedyget(found)), True

idjarmap = {}
idfailmap = {}

def writeout():
  json.dump(idjarmap, open(PROJECTNEWJAR, "w"), indent = 4, sort_keys= True, separators = (",", ": "))
  json.dump(idfailmap, open(PROJECTFAILED, "w"), indent = 4, sort_keys= True, separators = (",", ": "))

def resolve():
  for id in missing_packs:
    print id
    jars, success = getMissing(missing_packs[id])
    if success:
      idjarmap[id] = jars
    else:
      idfailmap[id] = jars
  writeout()

resolve()
