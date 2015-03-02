import os, zipfile, shelve, shutil, json
from threading import Thread, Lock
from subprocess import check_output, call, CalledProcessError, STDOUT

MOUNTLOCATION = "/mnt/sourcerer_repo/repo/"
PROJECTMAP = "javaprojects.shelve"
SUCCESSMAP = "successmap.json"

shelveobj = shelve.open(PROJECTMAP)
lock = Lock()
def copyrecursively(source_folder, destination_folder):
  for root, dirs, files in os.walk(source_folder):
    for item in files:
        src_path = os.path.join(root, item)
        dst_path = os.path.join(destination_folder, src_path.replace(source_folder+"/", ""))
        if os.path.exists(dst_path):
            if os.stat(src_path).st_mtime > os.stat(dst_path).st_mtime:
                shutil.copy2(src_path, dst_path)
        else:
            shutil.copy2(src_path, dst_path)
    for item in dirs:
        src_path = os.path.join(root, item)
        dst_path = os.path.join(destination_folder, src_path.replace(source_folder+"/", ""))
        if not os.path.exists(dst_path):
            os.mkdir(dst_path)

def unzip(zipFilePath, destDir):
  with zipfile.ZipFile(zipFilePath) as zipf:
    zipf.extractall(destDir)

def makeproject(path, data, srcdir):
  if data["iszipped"]:
    unzip(data["sourcererpath"] + "/content.zip", srcdir)
  elif "content" in os.listdir(data["sourcererpath"]):
    copyrecursively(data["sourcererpath"] + "/content", srcdir)

def GatherOutput(output):
  print output
  return True

def compile(srcdir):
  findbuild = check_output(["find", srcdir, "-name", "build.xml"])
  if findbuild != "":
    try:
      return True, check_output(["ant", "-f", findbuild.strip(), "compile"], stderr = STDOUT)
    except CalledProcessError, e:
      return False, e.output

  return False, "No Build File"

def cleanup(srcdir):
  call(["rm", "-r", srcdir])
  os.mkdir(srcdir)
successmap = shelve.open(SUCCESSMAP)
def setsuccess(name):
  with lock:
    successmap[name] = {"success": True, "output": ""}

def setfail(name, output):
  with lock:
    successmap[name] = {"sucess": False, "output": output}

def createProjectAndCompile(path, srcdir, namelist):
  i = 0
  for name in namelist:
    if name in successmap:
      continue
    print name
    try:
      makeproject(path, shelveobj[name], srcdir)
      succ, output = compile(srcdir)
    except Exception, e:
      succ, output = False, str(e)
      print output
    if succ:
      setsuccess(name)
    else:
      setfail(name, output)
    cleanup(srcdir)
    i+= 1
    if i%10 == 0:
      print srcdir, i
      with lock:
        successmap.sync()

threadcount = 8
for i in range(threadcount):
  cleanup("src" + str(i))
namelist = list(shelveobj)
for i in range(threadcount):
  Thread(target=createProjectAndCompile, args=(MOUNTLOCATION, "src" + str(i), namelist[i::threadcount])).start()

