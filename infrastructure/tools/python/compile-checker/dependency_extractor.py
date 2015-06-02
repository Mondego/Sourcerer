import MySQLdb
import json

CREDS = "creds.json"
JARHASHFILE = "/home/sourcerer/repo/jars/project-index.txt"
PROJECTDEPENDENCY = "projects_with_dependencies.json"

creds = json.load(open(CREDS, "r"))
db = MySQLdb.connect(host = creds["host"],
                     user = creds["user"],
                     passwd = creds["passwd"],
                     db = "utilization")

def createProjectMap():
  cur = db.cursor()
  cur.execute("select * from projects;")
  db.commit()
  count = int(cur.rowcount)
  pmap = {}
  hashmap = {}
  for rownum in range(count):
    id, type, name, desc, version, groop, path, source, hash, has_source = cur.fetchone()
    pmap[int(id)] = {
        "name": name,
        "description": desc,
        "type": type,
        "version": version,
        "groop": groop,
        "path": path,
        "source": source,
        "hash": hash,
        "has_source": has_source
    }
    hashmap[hash] = id
  return pmap, hashmap

def createJarHash(jarfile):
  jarmap = {}
  for line in open(jarfile, "r").readlines()[2:-1]:
    hash, num, path = line.strip().split()
    jarmap[hash] = path
  return jarmap

def createMavenDepend(projectdetails):
  return "<dependency org=\"" + projectdetails["groop"] + "\" name=\"" + projectdetails["name"] + "\" rev=\"" + projectdetails["version"] + "\"/>"

def createImportMap(projectmap, hashmap, jarhash):
  filescur = db.cursor()
  for id in projectmap:
    filescur.execute("select name, path, hash from files where project_id = ? file_type = 'JAR' group by name;", (id,))
    db.commit()
    count = int(cur.rowcount)
    depends = [cur.fetchone() for r in range(count)]
    projectmap[id]["depends"] = []
    for name, path, hash in depends:
      if hash in hashmap:
        if projectmap[hashmap[hash]]["type"] == "MAVEN":
          projectmap[id]["depends"].append((name, path, hash, True, createMavenDepend(projectmap[hashmap[hash]])))
        else:
          if hash in jarfile:
            projectmap[id]["depends"].append((name, path, hash, False, jarfile[hash]))
          else:
            projectmap[id]["depends"].append((name, path, hash, False, None))
      else:
        projectmap[id]["depends"].append((name, path, hash, None, None))

def filterProjects(projectmap):
  newmap = {}
  for id in projectmap:
    if projectmap[id]["type"] == "CRAWLED":
      newmap[id] = projectmap[id]
  return newmap

def main(jarhashfile, projectdependency = None):
  projectmap, hashmap = createProjectMap()
  jarhash = createJarHash(jarhashfile)
  createImportMap(projectmap, hashmap, jarhash)
  projectmap = filterProjects(projectmap)
  if projectdependency:
    json.dump(projectmap, open(PROJECTDEPENDENCY, "w"), sort_keys=True, indent=4, separators=(',', ': '))
  db.close()
  return projectmap

if __name__ == "__main__":
  main(JARHASHFILE, PROJECTDEPENDENCY)

