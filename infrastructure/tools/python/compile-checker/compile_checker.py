import os, zipfile, shelve, shutil, json
from threading import Thread, Lock
from subprocess import check_output, call, CalledProcessError, STDOUT

MOUNTLOCATION = "/home/sourcerer/repo"
SUCCESSMAP = "project_successmap_no_depends.json"
PROJECTMAP = "compile_list_no_depends.json"
PARTMAP = "project_compile_temp{0}.shelve"
THREADCOUNT = 24

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

def makebuildfile(path, data, srcdir):
  depends = set([(a,b,c,d,e) for a,b,c,d,e in data["depends"]])
  mavendepends = set([d for d in depends if d[3]])
  mavenline = "\n  ".join([d[4] for d in mavendepends])
  jardepends = depends - mavendepends
  jarline = "\n        ".join(["<pathelement path=\"{0}\" />".format(os.path.join(path, d[4], "jar.jar")) for d in jardepends])
  classpath = ""
  if jarline or mavenline:
    if mavenline:
      classpath += "\n      <classpath refid=\"default.classpath\" />"
    if jarline:
      classpath= "\n      <classpath>\n        " + jarline + "\n      </classpath>"

  desc = data["description"] if data["description"] else ""
  ivyfile = open("xml-templates/ivy-template.xml", "r").read().format(data["name"], mavenline)
  buildfile = open("xml-templates/build-template.xml", "r").read().format(data["name"], desc, classpath, "${build}", "${src}", data["encoding"] if "encoding" in data else "utf8")
  open(os.path.join(srcdir, "ivy.xml"), "w").write(ivyfile)
  open(os.path.join(srcdir, "build.xml"), "w").write(buildfile)
  return ivyfile, buildfile

def makeproject(path, data, srcdir):
  if data["property_file"]["iszipped"]:
    unzip(os.path.join(data["property_file"]["sourcererpath"], "content.zip"), srcdir)
  elif "content" in os.listdir(data["property_file"]["sourcererpath"]):
    copyrecursively(os.path.join(data["property_file"]["sourcererpath"], "content"), srcdir)
  return makebuildfile(path, data, srcdir)

def compile(srcdir):
  try:
    return True, check_output(["ant", "-f", os.path.join(srcdir, "build.xml"), "compile"], stderr = STDOUT)
  except CalledProcessError, e:
    return False, e.output

def cleanup(srcdir):
  if os.path.exists(srcdir):
    call(["rm", "-r", srcdir])
  os.mkdir(srcdir)

def clean(srcdir):
  call(["rm", "-r", srcdir])

def createProjectAndCompile(path, srcdir, namelist, projectmap, shelveobj):
  i = 0
  #print path, len(namelist)
  for id in namelist:
    id = str(id)
    if id in shelveobj or id == "323242":
      continue
    #print id
    ivyfile = ""
    buildfile = ""
    succ = False
    output = ""
    try:
      ivyfile, buildfile = makeproject(path, projectmap[id], srcdir)
      succ, output = compile(srcdir)
    except Exception, e:
      succ, output = False, str(e.message)
      print output
    output = "" if succ else output
    shelveobj[id] = {
        "build_files": {
            "ivyfile": ivyfile,
            "buildfile": buildfile
        },
        "success": succ,
        "output": output
    }
    shelveobj.sync()
    cleanup(srcdir)
    i += 1
    if i%100 == 0:
      print srcdir, i, "/", len(namelist)
  clean(srcdir)


def main(threadcount, mountloc, projectmap, successmaploc = None):
  shelvelist = []
  namelist = list(projectmap)
  threadlist = []
  for i in range(threadcount):
    tempdir = "src" + str(i)
    cleanup(tempdir)
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
  projectmap = json.load(open(PROJECTMAP, "r"))
  main(THREADCOUNT, MOUNTLOCATION, projectmap, SUCCESSMAP)

