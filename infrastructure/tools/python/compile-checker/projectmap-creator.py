import os, zipfile, shelve

PROJECTMAP = "javaprojects.shelve"
MOUNTPATH = "/mnt/sourcerer_repo/repo/"

shelveobj = shelve.open(PROJECTMAP)

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

def createProjectMap(path):
  count = 0
  upperlist = os.listdir(path)
  print "upper: ", len(upperlist)
  for upper in upperlist:
    if not os.path.isdir(path + "/" + upper):
      continue
    lowerlist = os.listdir(path + "/" + upper)
    print "lower: ", len(lowerlist)
    
    for lower in lowerlist:
      finalpath = path + "/" + upper + "/" + lower
      if not os.path.isdir(finalpath):
        continue
      fileList = os.listdir(finalpath)
      if "project.properties" in fileList:
        saveProject(makeProject(finalpath, fileList))
        count += 1
        print count

createProjectMap(MOUNTPATH)

    

