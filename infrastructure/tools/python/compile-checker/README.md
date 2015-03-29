dependency_extractor.py
=====================
Goes through the Sourcerer database, makes a map of projects with their corresponding data including dependencies.

projectmap_creator.py
=====================
Creates a map of the projects with their build information.

compile_checker.py
=====================
Goes through each project, creates the build files required, compiles them and records the results. Returns a map of project to the build success, error messages if any, and the build files used.

full_workflow.py
=====================
Executes the three python scripts in order and gets the output.
Set SAVE_INTERMEDIATE to True to save the intermediate files. Set the names and locations in the various constants found.

creds.json
=====================
Credentials required to access the sourcerer database. Fill it up with the right values.

xml-templates
=====================
The templates for the build.xml and ivy.xml files used by compile_checker.py to compile each project.