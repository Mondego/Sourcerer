import os, zipfile, shelve, json

FINALMAPPATH = "project_map_compile_ready.json"
MOUNTPATH = "/home/sourcerer/repo"
DATABASEMAP = "project_map_filtered.json"

def readPropFile(propFilePath):
  props = {}
  with open(propFilePath, "r") as propFile:
    for line in propFile.readlines():
      if line.startswith("#") or line.strip() == "":
        continue
      parts = line.strip().split("=")
      props[parts[0]] = "=".join(parts[1:])
  return props

def makeProject(dirName, fileList):
  projectdetails = {"sourcererpath": dirName}
  projectdetails.update(readPropFile(dirName + "/project.properties"))
  projectdetails["iszipped"] = True if "content.zip" in fileList else False
  return projectdetails

def saveProject(projectDetails):
  if projectDetails["name"] not in shelveobj:
    shelveobj[projectDetails["name"]] = projectDetails
    shelveobj.sync()

def annotateProjectMap(repoDir, databasemap):
  finalmap = {}
  count = 0
  print "Annotating Project Map"
  for id in databasemap:
    abspath = os.path.join(repoDir, databasemap[id]["path"])
    filelist = os.listdir(abspath)
    if "project.properties" in filelist:
      finalmap[id] = databasemap[id]
      finalmap[id]["property_file"] = makeProject(abspath, filelist)
    count += 1
    if count % 1000 == 0:
      print count, "/", len(databasemap)
  print "Finished annotation"
  return finalmap

def main(mountpath, databasemap, finalmapPath = None):
  finalmap = annotateProjectMap(mountpath, databasemap)
  if finalmapPath:
    json.dump(finalmap, open(finalmapPath, "w"), sort_keys=True, indent=4, separators=(',', ': '))
  return finalmap

if __name__ == "__main__":
  databasemap = json.load(open(DATABASEMAP, "r"))
  main(MOUNTPATH, databasemap, FINALMAPPATH)


    

