import compile_checker
import dependency_extractor
import projectmap_creator

THREADCOUNT = 24
MOUNTLOCATION = "/home/sourcerer/repo"
JARHASHFILE = "/home/sourcerer/repo/jars/project-index.txt"
SAVE_INTERMEDIATE = True
PROJECTDEPENDENCYMAP = "projects_with_dependencies.json"
COMPILEREADYMAP = "project_map_compile_ready.json"
COMPILEDSUCCESSMAP = "project_compile_checked.json"

compile_checker.main(
    THREADCOUNT,
    MOUNTLOCATION,
    projectmap_creator.main(
        MOUNTLOCATION,
        dependency_extractor.main(
            JARHASHFILE,
            PROJECTDEPENDENCYLOCATION if SAVE_INTERMEDIATE else None),
        COMPILEREADYMAP if SAVE_INTERMEDIATE else None),
    COMPILEDSUCCESSMAP if SAVE_INTERMEDIATE else None)

print "Completed"